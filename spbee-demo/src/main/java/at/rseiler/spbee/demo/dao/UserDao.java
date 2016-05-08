package at.rseiler.spbee.demo.dao;

import at.rseiler.spbee.core.annotation.Dao;
import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.core.annotation.StoredProcedure;
import at.rseiler.spbee.demo.McName;
import at.rseiler.spbee.demo.SpName;
import at.rseiler.spbee.demo.entity.User;
import at.rseiler.spbee.demo.resultset.UserPermissionsResultSet;
import org.hsqldb.HsqlException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLSyntaxErrorException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Dao
public interface UserDao {

    @StoredProcedure(SpName.GET_USERS)
    List<User> getUsers();

    @StoredProcedure(SpName.GET_USER)
    User getUser(int id);

    @StoredProcedure(SpName.GET_USER)
    Optional<User> getUserOptional(int id);

    @StoredProcedure(SpName.GET_USER)
    @MappingConstructor(McName.SIMPLE_USER)
    User getSimpleUserMappingConstructor(int id);

    @StoredProcedure(SpName.GET_USER_WITH_PERMISSIONS)
    UserPermissionsResultSet getUserWithPermissions(int id);

    @StoredProcedure(SpName.GET_SIMPLE_USERS)
    @MappingConstructor(McName.SIMPLE_USER)
    List<User> getSimpleUsersWithMappingConstructor();

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {SQLSyntaxErrorException.class, HsqlException.class})
    @StoredProcedure(SpName.SAVE_USER)
    void saveUser(int id, String name, Timestamp created);

}
