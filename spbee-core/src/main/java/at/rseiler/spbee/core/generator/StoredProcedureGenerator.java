package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.pojo.*;
import at.rseiler.spbee.core.util.CodeModelUtil;
import com.sun.codemodel.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generator for the StoredProcedure classes.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class StoredProcedureGenerator extends AbstractGenerator {

    private static final String SPRING_SQL_RETURN_RESULT_SET = "org.springframework.jdbc.core.SqlReturnResultSet";
    private static final String SPRING_STORED_PROCEDURE = "org.springframework.jdbc.object.StoredProcedure";
    private static final String SPRING_SQL_PARAMETER = "org.springframework.jdbc.core.SqlParameter";

    private final Map<String, ResultSetClass> resultSetsMap;

    public StoredProcedureGenerator(ProcessingEnvironment processingEnv, Map<String, ResultSetClass> resultSetsMap) {
        super(processingEnv);
        this.resultSetsMap = resultSetsMap;
    }

    public void generateStoredProcedureClasses(List<DtoClass> dtoClasses) throws JClassAlreadyExistsException, IOException {
        Set<String> storedProcedureNames = new HashSet<>();

        for (DtoClass dtoClass : dtoClasses) {
            for (StoredProcedureMethod storedProcedureMethod : dtoClass.getStoredProcedureMethods()) {
                // checks if the stored procedure already exists
                if (!storedProcedureNames.contains(storedProcedureMethod.getQualifiedClassName())) {
                    generateStoredProcedure(storedProcedureMethod);
                    storedProcedureNames.add(storedProcedureMethod.getQualifiedClassName());
                }
            }
        }
    }

    /**
     * Example:
     * <p>
     * <pre>
     * public class * extends StoredProcedure {
     *
     *     public *(DataSource dataSource) {
     *         super(dataSource, "*");
     *         [ declareParameter(new SqlParameter("*", Types.*)); ]*
     *         [ declareParameter(new SqlReturnResultSet("#result-set-*", new *Mapper())); ]*
     *         compile();
     *     }
     *
     *     public Map&lt;String, Object&gt; execute([ * * ]*) {
     *         return super.execute([ * ]*);
     *     }
     *
     * }
     * </pre>
     */
    private void generateStoredProcedure(StoredProcedureMethod storedProcedureMethod) throws JClassAlreadyExistsException, IOException {
        JCodeModel model = new JCodeModel();
        JDefinedClass spClass = createClass(model, storedProcedureMethod.getPackage(), storedProcedureMethod.getSimpleClassName());
        createConstructor(model, spClass, storedProcedureMethod);
        addExecuteMethod(model, spClass, storedProcedureMethod);

        generateClass(model, storedProcedureMethod.getQualifiedClassName());
    }

    /**
     * Generates:
     * <pre>
     * public class * extends StoredProcedure {className}
     * </pre>
     */
    private JDefinedClass createClass(JCodeModel model, String spPackage, String className) throws JClassAlreadyExistsException {
        JPackage jPackage = model._package(spPackage);
        JDefinedClass aClass = jPackage._class(className);
        CodeModelUtil.annotateGenerated(aClass);
        aClass._extends(model.ref(SPRING_STORED_PROCEDURE));
        return aClass;
    }

    /**
     * Generates:
     * <pre>
     * public {SP_CLASS_NAME}(DataSource dataSource) {
     *      super(dataSource, {SP_NAME});
     *      [ declareParameter(new SqlParameter({PARAMETER_NAME}, Types.*)); ]*
     *      [ declareParameter(new SqlReturnResultSet("#result-set-*", new *Mapper())); ]*
     *      compile();
     * }
     * </pre>
     */
    private void createConstructor(JCodeModel model, JDefinedClass spClass, StoredProcedureMethod storedProcedureMethod) {
        JMethod constructor = spClass.constructor(JMod.PUBLIC);
        JVar dataSource = constructor.param(DataSource.class, "dataSource");
        JBlock body = constructor.body();
        body.add(JExpr.invoke("super").arg(dataSource).arg(storedProcedureMethod.getStoredProcedureName()));
        declareSqlParameters(model, storedProcedureMethod, body);
        declareResultSets(model, body, storedProcedureMethod);
        body.add(JExpr.invoke("compile"));
    }

    /**
     * Generates:
     * <pre>
     * [ declareParameter(new SqlParameter({PARAMETER_NAME}, Types.*)); ]*
     * </pre>
     */
    private void declareSqlParameters(JCodeModel model, StoredProcedureMethod storedProcedureMethod, JBlock body) {
        for (Variable variable : storedProcedureMethod.getArguments()) {
            String sqlType = getSqlParameter(variable.getTypeInfo().getGenericTypeOrType());
            JInvocation sqlParameter = JExpr._new(model.ref(SPRING_SQL_PARAMETER));
            sqlParameter.arg(variable.getName());
            sqlParameter.arg(model.ref(Types.class.getCanonicalName()).staticRef(sqlType));
            body.add(JExpr.invoke("declareParameter").arg(sqlParameter));
        }
    }

    /**
     * Generates:
     * <pre>
     * [ declareParameter(new SqlReturnResultSet("#result-set-*", new *Mapper())); ]*
     * </pre>
     */
    private void declareResultSets(JCodeModel model, JBlock body, StoredProcedureMethod storedProcedureMethod) {
        String type = storedProcedureMethod.getReturnTypeInfo().getGenericType().orElse(storedProcedureMethod.getReturnTypeInfo().getType());

        if (!"void".equals(type)) {
            if (resultSetsMap.containsKey(type)) {
                multipleResultSets(model, body, type);
            } else {
                singleResultSet(model, body, storedProcedureMethod);
            }
        }
    }

    /**
     * If the type is a {@link at.rseiler.spbee.core.annotation.ResultSet} then multiple result-sets are declared.
     * <p>
     * Generates:
     * <pre>
     * [ declareParameter(new SqlReturnResultSet("#result-set-*", new *Mapper())); ]*
     * </pre>
     */
    private void multipleResultSets(JCodeModel model, JBlock body, String type) {
        List<ResultSetVariable> variables = resultSetsMap.get(type).getResultSetVariables();

        for (int i = 0; i < variables.size(); i++) {
            ResultSetVariable variable = variables.get(i);
            JInvocation newMapper = JExpr._new(model.ref(variable.getRowMapper()));
            JInvocation sqlReturnResultSet = JExpr._new(model.ref(SPRING_SQL_RETURN_RESULT_SET)).arg("#result-set-" + i).arg(newMapper);
            body.add(JExpr.invoke("declareParameter").arg(sqlReturnResultSet));
        }
    }

    /**
     * If it's a basic type then only one result-set is declared.
     * <p>
     * Generates:
     * <pre>
     * declareParameter(new SqlReturnResultSet("#result-set-0", new *Mapper()));
     * </pre>
     */
    private void singleResultSet(JCodeModel model, JBlock body, StoredProcedureMethod storedProcedureMethod) {
        JInvocation newMapper = JExpr._new(model.ref(storedProcedureMethod.getQualifiedRowMapperClass()));
        JInvocation sqlReturnResultSet = JExpr._new(model.ref(SPRING_SQL_RETURN_RESULT_SET)).arg("#result-set-0").arg(newMapper);
        body.add(JExpr.invoke("declareParameter").arg(sqlReturnResultSet));
    }

    /**
     * Generates:
     * <p>
     * <pre>
     * public Map<String, Object> execute( [ * ]* ) { return super.execute( [ * ]* ) }
     * </pre>
     */
    private void addExecuteMethod(JCodeModel model, JDefinedClass aClass, StoredProcedureMethod storedProcedureMethod) {
        JMethod method = aClass.method(JMod.PUBLIC, CodeModelUtil.getMapStringObject(model), "execute");
        JInvocation superExecute = JExpr._super().invoke("execute");
        JBlock block = method.body();
        boolean hasArrayType = storedProcedureMethod.getArguments().stream().anyMatch(this::isArrayType);
        JVar connection = null;

        if (hasArrayType) {
            connection = block.decl(model.ref(Connection.class.getCanonicalName()), "conn");
            block.assign(connection, JExpr._null());

            JTryBlock tryBlock = method.body()._try();
            // catch - rethrow exception
            JCatchBlock catchBlock = tryBlock._catch(model.ref(SQLException.class.getCanonicalName()));
            JInvocation exception = JExpr._new(model.ref("org.springframework.jdbc.UncategorizedSQLException"))
                    .arg(JExpr._this().invoke("getClass").invoke("getCanonicalName"))
                    .arg(JExpr._this().invoke("getSql"))
                    .arg(catchBlock.param("e"));
            catchBlock.body()._throw(exception);
            // finally - closes the connection
            JTryBlock connCloseTryBlock = tryBlock._finally()._if(connection.ne(JExpr._null()))._then()._try();
            connCloseTryBlock.body().add(connection.invoke("close"));
            JCatchBlock closeConnCatchBlock = connCloseTryBlock._catch(model.ref(SQLException.class.getCanonicalName()));
            JInvocation runtimeException = JExpr._new(model.ref(RuntimeException.class.getCanonicalName()))
                    .arg(JExpr.lit("Failed to close connection"))
                    .arg(closeConnCatchBlock.param("e"));
            closeConnCatchBlock.body()._throw(runtimeException);


            block = tryBlock.body();
            block.assign(connection, JExpr.invoke("getJdbcTemplate").invoke("getDataSource").invoke("getConnection"));
        }

        for (Variable variable : storedProcedureMethod.getArguments()) {
            JVar param = method.param(model.ref(variable.getTypeInfo().asString()), variable.getName());

            if (hasArrayType && isArrayType(variable)) {
                superExecute.arg(connection.invoke("createArrayOf").arg(getArrayType(variable.getTypeInfo().asString())).arg(param));
            } else {
                superExecute.arg(param);
            }
        }

        block._return(superExecute);
    }

    private boolean isArrayType(Variable variable) {
        return variable.getTypeInfo().asString().contains("[]");
    }

    /**
     * Retrieves the most fitting org.springframework.jdbc.core.SqlParameter type based on the type
     *
     * @param type the type
     * @return the parameter name
     */
    private static String getSqlParameter(String type) {
        switch (type) {
            case "boolean":
            case "java.lang.Boolean":
                return "BOOLEAN";
            case "byte":
            case "java.lang.Byte":
                return "TINYINT";
            case "short":
            case "java.lang.Short":
                return "SMALLINT";
            case "int":
            case "java.lang.Integer":
                return "INTEGER";
            case "long":
            case "java.lang.Long":
                return "BIGINT";
            case "float":
            case "java.lang.Float":
                return "FLOAT";
            case "double":
            case "java.lang.Double":
                return "DOUBLE";
            case "byte[]":
            case "java.lang.Byte[]":
                return "BINARY";
            case "java.lang.String":
                return "VARCHAR";
            case "java.sql.Date":
                return "DATE";
            case "java.sql.Timestamp":
                return "TIMESTAMP";
            case "java.sql.Time":
                return "TIME";
            case "java.math.BigDecimal":
                return "DOUBLE";
            default:
                if (type.contains("[]")) {
                    return "ARRAY";
                }

                throw new RuntimeException("Unknown SqlType: " + type);
        }
    }

    private String getArrayType(String type) {
        switch (type) {
            case "java.lang.Boolean[]":
                return "bool";
            case "java.lang.Character[]":
                return "char";
            case "java.lang.Byte[]":
                return "smallint";
            case "java.lang.Short[]":
                return "smallint";
            case "java.lang.Integer[]":
                return "int";
            case "java.lang.Long[]":
                return "bigint";
            case "java.lang.Float[]":
                return "float";
            case "java.lang.Decimal[]":
            case "java.math.BigDecimal[]":
                return "numeric";
            case "java.lang.String[]":
                return "varchar";
            case "java.sql.Timestamp":
                return "timestamp";
            case "java.sql.Time":
                return "time";
            case "java.sql.Date[]":
                return "date";
        }

        throw new RuntimeException("Unknown array type: " + type);
    }

}
