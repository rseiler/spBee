package at.rseiler.spbee.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Indicates which constructor should be used to map the result or the stored procedure.
 * <ul>
 * <li>
 * Marks a public constructor in a {@link at.rseiler.spbee.core.annotation.Entity} as mapping constructor. If more than
 * one mapping constructor is used then different  names can be used. It's not allowed that two constructors have the
 * same name.
 * </li>
 * <li>
 * Marks a method in a {@link at.rseiler.spbee.core.annotation.Dao} to use the specified mapping constructor.
 * </li>
 * <li>
 * Marks a field in a {@link at.rseiler.spbee.core.annotation.ResultSet} to use the specified mapping constructor.
 * </li>
 * </ul>
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Target(value = {CONSTRUCTOR, FIELD, METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MappingConstructor {

    /**
     * Defines the name of the mapping constructor.
     *
     * @return the name of the mapping constructor
     */
    String value() default "default";
}
