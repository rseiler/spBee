package at.rseiler.spbee.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Defines the name of the stored procedure which should be called.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Target(value = {METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface StoredProcedure {

    /**
     * Defines the name of the stored procedure.
     *
     * @return the name of the stored procedure
     */
    String value();
}
