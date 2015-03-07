package at.rseiler.spbee.core.util;

import at.rseiler.spbee.core.SPBeeAnnotationProcessor;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import javax.annotation.Generated;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Some util methods for the CodeModel library.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public final class CodeModelUtil {

    private static final String ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());

    private CodeModelUtil() {
    }

    /**
     * Adds the {@link javax.annotation.Generated} annotation to the class.
     * E.g.: @Generated(value = "at.rseiler.spbee.core.SPBeeAnnotationProcessor", date = "2015-02-21T18:40:15.517+0100")
     *
     * @param aClass the class which will be annotated
     */
    public static void annotateGenerated(JDefinedClass aClass) {
        aClass.annotate(Generated.class).param("value", SPBeeAnnotationProcessor.class.getCanonicalName()).param("date", ISO8601);
    }

    /**
     * Defines a Map<String, Object> JClass.
     *
     * @param model the model which will be used to define the map
     * @return the Map<String, Object> JClass
     */
    public static JClass getMapStringObject(JCodeModel model) {
        return model.directClass(Map.class.getCanonicalName()).narrow(String.class, Object.class);
    }

    /**
     * Defines a generic List like List<T>.
     *
     * @param model            the model which will be used to define the list
     * @param genericClassType the generic type of the list
     * @return the generic List JClass
     */
    public static JClass getGenericList(JCodeModel model, String genericClassType) {
        return model.directClass(List.class.getCanonicalName()).narrow(model.directClass(genericClassType));
    }

    /**
     * Defines a generic Optional like Optional<T>.
     *
     * @param model            the model which will be used to define the list
     * @param genericClassType the generic type of the list
     * @return the generic Optional JClass
     */

    public static JClass getGenericOptional(JCodeModel model, String genericClassType) {
        return model.directClass(Optional.class.getCanonicalName()).narrow(model.directClass(genericClassType));
    }

}
