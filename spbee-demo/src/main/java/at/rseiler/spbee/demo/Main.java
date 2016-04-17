package at.rseiler.spbee.demo;

import at.rseiler.spbee.demo.dao.AbstractUserDao;
import at.rseiler.spbee.demo.dao.UserDao;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Demo to show how to use spBee in a standalone application.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) throws IOException, SQLException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
        initDB((DataSource) ctx.getBean("dataSource"));
        UserDao userDao = ctx.getBean(UserDao.class);
        AbstractUserDao abstractUserDao = ctx.getBean(AbstractUserDao.class);

        LOG.info(userDao.getUsers());
        LOG.info(userDao.getUser(2));
        LOG.info(userDao.getUserWithPermissions(3).getUser());
        LOG.info(userDao.getSimpleUsersWithMappingConstructor());
        LOG.info(abstractUserDao.getSimpleUsersWithOwnMapper());
        LOG.info(abstractUserDao.userDataSourceDirectly(1));

        // look at the Test to see more examples
    }

    private static void initDB(DataSource dataSource) throws IOException, SQLException {
        File initDbFile = new File(Main.class.getClassLoader().getResource("init-db.sql").getFile());
        String initSqlStatements = FileUtils.readFileToString(initDbFile, "UTF-8");
        String[] sqlStatements = initSqlStatements.split("--;");

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            Arrays.asList(sqlStatements).stream().forEach(sql -> addBatch(statement, sql));
            statement.executeBatch();
        }
    }

    private static void addBatch(Statement statement, String sql) {
        try {
            statement.addBatch(sql);
        } catch (SQLException e) {
            LOG.error("SQL statement failed:\n" + sql, e);
        }
    }

}

