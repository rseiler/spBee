package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.pojo.MapperClass;
import at.rseiler.spbee.core.pojo.Variable;
import com.sun.codemodel.JClassAlreadyExistsException;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static at.rseiler.spbee.core.generator.GeneratorUtil.assertContains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperGeneratorTest {

    private ProcessingEnvironment processingEnv;

    @Before
    public void setUp() {
        processingEnv = mock(ProcessingEnvironment.class);
    }

    @Test
    public void testGenerateMappers1() throws Exception {
        MapperClass mapperClass = new MapperClass("at.rseiler.spbee.test.mapper.TestMapper", "at.rseiler.spbee.test.entity.TestEntity");
        String javaCode = generateJavaCode(mapperClass);

        assertContains(javaCode,
                "package at.rseiler.spbee.test.mapper;",
                "import java.sql.ResultSet;",
                "import javax.annotation.Generated;",
                "import at.rseiler.spbee.test.entity.TestEntity;",
                "import org.springframework.jdbc.core.RowMapper;",
                "@Generated(value = \"at.rseiler.spbee.core.SPBeeAnnotationProcessor\", date = ",
                "public class TestMapper",
                "implements RowMapper<TestEntity>",
                "public TestEntity mapRow(ResultSet rs, int rowNum)",
                "return new TestEntity();"
        );
    }

    @Test
    public void testGenerateMappers2() throws Exception {
        MapperClass mapperClass = new MapperClass("at.rseiler.spbee.test.mapper.TestMapper", "at.rseiler.spbee.test.entity.TestEntity");
        mapperClass.addVariable(new Variable("id", int.class.getCanonicalName()));
        mapperClass.addVariable(new Variable("name", String.class.getCanonicalName()));
        mapperClass.addVariable(new Variable("isTest", Boolean.class.getCanonicalName()));

        String javaCode = generateJavaCode(mapperClass);

        assertContains(javaCode,
                "return new TestEntity(rs.getInt(1), rs.getString(2), rs.getBoolean(3));"
        );
    }

    private String generateJavaCode(MapperClass mapperClass) throws IOException, JClassAlreadyExistsException, ClassNotFoundException {
        Filer filer = mock(Filer.class);
        JavaFileObject javaFileObject = mock(JavaFileObject.class);
        StringWriter stringWriter = new StringWriter();

        when(processingEnv.getFiler()).thenReturn(filer);
        when(filer.createSourceFile("at.rseiler.spbee.test.mapper.TestMapper")).thenReturn(javaFileObject);
        when(javaFileObject.openWriter()).thenReturn(stringWriter);

        MapperGenerator mapperGenerator = new MapperGenerator(processingEnv);
        List<MapperClass> mapperClasses = Collections.singletonList(mapperClass);
        mapperGenerator.generateMappers(mapperClasses);

        return stringWriter.toString();
    }

}