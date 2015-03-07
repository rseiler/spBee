package at.rseiler.spbee.core.exception;

/**
 * This exception is thrown if one object is expected but the stored procedure returned no rows.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class ObjectDoesNotExist extends RuntimeException {
    private static final long serialVersionUID = 6520845859617682407L;
}
