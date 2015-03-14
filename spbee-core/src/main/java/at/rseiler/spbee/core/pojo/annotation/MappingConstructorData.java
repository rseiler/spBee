package at.rseiler.spbee.core.pojo.annotation;

import at.rseiler.spbee.core.annotation.MappingConstructor;

import java.io.Serializable;

/**
 * Holds the data of a {@link at.rseiler.spbee.core.annotation.MappingConstructor} annotation.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class MappingConstructorData implements Serializable {

    private static final long serialVersionUID = -441709829937601253L;

    private final String name;

    public MappingConstructorData(MappingConstructor mappingConstructor) {
        name = mappingConstructor != null ? mappingConstructor.value() : "Default";
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "MappingConstructorData{" +
                "name='" + name + '\'' +
                '}';
    }

}
