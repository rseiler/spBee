package at.rseiler.spbee.core.pojo.annotation;

import at.rseiler.spbee.core.annotation.StoredProcedure;

import java.io.Serializable;

/**
 * Holds the data of a {@link at.rseiler.spbee.core.annotation.StoredProcedure} annotation.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class StoredProcedureData implements Serializable {

    private static final long serialVersionUID = 8765442214373371627L;

    private final String name;

    public StoredProcedureData(StoredProcedure storedProcedure) {
        this.name = storedProcedure.value();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "StoredProcedureData{" +
                "name='" + name + '\'' +
                '}';
    }

}
