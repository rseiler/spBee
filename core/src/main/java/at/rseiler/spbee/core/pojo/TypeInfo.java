package at.rseiler.spbee.core.pojo;

import java.io.Serializable;
import java.util.Optional;

/**
 * Holds the type information which consists of the type and the generic type.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class TypeInfo implements Serializable {

    private static final long serialVersionUID = -6281028938216215481L;

    private final String type;
    private final String genericType;

    /**
     * Constructs a new TypeInfo so that it represents a basic type.
     *
     * @param type the type
     */
    public TypeInfo(String type) {
        this(type, null);
    }

    /**
     * Constructs a new TypeInfo so that it represents a basic type or a type with generics.
     *
     * @param type        the type
     * @param genericType the generic type
     */
    public TypeInfo(String type, String genericType) {
        this.type = type;
        this.genericType = genericType;
    }

    /**
     * Returns the base type.
     * E.g.:
     * <pre>
     * String -> String
     * List<String> -> List
     * </pre>
     *
     * @return the base type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the generic type.
     * E.g.:
     * <pre>
     * List<String> -> String;
     * String -> Optional.empty()
     * </pre>
     *
     * @return the generic type
     */
    public Optional<String> getGenericType() {
        return Optional.ofNullable(genericType);
    }

    /**
     * Returns the generic type if it exists otherwise the base type.
     * E.g.:
     * <pre>
     * String -> String
     * List<String> -> String
     * </pre>
     *
     * @return the generic type or the base type
     */
    public String getGenericTypeOrType() {
        return Optional.ofNullable(genericType).orElse(type);
    }

    /**
     * Returns the string representation of the type and generic type.
     *
     * @return the string representation
     */
    public String asString() {
        return genericType != null ? type + '<' + genericType + '>' : type;
    }

    @Override
    public String toString() {
        return "TypeInfo{" +
                "type='" + type + '\'' +
                ", genericType=" + genericType +
                '}';
    }

}
