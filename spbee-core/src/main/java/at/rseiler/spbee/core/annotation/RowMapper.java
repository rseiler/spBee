package at.rseiler.spbee.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Indicates which row mapper should be used to map the result or the stored procedure.
 * <ul>
 * <li>
 * Marks a class, which implements the org.springframework.jdbc.core.RowMapper interface, as possible row mapper.
 * </li>
 * <li>
 * Marks a method in a {@link at.rseiler.spbee.core.annotation.Dao} to use the specified row mapper.
 * </li>
 * <li>
 * Marks a field in a {@link at.rseiler.spbee.core.annotation.ResultSet} to use the specified row mapper.
 * </li>
 * </ul>
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Target(value={METHOD, FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface RowMapper {

    /**
     * Defines the class of the row mapper.
     *
     * @return the class of the row mapper
     */
    Class<?> value();
}
