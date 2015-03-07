package at.rseiler.spbee.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Indicates that the annotated method should return null instead of throwing a
 * {@link at.rseiler.spbee.core.exception.ObjectDoesNotExist} exception.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Target(value = {METHOD, FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface ReturnNull {
}
