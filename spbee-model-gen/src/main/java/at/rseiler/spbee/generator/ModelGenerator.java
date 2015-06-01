package at.rseiler.spbee.generator;

import com.sun.codemodel.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelGenerator {

    private final DataSource dataSource;
    private final String packageName;
    private final String spName;
    private final String spArgs;

    public static void main(String[] args) throws IOException, SQLException, JClassAlreadyExistsException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
        DataSource dataSource = (DataSource) ctx.getBean("dataSource");

        String[] spArgs = Arrays.copyOfRange(args, 2, args.length);
        String spArgString = String.join(",", spArgs);

        new ModelGenerator(dataSource, args[0], args[1], spArgString).execute();
    }

    public ModelGenerator(DataSource dataSource, String packageName, String spName, String spArgs) {
        this.dataSource = dataSource;
        this.packageName = packageName;
        this.spName = spName;
        this.spArgs = spArgs;
    }

    public void execute() {
        try {
            List<ColumnDataList> columnDataLists = new ArrayList<>();
            try (Connection connection = dataSource.getConnection(); CallableStatement statement = connection.prepareCall("{call " + spName + " (" + spArgs + ")}")) {
                ResultSet resultSet = statement.executeQuery();

                do {
                    ResultSet rs = statement.getResultSet();

                    if (rs != null) {
                        if (rs.next()) {
                            handleResultSetMetaData(columnDataLists, rs);
                        }
                        System.out.println();
                    } else if (resultSet.next()) {
                        handleResultSetMetaData(columnDataLists, resultSet);
                    }
                } while (statement.getMoreResults());
            }

            generateSpBeeModelClasses(spName, columnDataLists);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleResultSetMetaData(List<ColumnDataList> columnDataLists, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        List<ColumnData> columnDataList = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
//            System.out.print(metaData.getColumnName(i) + "[" + metaData.getColumnType(i) + metaData.getColumnTypeName(i) + "] ");
            columnDataList.add(new ColumnData(metaData.getColumnType(i), metaData.getColumnLabel(i)));
        }

        columnDataLists.add(new ColumnDataList(rs.next(), columnDataList));
    }

    private void generateSpBeeModelClasses(String spName, List<ColumnDataList> columnDataLists) throws JClassAlreadyExistsException, IOException {
        if (columnDataLists.size() > 1) {
            generateResultSetClass(spName, columnDataLists);
        }

        generateEntityClass(spName, columnDataLists);
    }

    private void generateResultSetClass(String spName, List<ColumnDataList> columnDataLists) throws JClassAlreadyExistsException, IOException {
        JCodeModel model = new JCodeModel();
        JPackage jPackage = model._package(packageName + ".resultset");
        JDefinedClass resultSetClass = jPackage._class(StringUtil.transformToJavaClassName(spName));
        resultSetClass.annotate(at.rseiler.spbee.core.annotation.ResultSet.class);
        JMethod constructor = resultSetClass.constructor(JMod.PUBLIC);

        int i = 1;
        for (ColumnDataList columnDataList : columnDataLists) {
            String className = StringUtil.transformToJavaClassName(spName) + "ResultSet" + i;
            String variableName = StringUtil.firstCharToLowerCase(className);
            JClass aClass = columnDataList.hasSeveralRows() ?
                    model.ref(List.class.getCanonicalName()).narrow(model.ref(packageName + ".entity." + className)) :
                    model.ref(packageName + ".entity." + className);
            JFieldVar field = resultSetClass.field(JMod.PRIVATE, aClass, variableName);
            JVar param = constructor.param(aClass, variableName);
            constructor.body().assign(JExpr._this().ref(field), param);
            i++;
        }

        System.out.println(getJavaCode(model));
    }

    private void generateEntityClass(String spName, List<ColumnDataList> columnDataLists) throws JClassAlreadyExistsException, IOException {
        int i = 1;
        for (ColumnDataList columnDataList : columnDataLists) {
            JCodeModel model = new JCodeModel();
            JPackage jPackage = model._package(packageName);
            JDefinedClass resultSetClass = jPackage._class(StringUtil.transformToJavaClassName(spName) + "ResultSet" + i);
            resultSetClass.annotate(at.rseiler.spbee.core.annotation.Entity.class);
            JMethod constructor = resultSetClass.constructor(JMod.PUBLIC);

            for (ColumnData columnData : columnDataList.getColumnDataList()) {
                String variableName = StringUtil.transformToJavaVariableName(columnData.getName());
                JType fieldClass = getFieldClass(model, columnData.getType());
                JFieldVar field = resultSetClass.field(JMod.PRIVATE, fieldClass, variableName);
                JVar param = constructor.param(fieldClass, variableName);
                constructor.body().assign(JExpr._this().ref(field), param);
            }

            i++;
            System.out.println("\n--------------------------------------------------------------------------------\n");
            System.out.println(getJavaCode(model));
        }

    }

    private JType getFieldClass(JCodeModel model, int type) {
        String qualifiedClassName;
        switch (type) {
            case Types.BOOLEAN:
                qualifiedClassName = boolean.class.getCanonicalName();
                break;
            case Types.CHAR:
                qualifiedClassName = char.class.getCanonicalName();
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                qualifiedClassName = int.class.getCanonicalName();
                break;
            case Types.BIGINT:
                qualifiedClassName = long.class.getCanonicalName();
                break;
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
                qualifiedClassName = double.class.getCanonicalName();
                break;
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
                qualifiedClassName = String.class.getCanonicalName();
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                qualifiedClassName = java.util.Date.class.getCanonicalName();
                break;
            default:
                qualifiedClassName = "java.lang.Object";
                break;
        }
        return model.ref(qualifiedClassName);
    }

    private String getJavaCode(JCodeModel model) throws IOException {
        StringCodeWriter codeWriter = new StringCodeWriter();
        model.build(codeWriter);
        return codeWriter.getJavaCode();
    }

    private static class ColumnDataList {
        private boolean hasSeveralRows;
        private List<ColumnData> columnDataList;

        public ColumnDataList(boolean hasSeveralRows, List<ColumnData> columnDataList) {
            this.hasSeveralRows = hasSeveralRows;
            this.columnDataList = columnDataList;
        }

        public boolean hasSeveralRows() {
            return hasSeveralRows;
        }

        public List<ColumnData> getColumnDataList() {
            return columnDataList;
        }

        @Override
        public String toString() {
            return "ColumnDataList{" +
                    "hasSeveralRows=" + hasSeveralRows +
                    ", columnDataList=" + columnDataList +
                    '}';
        }
    }

    private static class ColumnData {
        private int type;
        private String name;

        public ColumnData(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "ColumnData{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

}
