package at.rseiler.spbee.core.exception;

/**
 * This exception is called if one object was expected but multiple rows were returned from the stored procedure.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class MultipleObjectsReturned extends RuntimeException {
    private static final long serialVersionUID = -6879962955270015853L;
}
