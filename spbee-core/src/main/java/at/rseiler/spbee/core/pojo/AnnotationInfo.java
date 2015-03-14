package at.rseiler.spbee.core.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the information of an annotation.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class AnnotationInfo implements Serializable {

    private static final long serialVersionUID = 573649572871493759L;

    private final String annotationType;
    private final List<AnnotationValueInfo> annotationValueInfos = new ArrayList<>();

    /**
     * Constructs a new AnnotationInfo.
     *
     * @param annotationType the type of the annotation
     */
    public AnnotationInfo(String annotationType) {
        this.annotationType = annotationType;
    }

    /**
     * Adds a parameter to the annotation.
     *
     * @param annotationValueInfo the parameter
     * @return itself
     */
    public AnnotationInfo addAnnotationValueInfo(AnnotationValueInfo annotationValueInfo) {
        annotationValueInfos.add(annotationValueInfo);
        return this;
    }

    /**
     * Returns the type of the annotation.
     *
     * @return the type
     */
    public String getAnnotationType() {
        return annotationType;
    }

    /**
     * Returns the parameters of the annotation.
     *
     * @return the parameters
     */
    public List<AnnotationValueInfo> getAnnotationValueInfos() {
        return annotationValueInfos;
    }

    @Override
    public String toString() {
        return "AnnotationInfo{" +
                "annotationType='" + annotationType + '\'' +
                ", annotationValueInfos=" + annotationValueInfos +
                '}';
    }

}
