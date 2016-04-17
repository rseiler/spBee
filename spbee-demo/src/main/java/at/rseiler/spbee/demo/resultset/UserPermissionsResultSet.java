package at.rseiler.spbee.demo.resultset;


import at.rseiler.spbee.core.annotation.ResultSet;
import at.rseiler.spbee.core.annotation.ReturnNull;
import at.rseiler.spbee.core.annotation.RowMapper;
import at.rseiler.spbee.demo.entity.Permission;
import at.rseiler.spbee.demo.entity.User;
import at.rseiler.spbee.demo.entity.User.Builder;
import at.rseiler.spbee.demo.mapper.SimpleUserMapper;

import java.util.List;
import java.util.Optional;

/**
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@ResultSet
public class UserPermissionsResultSet {

    @ReturnNull
    @RowMapper(SimpleUserMapper.class)
    private final User user;
    private final List<Permission> permissions;

    public UserPermissionsResultSet(User user, List<Permission> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    public Optional<User> getUser() {
        if (user != null) {
            return Optional.of(new Builder(user).permissions(permissions).build());
        }
        return Optional.empty();
    }

}
