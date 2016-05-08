package at.rseiler.spbee.generator;

import com.sun.codemodel.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModelGenerator {

    private final DataSource dataSource;
    private final String packageName;
    private final String spName;
    private final String spArgs;

    public ModelGenerator(DataSource dataSource, String packageName, String spName, String spArgs) {
        this.dataSource = dataSource;
        this.packageName = packageName;
        this.spName = spName;
        this.spArgs = spArgs;
    }

    public List<ClassData> execute() {
        try (Connection connection = dataSource.getConnection(); CallableStatement statement = connection.prepareCall("{call " + spName + " (" + spArgs + ")}")) {
            List<ColumnDataList> columnDataLists = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery()) {
                do {
                    try (ResultSet rs = statement.getResultSet()) {

                        if (rs != null) {
                            if (rs.next()) {
                                handleResultSetMetaData(columnDataLists, rs);
                            }
                        } else if (resultSet.next()) {
                            handleResultSetMetaData(columnDataLists, resultSet);
                        }
                    }
                } while (statement.getMoreResults());
            }

            return generateSpBeeModelClasses(spName, columnDataLists);
        } catch (SQLException | JClassAlreadyExistsException | IOException e) {
            throw new RuntimeException("Error", e);
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

    private List<ClassData> generateSpBeeModelClasses(String spName, List<ColumnDataList> columnDataLists) throws JClassAlreadyExistsException, IOException {
        List<ClassData> result = new ArrayList<>();

        if (columnDataLists.size() > 1) {
            result.add(generateResultSetClass(spName, columnDataLists));
        }

        result.addAll(generateEntityClass(spName, columnDataLists));
        return result;
    }

    private ClassData generateResultSetClass(String spName, List<ColumnDataList> columnDataLists) throws JClassAlreadyExistsException, IOException {
        JCodeModel model = new JCodeModel();
        String resultPackageName = packageName + ".resultset";
        JPackage jPackage = model._package(resultPackageName);
        String resultClassName = StringUtil.transformToJavaClassName(spName);
        JDefinedClass resultSetClass = jPackage._class(resultClassName);
        resultSetClass.annotate(at.rseiler.spbee.core.annotation.ResultSet.class);
        JMethod constructor = resultSetClass.constructor(JMod.PUBLIC);

        for (int i = 0; i < columnDataLists.size(); i++) {
            ColumnDataList columnDataList = columnDataLists.get(i);
            String entityClassName = resultClassName + (columnDataLists.size() > 1 ? "ResultSet" + (i + 1) : "");
            String variableName = StringUtil.firstCharToLowerCase(entityClassName);
            JClass aClass = columnDataList.hasSeveralRows() ?
                    model.ref(List.class.getCanonicalName()).narrow(model.ref(packageName + ".entity." + entityClassName)) :
                    model.ref(packageName + ".entity." + entityClassName);
            JFieldVar field = resultSetClass.field(JMod.PRIVATE, aClass, variableName);
            JVar param = constructor.param(aClass, variableName);
            constructor.body().assign(JExpr._this().ref(field), param);
        }

        return new ClassData(resultClassName, resultPackageName, getJavaCode(model));
    }

    private List<ClassData> generateEntityClass(String spName, List<ColumnDataList> columnDataLists) throws JClassAlreadyExistsException, IOException {
        List<ClassData> result = new ArrayList<>();

        for (int i = 0; i < columnDataLists.size(); i++) {
            ColumnDataList columnDataList = columnDataLists.get(i);
            JCodeModel model = new JCodeModel();
            String entityPackageName = packageName + ".entity";
            JPackage jPackage = model._package(entityPackageName);
            String entityClassName = StringUtil.transformToJavaClassName(spName) + (columnDataLists.size() > 1 ? "ResultSet" + (i + 1) : "");
            JDefinedClass resultSetClass = jPackage._class(entityClassName);
            resultSetClass.annotate(at.rseiler.spbee.core.annotation.Entity.class);
            JMethod constructor = resultSetClass.constructor(JMod.PUBLIC);

            for (ColumnData columnData : columnDataList.getColumnDataList()) {
                String variableName = StringUtil.transformToJavaVariableName(columnData.getName());
                JType fieldClass = getFieldClass(model, columnData.getType());
                JFieldVar field = resultSetClass.field(JMod.PRIVATE, fieldClass, variableName);
                JVar param = constructor.param(fieldClass, variableName);
                constructor.body().assign(JExpr._this().ref(field), param);
            }

            result.add(new ClassData(entityClassName, entityPackageName, getJavaCode(model)));
        }

        return result;
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

        ColumnDataList(boolean hasSeveralRows, List<ColumnData> columnDataList) {
            this.hasSeveralRows = hasSeveralRows;
            this.columnDataList = columnDataList;
        }

        boolean hasSeveralRows() {
            return hasSeveralRows;
        }

        List<ColumnData> getColumnDataList() {
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

        ColumnData(int type, String name) {
            this.type = type;
            this.name = name;
        }

        int getType() {
            return type;
        }

        String getName() {
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
