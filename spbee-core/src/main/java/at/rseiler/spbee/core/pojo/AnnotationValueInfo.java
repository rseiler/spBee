package at.rseiler.spbee.core.pojo;

import java.io.Serializable;

/**
 * Holds the information of a annotation parameter.
 * <p>
 * The field value can hold other javax.lang.model.element.* classes.
 * See: at.rseiler.spbee.core.generator.DtoGenerator.DtoClassGeneratorInstance#addAnnotationParam(com.sun.codemodel.JAnnotationUse, AnnotationValueInfo)
 * and at.rseiler.spbee.core.generator.DtoGenerator.DtoClassGeneratorInstance#addAnnotationArrayMemberParam(com.sun.codemodel.JAnnotationArrayMember, AnnotationValueInfo)
 * </p>
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class AnnotationValueInfo implements Serializable {

    private static final long serialVersionUID = 5474410388632197978L;

    private final String name;
    private final Object value;
    private final String type;
    private final Kind kind;

    /**
     * Constructs a new AnnotationValueInfo.
     *
     * @param name  the name of the annotation parameter
     * @param value the value of the annotation parameter
     * @param type the type of the annotation parameter
     * @param kind  the kind of the annotation parameter
     */
    public AnnotationValueInfo(String name, Object value, String type, Kind kind) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.kind = kind;
    }

    /**
     * Returns the name of the annotation parameter.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the parameter.
     *
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return "AnnotationValueInfo{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", type='" + type + '\'' +
                ", kind=" + kind +
                '}';
    }

    public enum Kind {
        BASIC, ELEMENT, DECLARED_TYPE, LIST
    }

}
