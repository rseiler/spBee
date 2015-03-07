package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.pojo.MapperClass;
import at.rseiler.spbee.core.pojo.Variable;
import at.rseiler.spbee.core.util.CodeModelUtil;
import at.rseiler.spbee.core.util.StringUtil;
import com.sun.codemodel.*;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Generator for the RowMapper classes.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class MapperGenerator extends AbstractGenerator {

    private static final String SPRING_ROW_MAPPER = "org.springframework.jdbc.core.RowMapper";

    public MapperGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public void generateMappers(Collection<MapperClass> mapperClasses) throws JClassAlreadyExistsException, ClassNotFoundException, IOException {
        for (MapperClass mapperClass : mapperClasses) {
            generateMapper(mapperClass);
        }
    }

    /**
     * Generates a MapperClass.
     * <p>
     * Example:
     * <pre>
     * public class *Mapper implements RowMapper<*> {
     *
     *      public T mapRow(ResultSet rs, int rowNum) throws SQLException {
     *          return new T( [rs.*(1)]+ );
     *      }
     *
     * }
     * </pre>
     *
     * @param mapperClass the mapper class
     * @throws JClassAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void generateMapper(MapperClass mapperClass) throws JClassAlreadyExistsException, ClassNotFoundException, IOException {
        String type = mapperClass.getType();
        String qualifiedClassName = mapperClass.getQualifiedMapperClassName();

        JCodeModel model = new JCodeModel();
        JClass typeJClass = model.directClass(type);
        JDefinedClass mapperJDefinedClass = createClass(model, qualifiedClassName, typeJClass);
        createMapRow(model, mapperJDefinedClass, typeJClass, mapperClass.getVariables());

        generateClass(model, qualifiedClassName);
    }

    /**
     * Generates:
     * <pre>
     * class *Mapper implements RowMapper<*> [className, typeJClass]
     * </pre>
     */
    private JDefinedClass createClass(JCodeModel model, String qualifiedClassName, JClass typeJClass) throws JClassAlreadyExistsException {
        String className = StringUtil.getSimpleClassName(qualifiedClassName);
        String aPackage = StringUtil.getPackage(qualifiedClassName);

        JPackage jPackage = model._package(aPackage);
        JDefinedClass aClass = jPackage._class(className);
        CodeModelUtil.annotateGenerated(aClass);
        aClass._implements(model.directClass(SPRING_ROW_MAPPER).narrow(typeJClass));

        return aClass;
    }

    /**
     * Generates:
     * <pre>
     * public * mapRow(ResultSet rs, int rowNum) throws SQLException {
     *     return new * ( [ rs.get*(*) ]* );
     * }
     * </pre>
     */
    private void createMapRow(JCodeModel model, JDefinedClass mapperClass, JClass type, List<Variable> mapperVariables) throws ClassNotFoundException {
        JMethod method = mapperClass.method(JMod.PUBLIC, type, "mapRow");
        method._throws(SQLException.class);
        method.param(model.directClass(ResultSet.class.getCanonicalName()), "rs");
        method.param(model.parseType("int"), "rowNum");
        JInvocation invocation = JExpr._new(type);

        for (int i = 0; i < mapperVariables.size(); i++) {
            Variable variable = mapperVariables.get(i);
            String methodName = getResultSetMethod(variable.getTypeInfo().getGenericTypeOrType());
            invocation.arg(JExpr.invoke(JExpr.ref("rs"), methodName).arg(JExpr.lit(i + 1)));
        }

        method.body()._return(invocation);
    }

    /**
     * Retrieves the most fitting {@link java.sql.ResultSet} method based on the type.
     *
     * @param type the type
     * @return the method name
     */
    private static String getResultSetMethod(String type) {
        switch (type) {
            case "boolean":
            case "java.lang.Boolean":
                return "getBoolean";
            case "byte":
            case "java.lang.Byte":
                return "getByte";
            case "short":
            case "java.lang.Short":
                return "getShort";
            case "int":
            case "java.lang.Integer":
                return "getInt";
            case "long":
            case "java.lang.Long":
                return "getLong";
            case "float":
            case "java.lang.Float":
                return "getFloat";
            case "double":
            case "java.lang.Double":
                return "getDouble";
            case "byte[]":
            case "java.lang.Byte[]":
                return "getByte";
            case "java.lang.String":
                return "getString";
            case "java.sql.Date":
            case "java.util.Date":
                return "getDate";
            case "java.math.BigDecimal":
                return "getBigDecimal";
            default:
                return "getObject";
        }
    }

}
