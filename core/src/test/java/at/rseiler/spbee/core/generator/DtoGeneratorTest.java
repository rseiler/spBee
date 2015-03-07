package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.pojo.*;
import at.rseiler.spbee.core.util.ElementConverter;
import com.sun.codemodel.JClassAlreadyExistsException;
import org.junit.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static at.rseiler.spbee.core.generator.GeneratorUtil.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DtoGeneratorTest {

    @Test
    public void testGenerateDtoClasses() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "package at.rseiler.spbee.test;",
                "import java.util.List;",
                "import javax.annotation.Generated;",
                "import javax.sql.DataSource;",
                "import at.rseiler.spbee.test.storedprocedure.SpGetSomethingWithSpecialMapper;",
                "import org.springframework.beans.factory.annotation.Autowired;",
                "import org.springframework.stereotype.Service;",
                "@Generated(value = \"at.rseiler.spbee.core.SPBeeAnnotationProcessor\", date =",
                "@Service",
                "public class DtoTestImpl",
                "implements DtoTest",
                "private final SpGetSomethingWithSpecialMapper spGetSomethingWithSpecialMapper;",
                "@Autowired",
                "public DtoTestImpl(DataSource dataSource)",
                "spGetSomethingWithSpecialMapper = new SpGetSomethingWithSpecialMapper(dataSource);",
                "public List<String> getSomething()",
                "Map<String, Object> map;",
                "map = spGetSomethingWithSpecialMapper.execute();",
                "return ((List<String> ) map.get(\"#result-set-0\"));"
                );
    }

    @Test
    public void testGenerateDtoClassesWithAbstractClass() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", false, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "extends DtoTest"
        );
    }

    @Test
    public void testGenerateDtoClassesWithEntityAsReturnValue() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .returnNull(null)
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "public java.lang.String getSomething()",
                "java.util.List<java.lang.String> list;",
                "list = ((java.util.List<java.lang.String> ) spGetSomethingWithSpecialMapper.execute().get(\"#result-set-0\"));",
                "if (list.size() == 1)",
                "return list.get(0);",
                "if (list.size() == 0)",
                "throw new ObjectDoesNotExist();",
                "throw new MultipleObjectsReturned();"
        );
    }

    @Test
    public void testGenerateDtoClassesWithReturnNull() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .returnNull(getReturnNull())
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "public java.lang.String getSomething()",
                "java.util.List<java.lang.String> list;",
                "list = ((java.util.List<java.lang.String> ) spGetSomethingWithSpecialMapper.execute().get(\"#result-set-0\"));",
                "if (list.size() == 1)",
                "return list.get(0);",
                "if (list.size() == 0)",
                "return null;",
                "throw new MultipleObjectsReturned();"
        );
    }

    @Test
    public void testGenerateDtoClassesWithOptional() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(Optional.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .returnNull(getReturnNull())
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "public java.util.Optional<java.lang.String> getSomething()",
                "java.util.List<java.lang.String> list;",
                "list = ((java.util.List<java.lang.String> ) spGetSomethingWithSpecialMapper.execute().get(\"#result-set-0\"));",
                "if (list.size() == 1)",
                "return java.util.Optional.of(list.get(0));",
                "if (list.size() == 0)",
                "return java.util.Optional.empty();",
                "throw new MultipleObjectsReturned();"
        );
    }

    public Optional<String> test() {
        return Optional.of("test");
    }


    @Test
    public void testGenerateDtoClassesWithParameters() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
                        .addArgument(new Variable("id", int.class.getCanonicalName()))
                        .addArgument(new Variable("name", String.class.getCanonicalName()))
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "public List<java.lang.String> getSomething(int id, java.lang.String name)",
                "Map<java.lang.String, Object> map;",
                "map = spGetSomethingWithSpecialMapper.execute(id, name);",
                "return ((List<java.lang.String> ) map.get(\"#result-set-0\"));"
        );
    }

    @Test
    public void testGenerateDtoClassesWithVoid() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("void"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "public void getSomething()",
                "spGetSomethingWithSpecialMapper.execute()"
        );
    }

    @Test
    public void testGenerateDtoClassesWithVoidAndArguments() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("void"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
                        .addArgument(new Variable("id", int.class.getCanonicalName()))
                        .addArgument(new Variable("name", String.class.getCanonicalName()))
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "getSomething(int id, String name)",
                "spGetSomethingWithSpecialMapper.execute(id, name)"
        );
    }

    @Test
    public void testGenerateDtoClassesWithInterceptor() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        Properties config = new Properties();
        config.put("interceptor", "at.rseiler.spbee.test.Interceptor");
        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, config);

        assertContains(javaCode,
                "java.lang.Object interceptorIdObject;",
                "interceptorIdObject = at.rseiler.spbee.test.Interceptor.before(\"sp_get_something\");",
                "Map<String, java.lang.Object> map;",
                "map = spGetSomething.execute();",
                "at.rseiler.spbee.test.Interceptor.after(interceptorIdObject, \"sp_get_something\");",
                "return ((List<String> ) map.get(\"#result-set-0\"));"
        );
    }

    @Test
    public void testGenerateDtoClassesWithInterceptorAndEntity() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        Properties config = new Properties();
        config.put("interceptor", "at.rseiler.spbee.test.Interceptor");
        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, config);

        assertContains(javaCode,
                "Object interceptorIdObject;",
                "interceptorIdObject = at.rseiler.spbee.test.Interceptor.before(\"sp_get_something\");",
                "java.util.List<java.lang.String> list;",
                "list = ((java.util.List<java.lang.String> ) spGetSomething.execute().get(\"#result-set-0\"));",
                "at.rseiler.spbee.test.Interceptor.after(interceptorIdObject, \"sp_get_something\");"
        );
    }

    @Test
    public void testGenerateDtoClassesWithInterceptorAndOptionalEntity() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(Optional.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        Properties config = new Properties();
        config.put("interceptor", "at.rseiler.spbee.test.Interceptor");
        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, config);

        assertContains(javaCode,
                "Object interceptorIdObject;",
                "interceptorIdObject = at.rseiler.spbee.test.Interceptor.before(\"sp_get_something\");",
                "java.util.List<java.lang.String> list;",
                "list = ((java.util.List<java.lang.String> ) spGetSomething.execute().get(\"#result-set-0\"));",
                "at.rseiler.spbee.test.Interceptor.after(interceptorIdObject, \"sp_get_something\");"
        );
    }

    @Test
    public void testGenerateDtoClassesWithMultipleResultSets() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        ArrayList<ResultSetVariable> resultSetVariables = new ArrayList<>();
        resultSetVariables.add(new ResultSetVariable("rs1", "at.rseiler.spbee.test.entity.Test1Entity", null, null, null));
        resultSetVariables.add(new ResultSetVariable("rs2", "java.util.List<at.rseiler.spbee.test.entity.Test2Entity>", null, null, null));
        resultSetClassMap.put("MultiResultSet", new ResultSetClass("MultiResultSet", resultSetVariables));
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("MultiResultSet"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "Map<String, Object> map;",
                "map = spGetSomethingWithSpecialMapper.execute();",
                "java.util.List<at.rseiler.spbee.test.entity.Test1Entity> list0;",
                "list0 = ((java.util.List<at.rseiler.spbee.test.entity.Test1Entity> ) map.get(\"#result-set-0\"));",
                "at.rseiler.spbee.test.entity.Test1Entity obj0;",
                "if (list0 .size() == 1)",
                "obj0 = list0 .get(0);",
                "if (list0 .size() == 0)",
                "throw new ObjectDoesNotExist();",
                "throw new MultipleObjectsReturned();",
                "java.util.List<Test2Entity> list1;",
                "list1 = ((java.util.List<Test2Entity> ) map.get(\"#result-set-1\"));",
                "return new MultiResultSet(obj0, list1);"
        );
    }

    @Test
    public void testGenerateDtoClassesWithMultipleResultSetsAndReturnNull() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        ArrayList<ResultSetVariable> resultSetVariables = new ArrayList<>();
        resultSetVariables.add(new ResultSetVariable("rs1", "at.rseiler.spbee.test.entity.Test1Entity", null, getReturnNull(), null));
        resultSetVariables.add(new ResultSetVariable("rs2", "java.util.List<at.rseiler.spbee.test.entity.Test2Entity>", null, null, null));
        resultSetClassMap.put("MultiResultSet", new ResultSetClass("MultiResultSet", resultSetVariables));
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("MultiResultSet"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "obj0 = null;"
        );
    }

    @Test
    public void testGenerateDtoClassesWithMultipleResultSetsAndOptional() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        ArrayList<ResultSetVariable> resultSetVariables = new ArrayList<>();
        resultSetVariables.add(new ResultSetVariable("rs1", "java.util.Optional<at.rseiler.spbee.test.entity.Test1Entity>", null, null, null));
        resultSetVariables.add(new ResultSetVariable("rs2", "java.util.List<at.rseiler.spbee.test.entity.Test2Entity>", null, null, null));
        resultSetClassMap.put("MultiResultSet", new ResultSetClass("MultiResultSet", resultSetVariables));
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("MultiResultSet"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "java.util.Optional obj0;",
                "obj0 = java.util.Optional.of(list0 .get(0));",
                "obj0 = java.util.Optional.empty();",
                "return new MultiResultSet(obj0, list1);"
        );
    }

    @Test
    public void testGenerateDtoClassesWithMultipleResultSetsAndInterceptor() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        ArrayList<ResultSetVariable> resultSetVariables = new ArrayList<>();
        resultSetVariables.add(new ResultSetVariable("rs1", "at.rseiler.spbee.test.entity.Test1Entity", null, null, null));
        resultSetVariables.add(new ResultSetVariable("rs2", "java.util.List<at.rseiler.spbee.test.entity.Test2Entity>", null, null, null));
        resultSetClassMap.put("MultiResultSet", new ResultSetClass("MultiResultSet", resultSetVariables));
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("MultiResultSet"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        Properties config = new Properties();
        config.put("interceptor", "at.rseiler.spbee.test.Interceptor");
        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, config);

        assertContains(javaCode,
                "java.lang.Object interceptorIdObject;",
                "interceptorIdObject = at.rseiler.spbee.test.Interceptor.before(\"sp_get_something\");",
                "Map<String, java.lang.Object> map;",
                "map = spGetSomethingWithSpecialMapper.execute();",
                "at.rseiler.spbee.test.Interceptor.after(interceptorIdObject, \"sp_get_something\");"
        );
    }

    @Test
    public void testGenerateDtoClassesWithAnnotation() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        Element isolationElement = mock(Element.class);
        when(isolationElement.toString()).thenReturn("READ_COMMITTED");

        AnnotationValue rollbackElement1 = mock(AnnotationValue.class);
        DeclaredType declaredType1 = mock(DeclaredType.class);
        when(declaredType1.toString()).thenReturn("java.sql.SQLSyntaxErrorException");
        when(rollbackElement1.getValue()).thenReturn(declaredType1);
        when(rollbackElement1.toString()).thenReturn("java.sql.SQLSyntaxErrorException.class");

        AnnotationValue rollbackElement2 = mock(AnnotationValue.class);
        DeclaredType declaredType2 = mock(DeclaredType.class);
        when(declaredType2.toString()).thenReturn("org.hsqldb.HsqlException");
        when(rollbackElement2.getValue()).thenReturn(declaredType2);
        when(rollbackElement2.toString()).thenReturn("org.hsqldb.HsqlException.class");

        List<AnnotationValue> rollbackForList = Arrays.asList(
                rollbackElement1,
                rollbackElement2
        );

        List<AnnotationInfo> annotationInfos = Arrays.asList(
                new AnnotationInfo("org.springframework.transaction.annotation.Transactional")
                        .addAnnotationValueInfo(ElementConverter.convert("isolation", isolationElement, "org.springframework.transaction.annotation.Isolation.READ_COMMITTED"))
                        .addAnnotationValueInfo(ElementConverter.convert("rollbackFor", rollbackForList, "java.util.List"))
        );

        System.out.println(annotationInfos);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .annotationInfos(annotationInfos)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "import java.sql.SQLSyntaxErrorException;",
                "import org.hsqldb.HsqlException;",
                "import org.springframework.transaction.annotation.Isolation;",
                "import org.springframework.transaction.annotation.Transactional;",
                "@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {",
                "SQLSyntaxErrorException.class,",
                "HsqlException.class",
                "})"
        );
    }

    @Test
    public void testGenerateDtoClassesSPTwice() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething1")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );
        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething2")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), String.class.getCanonicalName()))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "private final SpGetSomething spGetSomething;",
                "spGetSomething = new SpGetSomething(dataSource);",
                "public List<String> getSomething1()",
                "java.util.Map<String, Object> map;",
                "map = spGetSomething.execute();",
                "return ((List<String> ) map.get(\"#result-set-0\"));",
                "public List<String> getSomething2()",
                "java.util.Map<String, Object> map;",
                "map = spGetSomething.execute();",
                "return ((List<String> ) map.get(\"#result-set-0\"));"
        );
    }

    @Test
    public void testGenerateDuoClassesSPTwiceWithDifferentUsage() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        Set<String> mapperNames = new HashSet<>();
        mapperNames.add("at.rseiler.spbee.test.mapper.TestEntitySpGetSomethingMapper");
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething1")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), "at.rseiler.spbee.test.TestEntity"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );
        dtoClass.add(new StoredProcedureMethod.Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething2")
                        .returnTypeInfo(new TypeInfo(List.class.getCanonicalName(), "at.rseiler.spbee.test.TestEntity"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(getMappingConstructor("sp_get_something"))
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap);

        assertContains(javaCode,
                "private final SpGetSomething spGetSomething;",
                "private final SpGetSomethingWithSpGetSomething spGetSomethingWithSpGetSomething;",
                "spGetSomething = new SpGetSomething(dataSource);",
                "public List<at.rseiler.spbee.test.TestEntity> getSomething1()",
                "java.util.Map<String, Object> map;",
                "map = spGetSomething.execute();",
                "return ((List<at.rseiler.spbee.test.TestEntity> ) map.get(\"#result-set-0\"));",
                "public List<at.rseiler.spbee.test.TestEntity> getSomething2()",
                "java.util.Map<String, Object> map;",
                "map = spGetSomething.execute();",
                "return ((List<at.rseiler.spbee.test.TestEntity> ) map.get(\"#result-set-0\"));"
        );
    }

    private String generateJavaCode(DtoClass dtoClass, Map<String, ResultSetClass> resultSetClassMap) throws IOException, JClassAlreadyExistsException, ClassNotFoundException {
        return generateJavaCode(dtoClass,resultSetClassMap, new Properties());
    }

    private String generateJavaCode(DtoClass dtoClass, Map<String, ResultSetClass> resultSetClassMap, Properties config) throws IOException, JClassAlreadyExistsException, ClassNotFoundException {
        ProcessingEnvironment processingEnv = mock(ProcessingEnvironment.class);
        Filer filer = mock(Filer.class);
        JavaFileObject javaFileObject = mock(JavaFileObject.class);
        StringWriter stringWriter = new StringWriter();

        when(processingEnv.getFiler()).thenReturn(filer);
        when(filer.createSourceFile("at.rseiler.spbee.test.DtoTestImpl")).thenReturn(javaFileObject);
        when(javaFileObject.openWriter()).thenReturn(stringWriter);

        DtoGenerator dtoGenerator = new DtoGenerator(processingEnv, config, resultSetClassMap);
        List<DtoClass> dtoClasses = Arrays.asList(dtoClass);
        dtoGenerator.generateDtoClasses(dtoClasses);

        return stringWriter.toString();
    }

}