package at.rseiler.spbee.generator;

import at.rseiler.annotation.junit.ThreadUnsafe;
import at.rseiler.spbee.AbstractTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:context.xml")
@Category(ThreadUnsafe.class)
public class ModelGeneratorTest extends AbstractTest {

    @Test
    public void testSingleResultSet() throws Exception {
        String spName = "sp_get_user";
        String spArgs = "1";
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");

        new ModelGenerator(dataSource, "at.rseiler.spbee.generator", spName, spArgs).execute();
    }

    @Test
    public void testMultipleResultSets() throws Exception {
        String spName = "sp_get_user_with_permissions";
        String spArgs = "1";
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");

        new ModelGenerator(dataSource, "at.rseiler.spbee.generator", spName, spArgs).execute();
    }

}