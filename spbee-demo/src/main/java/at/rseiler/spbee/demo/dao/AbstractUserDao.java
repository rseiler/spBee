package at.rseiler.spbee.demo.dao;

import at.rseiler.spbee.core.annotation.Dao;
import at.rseiler.spbee.core.annotation.ReturnNull;
import at.rseiler.spbee.core.annotation.RowMapper;
import at.rseiler.spbee.core.annotation.StoredProcedure;
import at.rseiler.spbee.demo.SpName;
import at.rseiler.spbee.demo.entity.User;
import at.rseiler.spbee.demo.mapper.SimpleUserMapper;
import at.rseiler.spbee.demo.resultset.UserPermissionsResultSet;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Dao
public abstract class AbstractUserDao {

    private static final Logger LOG = Logger.getLogger(AbstractUserDao.class);

    private final DataSource dataSource;

    AbstractUserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @StoredProcedure(SpName.GET_USER_WITH_PERMISSIONS)
    abstract UserPermissionsResultSet getUserWithPermissionsInternal(int id);

    public Optional<User> getUserWithPermissions(int id) {
        return getUserWithPermissionsInternal(id).getUser();
    }

    @StoredProcedure(SpName.GET_USER)
    public abstract Optional<User> getUser(int id);

    @RowMapper(SimpleUserMapper.class)
    @StoredProcedure(SpName.GET_SIMPLE_USERS)
    public abstract List<User> getSimpleUsersWithOwnMapper();

    @ReturnNull
    @StoredProcedure(SpName.GET_USER)
    public abstract User getUserPossibleNull(int id);

    @StoredProcedure(SpName.GET_USERS_BY_IDS)
    public abstract List<User> getUsersByIds(Integer[] ids);

    public User userDataSourceDirectly(int id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM USER WHERE id=?")
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return new User(resultSet.getInt(1), resultSet.getString(2));
                }
            }
        } catch (SQLException e) {
            LOG.error("SqlException occurred", e);
        }

        return null;
    }

}
