package at.rseiler.spbee.core.pojo.annotation;

import at.rseiler.spbee.core.annotation.RowMapper;

import javax.lang.model.type.MirroredTypeException;
import java.io.Serializable;

/**
 * Holds the data of a {@link at.rseiler.spbee.core.annotation.RowMapper} annotation.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class RowMapperData implements Serializable {

    private static final long serialVersionUID = 2273627881915262221L;

    private final String type;

    public RowMapperData(RowMapper rowMapper) {
        this.type = getRowMapperType(rowMapper);
    }

    public String getType() {
        return type;
    }

    private String getRowMapperType(RowMapper rowMapper) {
        try {
            return rowMapper.value().toString().replace("class ", "");
        } catch (MirroredTypeException mte) {
            // http://stackoverflow.com/a/10167558
            return mte.getTypeMirror().toString();
        }
    }

    @Override
    public String toString() {
        return "RowMapperData{" +
                "type='" + type + '\'' +
                '}';
    }
}
