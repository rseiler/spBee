package at.rseiler.spbee.core.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Indicates that an annotated class is an entity.
 * The entity is used to map the result of a stored procedure call.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Target(value = {TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Entity {
}
