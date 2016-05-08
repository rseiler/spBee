package at.rseiler.spbee.demo.dao;

import at.rseiler.annotation.junit.ThreadUnsafe;
import org.apache.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:context.xml")
@Category(ThreadUnsafe.class)
public abstract class AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractTest.class);

    @Autowired
    ApplicationContext applicationContext;

//    @Before
//    public void setUp() throws Exception {
//        initDB((DataSource) applicationContext.getBean("dataSource"));
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        clearDB((DataSource) applicationContext.getBean("dataSource"));
//    }
//
//    private static void clearDB(DataSource dataSource) throws IOException, SQLException {
//        executeSqlFile(dataSource, "clear-db.sql");
//    }
//
//    private static void initDB(DataSource dataSource) throws IOException, SQLException {
//        executeSqlFile(dataSource, "init-db.sql");
//    }
//
//    private static void executeSqlFile(DataSource dataSource, String sqlFileName) throws IOException, SQLException {
//        File initDbFile = new File(AbstractTest.class.getClassLoader().getResource(sqlFileName).getFile());
//        String initSqlStatements = FileUtils.readFileToString(initDbFile, "UTF-8");
//        String[] sqlStatements = initSqlStatements.split("--;");
//
//        try (Statement statement = dataSource.getConnection().createStatement()) {
//            Arrays.asList(sqlStatements).stream().forEach(sql -> addBatch(statement, sql));
//            statement.executeBatch();
//        }
//    }
//
//    private static void addBatch(Statement statement, String sql) {
//        try {
//            statement.addBatch(sql);
//        } catch (SQLException e) {
//            LOG.error("SQL statement failed:\n" + sql, e);
//        }
//    }

}
