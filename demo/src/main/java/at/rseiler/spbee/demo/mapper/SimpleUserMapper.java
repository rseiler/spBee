package at.rseiler.spbee.demo.mapper;


import at.rseiler.spbee.demo.entity.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class SimpleUserMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getInt(1),
                "not loaded",
                new ArrayList<>());
    }

}
