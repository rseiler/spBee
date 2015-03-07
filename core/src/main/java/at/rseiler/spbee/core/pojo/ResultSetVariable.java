package at.rseiler.spbee.core.pojo;

import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.core.annotation.ReturnNull;
import at.rseiler.spbee.core.annotation.RowMapper;
import at.rseiler.spbee.core.pojo.annotation.MappingConstructorData;
import at.rseiler.spbee.core.pojo.annotation.RowMapperData;
import at.rseiler.spbee.core.util.StringUtil;

/**
 * Represents a variable of a ResultSet.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class ResultSetVariable extends Variable {

    private static final long serialVersionUID = -1847032188564695473L;

    private final boolean returnNull;
    private final RowMapperData rowMapper;
    private final MappingConstructorData mappingConstructor;

    /**
     * Constructs a new ResultSetVariable.
     *
     * @param name               the name of the variable
     * @param qualifiedClassName the type of the variable
     * @param rowMapper          (nullable) the RowMapper annotation of the variable
     * @param returnNull         (nullable) the ReturnNull annotation of the variable
     * @param mappingConstructor (nullable) the MappingConstructor annotation of the variable
     */
    public ResultSetVariable(String name, String qualifiedClassName, RowMapper rowMapper, ReturnNull returnNull, MappingConstructor mappingConstructor) {
        super(name, qualifiedClassName);
        this.rowMapper = rowMapper != null ? new RowMapperData(rowMapper) : null;
        this.returnNull = returnNull != null;
        this.mappingConstructor = new MappingConstructorData(mappingConstructor);
    }

    /**
     * Returns the qualified class name of the RowMapper.
     *
     * @return the qualified class name
     */
    public String getRowMapper() {
        if (rowMapper != null) {
            return rowMapper.getType();
        }

        return StringUtil.getQualifiedMapperClassName(getTypeInfo().getGenericTypeOrType(), mappingConstructor.getName());
    }

    /**
     * Returns if null should be used instead of throwing an exception if an entity is expected but nothing is returned
     * from the database.
     *
     * @return true if null should be used as return value
     */
    public boolean useNullInsteadOfAnException() {
        return returnNull;
    }

}
