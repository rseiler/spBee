package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.pojo.*;
import at.rseiler.spbee.core.pojo.StoredProcedureMethod.Builder;
import com.sun.codemodel.JClassAlreadyExistsException;
import org.junit.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.*;

import static at.rseiler.spbee.core.generator.GeneratorUtil.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoredProcedureGeneratorTest {

    @Test
    public void testGenerateStoredProcedureClasses() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        Set<String> entityNames = new HashSet<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("at.rseiler.spbee.test.entity.TestEntity"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, entityNames);

        assertContains(javaCode,
                "package at.rseiler.spbee.test.storedprocedure;",
                "import java.util.Map;",
                "import javax.annotation.Generated;",
                "import javax.sql.DataSource;",
                "import at.rseiler.spbee.test.entity.mapper.TestEntityDefaultMapper;",
                "import org.springframework.jdbc.core.SqlReturnResultSet;",
                "import org.springframework.jdbc.object.StoredProcedure;",
                "@Generated(value = \"at.rseiler.spbee.core.SPBeeAnnotationProcessor\", date = ",
                "public class SpGetSomething",
                "extends StoredProcedure",
                "public SpGetSomething(DataSource dataSource)",
                "super(dataSource, \"sp_get_something\");",
                "declareParameter(new SqlReturnResultSet(\"#result-set-0\", new TestEntityDefaultMapper()));",
                "compile();",
                "public Map<String, Object> execute()",
                "return super.execute();"
        );
    }

    @Test
    public void testGenerateStoredProcedureClassesWithArguments() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        Set<String> entityNames = new HashSet<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("at.rseiler.spbee.test.entity.TestEntity"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
                        .addArgument(new Variable("id", int.class.getCanonicalName()))
                        .addArgument(new Variable("name", String.class.getCanonicalName()))
                        .addArgument(new Variable("timestamp", Timestamp.class.getCanonicalName()))
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, entityNames);

        assertContains(javaCode,
                "declareParameter(new SqlParameter(\"id\", Types.INTEGER));",
                "declareParameter(new SqlParameter(\"name\", Types.VARCHAR));",
                "declareParameter(new SqlParameter(\"timestamp\", Types.TIMESTAMP));",
                "execute(int id, String name, Timestamp timestamp)",
                "return super.execute(id, name, timestamp);"
        );
    }

    @Test
    public void testGenerateStoredProcedureClassesWithRowMapper() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        Set<String> entityNames = new HashSet<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("at.rseiler.spbee.test.entity.TestEntity"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(getRowMapper(SpecialMapper.class))
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, entityNames);

        assertContains(javaCode,
                "import at.rseiler.spbee.core.generator.SpecialMapper;",
                "new SpecialMapper()"
        );
    }

    @Test
    public void testGenerateStoredProcedureClassesWithMappingConstructor() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        Set<String> entityNames = new HashSet<>();
        entityNames.add("at.rseiler.spbee.test.entity.mapper.TestEntitySpGetSomethingMapper");
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("at.rseiler.spbee.test.entity.TestEntity"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(getMappingConstructor("sp_get_something"))
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, entityNames);

        assertContains(javaCode,
                "import at.rseiler.spbee.test.entity.mapper.TestEntitySpGetSomethingMapper;",
                "declareParameter(new SqlReturnResultSet(\"#result-set-0\", new TestEntitySpGetSomethingMapper()));"
        );
    }

    @Test
    public void testGenerateStoredProcedureClassesWithMultipleResultSets() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        ArrayList<ResultSetVariable> resultSetVariables = new ArrayList<>();
        resultSetVariables.add(new ResultSetVariable("rs1", "at.rseiler.spbee.test.entity.Test1Entity", null, null, null));
        resultSetVariables.add(new ResultSetVariable("rs2", "java.util.List<at.rseiler.spbee.test.entity.Test2Entity>", null, null, null));
        resultSetClassMap.put("MultiResultSet", new ResultSetClass("MultiResultSet", resultSetVariables));
        Set<String> entityNames = new HashSet<>();
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("MultiResultSet"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, entityNames);

        assertContains(javaCode,
                "declareParameter(new SqlReturnResultSet(\"#result-set-0\", new Test1EntityDefaultMapper()));",
                "declareParameter(new SqlReturnResultSet(\"#result-set-1\", new Test2EntityDefaultMapper()));"
        );
    }

    @Test
    public void testGenerateStoredProcedureClassesWithMultipleResultSetsAndMappingConstructor() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        ArrayList<ResultSetVariable> resultSetVariables = new ArrayList<>();
        resultSetVariables.add(new ResultSetVariable("rs1", "at.rseiler.spbee.test.entity.Test1Entity", null, null, getMappingConstructor("sp_test1")));
        resultSetVariables.add(new ResultSetVariable("rs2", "java.util.List<at.rseiler.spbee.test.entity.Test2Entity>", null, null, getMappingConstructor("sp_test2")));
        resultSetClassMap.put("MultiResultSet", new ResultSetClass("MultiResultSet", resultSetVariables));
        Set<String> entityNames = new HashSet<>();
        entityNames.add("at.rseiler.spbee.test.entity.mapper.Test1EntitySpTest1Mapper");
        entityNames.add("at.rseiler.spbee.test.entity.mapper.Test2EntitySpTest2Mapper");
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("MultiResultSet"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, entityNames);

        assertContains(javaCode,
                "import at.rseiler.spbee.test.entity.mapper.Test1EntitySpTest1Mapper",
                "import at.rseiler.spbee.test.entity.mapper.Test2EntitySpTest2Mapper",
                "declareParameter(new SqlReturnResultSet(\"#result-set-0\", new Test1EntitySpTest1Mapper()));",
                "declareParameter(new SqlReturnResultSet(\"#result-set-1\", new Test2EntitySpTest2Mapper()));"
        );
    }

    @Test
    public void testGenerateStoredProcedureClassesWithMultipleResultSetsAndRowMapper() throws Exception {
        Map<String, ResultSetClass> resultSetClassMap = new HashMap<>();
        ArrayList<ResultSetVariable> resultSetVariables = new ArrayList<>();
        resultSetVariables.add(new ResultSetVariable("rs1", "at.rseiler.spbee.test.entity.Test1Entity", getRowMapper(SpecialMapper.class), null, null));
        resultSetVariables.add(new ResultSetVariable("rs2", "java.util.List<at.rseiler.spbee.test.entity.Test2Entity>", getRowMapper(SpecialMapper.class), null, null));
        resultSetClassMap.put("MultiResultSet", new ResultSetClass("MultiResultSet", resultSetVariables));
        Set<String> entityNames = new HashSet<>();
        entityNames.add("at.rseiler.spbee.core.generator.SpecialMapper");
        DtoClass dtoClass = new DtoClass("at.rseiler.spbee.test.DtoTest", true, false);

        dtoClass.add(new Builder()
                        .dtoClassName("at.rseiler.spbee.test.DtoTest")
                        .methodName("getSomething")
                        .returnTypeInfo(new TypeInfo("MultiResultSet"))
                        .storedProcedure(getStoredProcedure("sp_get_something"))
                        .rowMapper(null)
                        .mappingConstructor(null)
                        .build()
        );

        String javaCode = generateJavaCode(dtoClass, resultSetClassMap, entityNames);

        assertContains(javaCode,
                "declareParameter(new SqlReturnResultSet(\"#result-set-0\", new SpecialMapper()));",
                "declareParameter(new SqlReturnResultSet(\"#result-set-1\", new SpecialMapper()));"
        );
    }

    private String generateJavaCode(DtoClass dtoClass, Map<String, ResultSetClass> resultSetClassMap, Set<String> entityNames) throws IOException, JClassAlreadyExistsException {
        ProcessingEnvironment processingEnv = mock(ProcessingEnvironment.class);
        Filer filer = mock(Filer.class);
        JavaFileObject javaFileObject = mock(JavaFileObject.class);
        StringWriter stringWriter = new StringWriter();

        when(processingEnv.getFiler()).thenReturn(filer);
        when(filer.createSourceFile(anyString())).thenReturn(javaFileObject);
        when(javaFileObject.openWriter()).thenReturn(stringWriter);

        StoredProcedureGenerator storedProcedureGenerator = new StoredProcedureGenerator(processingEnv, resultSetClassMap);
        List<DtoClass> dtoClasses = Collections.singletonList(dtoClass);
        storedProcedureGenerator.generateStoredProcedureClasses(dtoClasses);

        return stringWriter.toString();
    }

}