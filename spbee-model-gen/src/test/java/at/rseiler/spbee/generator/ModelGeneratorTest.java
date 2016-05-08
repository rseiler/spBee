package at.rseiler.spbee.generator;

import at.rseiler.annotation.junit.ThreadUnsafe;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:context.xml")
@Category(ThreadUnsafe.class)
public class ModelGeneratorTest {

    private static final Logger LOG = Logger.getLogger(ModelGeneratorTest.class);

    @Autowired
    DataSource dataSource;

    @Test
    public void testSingleResultSet() throws Exception {
        String spName = "sp_get_user";
        String spArgs = "1";

        List<ClassData> classDataList = new ModelGenerator(dataSource, "at.rseiler.spbee.generator", spName, spArgs).execute();
        classDataList.stream().forEach(System.out::println);
    }

    @Test
    public void testMultipleResultSets() throws Exception {
        String spName = "sp_get_user_with_permissions";
        String spArgs = "1";

        List<ClassData> classDataList = new ModelGenerator(dataSource, "at.rseiler.spbee.generator", spName, spArgs).execute();
        classDataList.stream().forEach(System.out::println);
    }
}