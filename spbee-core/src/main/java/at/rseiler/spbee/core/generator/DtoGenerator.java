package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.exception.MultipleObjectsReturned;
import at.rseiler.spbee.core.exception.ObjectDoesNotExist;
import at.rseiler.spbee.core.pojo.*;
import at.rseiler.spbee.core.util.CodeModelUtil;
import at.rseiler.spbee.core.util.StringUtil;
import com.sun.codemodel.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

/**
 * Generator for the DTO classes.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class DtoGenerator extends AbstractGenerator {

    private static final String SPRING_ANNOTATION_AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
    private static final String SPRING_ANNOTATION_SERVICE = "org.springframework.stereotype.Service";

    private final Properties config;
    private final Map<String, ResultSetClass> resultSetMap;

    public DtoGenerator(ProcessingEnvironment processingEnv, Properties config, Map<String, ResultSetClass> resultSetMap) {
        super(processingEnv);
        this.config = config;
        this.resultSetMap = resultSetMap;
    }

    /**
     * Generates the DTO classes.
     *
     * @param dtoClasses the DTO classes to generate
     * @throws JClassAlreadyExistsException if a class is defined twice
     * @throws IOException                  if the generated class can't be written
     */
    public void generateDtoClasses(List<DtoClass> dtoClasses) throws JClassAlreadyExistsException, IOException {
        for (DtoClass dtoClass : dtoClasses) {
            DtoClassGeneratorInstance dtoClassGeneratorInstance = new DtoClassGeneratorInstance(config, dtoClass, resultSetMap)
                    .createClass()
                    .createConstructor()
                    .addStoredProcedureMethods();

            generateClass(dtoClassGeneratorInstance.getModel(), dtoClass.getQualifiedClassName());
        }
    }

    /**
     * Holds all information to generate one specific DTO class and generates the necessary code.
     */
    private static class DtoClassGeneratorInstance {

        private final Properties config;
        private final DtoClass dtoClass;
        private final Map<String, ResultSetClass> resultSetMap;
        private final JCodeModel model = new JCodeModel();
        private final Map<String, JFieldVar> spFields = new HashMap<>();
        private JDefinedClass dtoJClass;
        private JMethod constructor;
        private JVar dataSource;

        DtoClassGeneratorInstance(Properties config, DtoClass dtoClass, Map<String, ResultSetClass> resultSetMap) {
            this.config = config;
            this.dtoClass = dtoClass;
            this.resultSetMap = resultSetMap;
        }

        public JCodeModel getModel() {
            return model;
        }

        /**
         * Generates:
         * <pre>
         * {@literal @Service} public class *DaoImpl extends *Dao
         * {@literal @Service} public class *DaoImpl implements *Dao
         * </pre>
         */
        DtoClassGeneratorInstance createClass() throws JClassAlreadyExistsException {
            JPackage dtoJPackage = model._package(dtoClass.getPackage());
            dtoJClass = dtoJPackage._class(dtoClass.getSimpleClassName());
            CodeModelUtil.annotateGenerated(dtoJClass);
            dtoJClass.annotate(model.ref(SPRING_ANNOTATION_SERVICE));
            addSuperClassOrInterface();
            return this;
        }

        /**
         * Generates:
         * <pre>
         * {@literal @Autowired} public * (DataSource dataSource)
         * </pre>
         */
        DtoClassGeneratorInstance createConstructor() {
            constructor = dtoJClass.constructor(JMod.PUBLIC);
            constructor.annotate(model.ref(SPRING_ANNOTATION_AUTOWIRED));
            dataSource = constructor.param(DataSource.class, "dataSource");

            if (dtoClass.hasDataSourceConstructor()) {
                constructor.body().add(JExpr.invoke("super").arg(dataSource));
            }

            return this;
        }

        /**
         * Adds the stored procedure to the constructor (to get it autowired into the class) and creates the actual
         * method to call the stored procedure.
         */
        DtoClassGeneratorInstance addStoredProcedureMethods() throws JClassAlreadyExistsException {
            for (StoredProcedureMethod storedProcedureMethod : dtoClass.getStoredProcedureMethods()) {
                String fieldName = storedProcedureMethod.getDtoFieldName();

                if (!spFields.containsKey(fieldName)) {
                    spFields.put(fieldName, addConstructorField(storedProcedureMethod));
                }

                addDtoMethod(storedProcedureMethod, spFields.get(fieldName));
            }
            return this;
        }

        /**
         * Generates:
         * <pre>
         * implements *Dao
         * extends *Dao
         * </pre>
         */
        private void addSuperClassOrInterface() {
            if (dtoClass.isAnInterface()) {
                dtoJClass._implements(model.ref(dtoClass.getSuperQualifiedClassName()));
            } else {
                dtoJClass._extends(model.ref(dtoClass.getSuperQualifiedClassName()));
            }
        }

        /**
         * Generates:
         * Field variable:
         * <pre>
         * [ private final {SP_NAME_CLASS} {SP_NAME_VARIABLE} ]+;
         * <pre>
         * Constructor assignment:
         * <pre>
         * [ {SP_NAME_VARIABLE} = new {SP_NAME_CLASS}(dataSource) ]+
         * </pre>
         */
        private JFieldVar addConstructorField(StoredProcedureMethod storedProcedureMethod) {
            if (constructor == null) {
                throw new RuntimeException("Invalid Usage. createConstructor() must be called first.");
            }

            JClass jClass = model.ref(storedProcedureMethod.getQualifiedClassName());
            JFieldVar field = dtoJClass.field(JMod.PRIVATE | JMod.FINAL, jClass, storedProcedureMethod.getDtoFieldName());
            constructor.body().assign(field, JExpr._new(jClass).arg(dataSource));
            return field;
        }

        /**
         * Generates:
         * <pre>
         * public {DTO_METHOD_RETURN_TYPE} {DTO_METHOD_NAME}({DTO_METHOD_PARAMETERS}) {
         *      return (({DTO_METHOD_RETURN_TYPE}) {SP_NAME_VARIABLE}.execute({DTO_METHOD_PARAMETERS}).get("#result-set-0"));
         * }
         * </pre>
         * If it's a ResultSet:
         * <pre>
         * public {DTO_METHOD_RETURN_TYPE} {DTO_METHOD_NAME}({DTO_METHOD_PARAMETERS}) {
         *      Map<String, Object> map = {SP_NAME_VARIABLE}.execute({DTO_METHOD_PARAMETERS});
         *      return new {DTO_METHOD_RETURN_TYPE}((*)map.get("#result-set-0"), (*)map.get("#result-set-*"), ...);
         * }
         * </pre>
         */
        private void addDtoMethod(StoredProcedureMethod storedProcedureMethod, JFieldVar field) throws JClassAlreadyExistsException {
            JClass returnClass = getReturnClass(storedProcedureMethod);
            JMethod method = dtoJClass.method(JMod.PUBLIC, returnClass, storedProcedureMethod.getMethodName());
            addAnnotations(storedProcedureMethod, method);
            JInvocation execute = getExecute(storedProcedureMethod, field, method);
            JVar interceptorIdObject = null;

            if (config.containsKey("interceptor")) {
                JInvocation before = model.ref(config.getProperty("interceptor")).staticInvoke("before");
                before.arg(JExpr.lit(storedProcedureMethod.getStoredProcedureName()));

                for (JExpression arg : execute.listArgs()) {
                    before.arg(arg);
                }

                interceptorIdObject = method.body().decl(model.ref(Object.class.getCanonicalName()), "interceptorIdObject");
                method.body().assign(interceptorIdObject, before);
            }

            if ("void".equals(storedProcedureMethod.getReturnTypeInfo().getType())) {
                method.body().add(execute);
                addInterceptorCallAfter(storedProcedureMethod, method, execute, interceptorIdObject);
            } else if (resultSetMap.containsKey(storedProcedureMethod.getReturnTypeInfo().getType())) {
                multipleResultSets(storedProcedureMethod, returnClass, method, execute, interceptorIdObject);
            } else {
                singleResultSet(storedProcedureMethod, returnClass, method, execute, interceptorIdObject);
            }
        }

        /**
         * Generates:
         * <pre>
         * {FIELD}.execute( [ * ]* )
         * </pre>
         */
        private JInvocation getExecute(StoredProcedureMethod storedProcedureMethod, JFieldVar field, JMethod method) {
            JInvocation execute = JExpr.invoke(field, "execute");

            for (Variable variable : storedProcedureMethod.getArguments()) {
                JVar param = method.param(model.ref(variable.getTypeInfo().asString()), variable.getName());
                execute.arg(param);
            }

            return execute;
        }

        /**
         * Generates the class for the return type.
         */
        private JClass getReturnClass(StoredProcedureMethod storedProcedureMethod) {
            Optional<String> genericType = storedProcedureMethod.getReturnTypeInfo().getGenericType();
            JClass returnClass = model.ref(storedProcedureMethod.getReturnTypeInfo().getType());

            if (genericType.isPresent()) {
                returnClass = returnClass.narrow(model.ref(genericType.get()));
            }

            return returnClass;
        }

        /**
         * Adds the annotations to the method.
         */
        private void addAnnotations(StoredProcedureMethod storedProcedureMethod, JMethod method) throws JClassAlreadyExistsException {
            for (AnnotationInfo annotationInfo : storedProcedureMethod.getAnnotations()) {
                JAnnotationUse jAnnotationUse = method.annotate(model.ref(annotationInfo.getAnnotationType()));

                for (AnnotationValueInfo annotationValueInfo : annotationInfo.getAnnotationValueInfos()) {
                    addAnnotationParam(jAnnotationUse, annotationValueInfo);
                }
            }
        }

        /**
         * Generates the body of the DTO method for methods with several result-sets.
         * <p>
         * Example:
         * <pre>
         * Object interceptorIdObject; // optional
         * interceptorIdObject = *SpInterceptor.before("*"); // optional
         * Map<String, Object> map;
         * map = sp*.execute(id);
         * *SpInterceptor.after(interceptorIdObject, "*"); // optional
         * List<*> list0;
         * list0 = ((*> ) map.get("#result-set-0"));
         * * obj0;
         * if (list0 .size() == 1) {
         *     obj0 = list0.get(0);
         * } else {
         *     if (list0.size() == 0) {
         *         throw new ObjectDoesNotExist();
         *     } else {
         *         throw new MultipleObjectsReturned();
         *     }
         * }
         * List<*> list1;
         * list1 = ((<*> ) map.get("#result-set-1"));
         * return new *ResultSet(obj0, list1);
         * </pre>
         * <p>
         * Example if the entity in the ResultSet is annotated with @ReturnNull:
         * <pre>
         * Object interceptorIdObject; // optional
         * interceptorIdObject = *SpInterceptor.before("*"); // optional
         * Map<String, Object> map;
         * map = sp*.execute(id);
         * *SpInterceptor.after(interceptorIdObject, "*"); // optional
         * List<*> list0;
         * list0 = ((List<*>) map.get("#result-set-0"));
         * * obj0;
         * if (list0.size() == 1) {
         *     obj0 = list0.get(0);
         * } else {
         *     if (list0.size() == 0) {
         *         obj0 = null;
         *     } else {
         *         throw new MultipleObjectsReturned();
         *     }
         * }
         * List<*> list1;
         * list1 = ((List<*>) map.get("#result-set-1"));
         * return new *ResultSet(obj0, list1);
         * </pre>
         * <p>
         * Example if the entity in the ResultSet is an Optional:
         * <pre>
         * Object interceptorIdObject; // optional
         * interceptorIdObject = *SpInterceptor.before("*"); // optional
         * Map<String, Object> map;
         * map = sp*.execute(id);
         * *SpInterceptor.after(interceptorIdObject, "*"); // optional
         * List<*> list0;
         * list0 = ((List<*>) map.get("#result-set-0"));
         * Optional obj0;
         * if (list0.size() == 1) {
         *     obj0 = Optional.of(list0.get(0));
         * } else {
         *     if (list0.size() == 0) {
         *         obj0 = Optional.empty();
         *     } else {
         *         throw new MultipleObjectsReturned();
         *     }
         * }
         * List<*> list1;
         * list1 = ((List<*>) map.get("#result-set-1"));
         * return new *ResultSet(obj0, list1);
         * </pre>
         */
        private void multipleResultSets(StoredProcedureMethod storedProcedureMethod, JClass returnClass, JMethod method, JInvocation execute, JVar interceptorIdObject) {
            JVar map = method.body().decl(CodeModelUtil.getMapStringObject(model), "map");
            method.body().assign(map, execute);
            addInterceptorCallAfter(storedProcedureMethod, method, execute, interceptorIdObject);
            JInvocation resultSetsInvoke = JExpr._new(returnClass);

            List<JVar> args = new ArrayList<>();
            List<ResultSetVariable> variables = resultSetMap.get(storedProcedureMethod.getReturnTypeInfo().getType()).getResultSetVariables();

            for (int i = 0; i < variables.size(); i++) {
                ResultSetVariable variable = variables.get(i);
                Optional<String> genericType = variable.getTypeInfo().getGenericType();

                if (genericType.isPresent()) {
                    if (Optional.class.getCanonicalName().equals(variable.getTypeInfo().getType())) {
                        JClass varType = CodeModelUtil.getGenericList(model,  genericType.get());
                        JVar list = method.body().decl(varType, "list" + i);
                        method.body().assign(list, JExpr.cast(varType, map.invoke("get").arg("#result-set-" + i)));

                        JVar obj = method.body().decl(model.ref(variable.getTypeInfo().getType()), "obj" + i);
                        JConditional condition = method.body()._if(list.invoke("size").eq(JExpr.lit(1)));
                        condition._then().assign(obj, model.ref(Optional.class).staticInvoke("of").arg(list.invoke("get").arg(JExpr.lit(0))));

                        condition = condition._elseif(list.invoke("size").eq(JExpr.lit(0)));
                        condition._then().assign(obj, model.ref(Optional.class).staticInvoke("empty"));
                        condition._else()._throw(JExpr._new(model.ref(MultipleObjectsReturned.class)));

                        args.add(obj);
                    } else {
                        JClass varType = model.ref(variable.getTypeInfo().getType()).narrow(model.ref(genericType.get()));
                        JVar list = method.body().decl(varType, "list" + i);
                        method.body().assign(list, JExpr.cast(varType, map.invoke("get").arg("#result-set-" + i)));
                        args.add(list);
                    }
                } else {
                    JClass varType = CodeModelUtil.getGenericList(model, variable.getTypeInfo().getType());
                    JVar list = method.body().decl(varType, "list" + i);
                    method.body().assign(list, JExpr.cast(varType, map.invoke("get").arg("#result-set-" + i)));

                    JVar obj = method.body().decl(model.ref(variable.getTypeInfo().getType()), "obj" + i);
                    JConditional condition = method.body()._if(list.invoke("size").eq(JExpr.lit(1)));
                    condition._then().assign(obj, list.invoke("get").arg(JExpr.lit(0)));

                    if (variable.useNullInsteadOfAnException()) {
                        condition = condition._elseif(list.invoke("size").eq(JExpr.lit(0)));
                        condition._then().assign(obj, JExpr._null());
                        condition._else()._throw(JExpr._new(model.ref(MultipleObjectsReturned.class)));
                    } else {
                        condition = condition._elseif(list.invoke("size").eq(JExpr.lit(0)));
                        condition._then()._throw(JExpr._new(model.ref(ObjectDoesNotExist.class)));
                        condition._else()._throw(JExpr._new(model.ref(MultipleObjectsReturned.class)));
                    }

                    args.add(obj);
                }
            }

            args.forEach(resultSetsInvoke::arg);
            method.body()._return(resultSetsInvoke);
        }

        /**
         * Generates the body of the DTO method for methods with exactly one result-set.
         * <p>
         * If the method's return value is a list:
         * <pre>
         * Map<String, Object> map;
         * Object interceptorIdObject; // optional
         * interceptorIdObject = *SpInterceptor.before("*"); // optional
         * map = sp*.execute();
         * *SpInterceptor.after(interceptorIdObject, "*"); // optional
         * return ((List<*> ) map.get("#result-set-0"));
         * </pre>
         * <p>
         * If the method's return value is an entity:
         * <pre>
         * Object interceptorIdObject; // optional
         * interceptorIdObject = *SpInterceptor.before("*"); // optional
         * List<*> list;
         * list = ((List<*> ) sp*.execute( [ * ]* ).get("#result-set-0"));
         * *SpInterceptor.after(interceptorIdObject, "*"); // optional
         * if (list.size() == 1) {
         *     return list.get(0);
         * } else {
         *     if (list.size() == 0) {
         *         throw new ObjectDoesNotExist();
         *     } else {
         *         throw new MultipleObjectsReturned();
         *     }
         * }
         * </pre>
         * <p>
         * If the method's return value is an entity and the method is annotated with @ReturnNull:
         * <pre>
         * Object interceptorIdObject; // optional
         * interceptorIdObject = *SpInterceptor.before("*"); // optional
         * List<*> list;
         * list = ((List<*> ) sp*.execute( [ * ]* ).get("#result-set-0"));
         * *SpInterceptor.after(interceptorIdObject, "*"); // optional
         * if (list.size() == 1) {
         *     return list.get(0);
         * } else {
         *     if (list.size() == 0) {
         *         return null;
         *     } else {
         *         throw new MultipleObjectsReturned();
         *     }
         * }
         * </pre>
         * <p>
         * If the method's return value is an Optional of an entity:
         * <pre>
         * Object interceptorIdObject; // optional
         * interceptorIdObject = *SpInterceptor.before("*"); // optional
         * List<*> list;
         * list = ((List<*> ) sp*.execute( [ * ]* ).get("#result-set-0"));
         * *SpInterceptor.after(interceptorIdObject, "*"); // optional
         * if (list.size() == 1) {
         *     return Optional.of(list.get(0));
         * } else {
         *     if (list.size() == 0) {
         *         return Optional.empty();
         *     } else {
         *         throw new MultipleObjectsReturned();
         *     }
         * }
         * </pre>
         */
        private void singleResultSet(StoredProcedureMethod storedProcedureMethod, JClass returnClass, JMethod method, JInvocation execute, JVar interceptorIdObject) {
            Optional<String> genericType = storedProcedureMethod.getReturnTypeInfo().getGenericType();

            if (genericType.isPresent()) {
                if (Optional.class.getCanonicalName().equals(storedProcedureMethod.getReturnTypeInfo().getType())) {
                    String genericClassType = genericType.get();
                    JVar list = method.body().decl(CodeModelUtil.getGenericList(model, genericClassType), "list");
                    method.body().assign(list, JExpr.cast(model.ref(List.class).narrow(model.ref(genericClassType)), execute.invoke("get").arg("#result-set-0")));
                    addInterceptorCallAfter(storedProcedureMethod, method, execute, interceptorIdObject);
                    JConditional condition = method.body()._if(list.invoke("size").eq(JExpr.lit(1)));
                    condition._then()._return(model.ref(Optional.class.getCanonicalName()).staticInvoke("of").arg(list.invoke("get").arg(JExpr.lit(0))));
                    condition = condition._elseif(list.invoke("size").eq(JExpr.lit(0)));
                    condition._then()._return(model.ref(Optional.class.getCanonicalName()).staticInvoke("empty"));
                    condition._else()._throw(JExpr._new(model.ref(MultipleObjectsReturned.class.getCanonicalName())));
                } else {
                    JVar map = method.body().decl(CodeModelUtil.getMapStringObject(model), "map");
                    method.body().assign(map, execute);
                    addInterceptorCallAfter(storedProcedureMethod, method, execute, interceptorIdObject);
                    method.body()._return(JExpr.cast(returnClass, map.invoke("get").arg("#result-set-0")));
                }
            } else {
                JVar list = method.body().decl(CodeModelUtil.getGenericList(model, storedProcedureMethod.getReturnTypeInfo().getType()), "list");
                method.body().assign(list, JExpr.cast(model.ref(List.class).narrow(returnClass), execute.invoke("get").arg("#result-set-0")));
                addInterceptorCallAfter(storedProcedureMethod, method, execute, interceptorIdObject);
                JConditional condition = method.body()._if(list.invoke("size").eq(JExpr.lit(1)));
                condition._then()._return(list.invoke("get").arg(JExpr.lit(0)));

                if (storedProcedureMethod.useNullInsteadOfAnException()) {
                    condition = condition._elseif(list.invoke("size").eq(JExpr.lit(0)));
                    condition._then()._return(JExpr._null());
                    condition._else()._throw(JExpr._new(model.ref(MultipleObjectsReturned.class)));
                } else {
                    condition = condition._elseif(list.invoke("size").eq(JExpr.lit(0)));
                    condition._then()._throw(JExpr._new(model.ref(ObjectDoesNotExist.class)));
                    condition._else()._throw(JExpr._new(model.ref(MultipleObjectsReturned.class)));
                }
            }
        }

        /**
         * Adds the interceptor call of the after method.
         */
        private void addInterceptorCallAfter(StoredProcedureMethod storedProcedureMethod, JMethod method, JInvocation execute, JVar interceptorIdObject) {
            if(interceptorIdObject != null) {
                JInvocation after = model.ref(config.getProperty("interceptor")).staticInvoke("after");
                after.arg(interceptorIdObject);
                after.arg(JExpr.lit(storedProcedureMethod.getStoredProcedureName()));

                for (JExpression arg : execute.listArgs()) {
                    after.arg(arg);
                }

                method.body().add(after);
            }
        }

        private void addAnnotationParam(JAnnotationUse jAnnotationUse, AnnotationValueInfo annotationValueInfo) throws JClassAlreadyExistsException {
            Object value = annotationValueInfo.getValue();
            String annotationName = annotationValueInfo.getName();

            switch (annotationValueInfo.getKind()) {
                case BASIC:
                    if (value instanceof Boolean) {
                        jAnnotationUse.param(annotationName, (Boolean) value);
                    } else if (value instanceof Byte) {
                        jAnnotationUse.param(annotationName, (Byte) value);
                    } else if (value instanceof Character) {
                        jAnnotationUse.param(annotationName, (Character) value);
                    } else if (value instanceof Double) {
                        jAnnotationUse.param(annotationName, (Double) value);
                    } else if (value instanceof Float) {
                        jAnnotationUse.param(annotationName, (Float) value);
                    } else if (value instanceof Long) {
                        jAnnotationUse.param(annotationName, (Long) value);
                    } else if (value instanceof Short) {
                        jAnnotationUse.param(annotationName, (Short) value);
                    } else if (value instanceof Integer) {
                        jAnnotationUse.param(annotationName, (Integer) value);
                    } else if (value instanceof String) {
                        jAnnotationUse.param(annotationName, (String) value);
                    }
                    break;
                case DECLARED_TYPE:
                    jAnnotationUse.param(annotationName, model.ref(value.toString()));
                    break;
                case ELEMENT:
                    String qualifiedEnumName = StringUtil.getPackage(annotationValueInfo.getType());
                    JPackage dtoJPackage = new JCodeModel()._package(StringUtil.getPackage(qualifiedEnumName));
                    JDefinedClass definedClass = dtoJPackage._class(StringUtil.getSimpleClassName(qualifiedEnumName));
                    jAnnotationUse.param(annotationName, definedClass.enumConstant(value.toString()));
                    break;
                case LIST:
                    JAnnotationArrayMember jAnnotationArrayMember = jAnnotationUse.paramArray(annotationName);
                    List<AnnotationValueInfo> list = (List<AnnotationValueInfo>) value;

                    for (AnnotationValueInfo valueInfo : list) {
                        addAnnotationArrayMemberParam(jAnnotationArrayMember, valueInfo);
                    }
                    break;
                default:
                    throw new RuntimeException("Failed to addAnnotationParam, because the kind is unknown: " + annotationValueInfo.getKind());
            }
        }

        private void addAnnotationArrayMemberParam(JAnnotationArrayMember jAnnotationArrayMember, AnnotationValueInfo annotationValueInfo) {
            Object value = annotationValueInfo.getValue();

            try {
                switch (annotationValueInfo.getKind()) {
                    case BASIC:
                        if (value instanceof Boolean) {
                            jAnnotationArrayMember.param((Boolean) value);
                        } else if (value instanceof Byte) {
                            jAnnotationArrayMember.param((Byte) value);
                        } else if (value instanceof Character) {
                            jAnnotationArrayMember.param((Character) value);
                        } else if (value instanceof Double) {
                            jAnnotationArrayMember.param((Double) value);
                        } else if (value instanceof Float) {
                            jAnnotationArrayMember.param((Float) value);
                        } else if (value instanceof Long) {
                            jAnnotationArrayMember.param((Long) value);
                        } else if (value instanceof Short) {
                            jAnnotationArrayMember.param((Short) value);
                        } else if (value instanceof Integer) {
                            jAnnotationArrayMember.param((Integer) value);
                        } else if (value instanceof String) {
                            jAnnotationArrayMember.param((String) value);
                        }
                        break;
                    case DECLARED_TYPE:
                        jAnnotationArrayMember.param(model.ref(value.toString()));
                        break;
                    case ELEMENT:
                        String qualifiedEnumName = StringUtil.getPackage(value.toString());
                        JPackage dtoJPackage = model._package(StringUtil.getPackage(qualifiedEnumName));
                        JDefinedClass definedClass = dtoJPackage._class(StringUtil.getSimpleClassName(qualifiedEnumName));
                        jAnnotationArrayMember.param(definedClass.enumConstant(value.toString()));
                        break;
                    default:
                        throw new RuntimeException("Failed to addAnnotationParam, because the kind is unknown: " + annotationValueInfo.getKind());
                }
            } catch (JClassAlreadyExistsException e) {
                throw new RuntimeException("JClassAlreadyExistsException", e);
            }
        }

    }
}
