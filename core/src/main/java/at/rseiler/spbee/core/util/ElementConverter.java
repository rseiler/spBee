package at.rseiler.spbee.core.util;

import at.rseiler.spbee.core.pojo.AnnotationValueInfo;
import at.rseiler.spbee.core.pojo.AnnotationValueInfo.Kind;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to convert an annotation element.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public final class ElementConverter {

    private ElementConverter() {
    }

    /**
     * Converts an annotation element object to an AnnotationValueInfo object.
     *
     * @param simpleName the name of the annotation
     * @param value      the value of the annotation
     * @param type       the type of the annotation
     * @return the converted element
     */
    public static AnnotationValueInfo convert(String simpleName, Object value, String type) {
        if (value instanceof Boolean || value instanceof Byte || value instanceof Character || value instanceof Double || value instanceof Float || value instanceof Long || value instanceof Short || value instanceof Integer || value instanceof String) {
            return new AnnotationValueInfo(simpleName, value, type, Kind.BASIC);
        } else if (value instanceof DeclaredType) {
            return new AnnotationValueInfo(simpleName, value.toString(), type, Kind.DECLARED_TYPE);
        } else if (value instanceof Element) {
            return new AnnotationValueInfo(simpleName, value.toString(), type, Kind.ELEMENT);
        } else if (value instanceof List) {
            List<AnnotationValueInfo> list = new ArrayList<>();

            for (AnnotationValue annotationValueObj : (List<AnnotationValue>) value) {
                list.add(convert("", annotationValueObj.getValue(), annotationValueObj.toString()));
            }

            return new AnnotationValueInfo(simpleName, list, type, Kind.LIST);
        } else {
            throw new RuntimeException("Failed to convert: simpleName=" + simpleName + ", value=" + value.toString() + ", type=" + type);
        }
    }

}
