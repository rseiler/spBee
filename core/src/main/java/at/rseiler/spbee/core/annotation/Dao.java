package at.rseiler.spbee.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Marks an interface or an abstract class as to generate the implementation code.
 * <p>
 * The generated class uses the same package like the annotated interface or abstract class.
 * The class name of the generated class is the name of the annotated object with "Impl" as postfix.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Target(value = {TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Dao {
}
