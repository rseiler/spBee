package at.rseiler.spbee.core.pojo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds the information to generate the RowMapper class.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class MapperClass implements Serializable {

    private static final long serialVersionUID = 8474863363651815804L;

    private final String qualifiedMapperClassName;
    private final String type;
    private final List<Variable> variables = new LinkedList<>();

    /**
     * Constructs a new MapperClass.
     *
     * @param qualifiedMapperClassName the qualified class name of the mapper class
     * @param type                     the type of the mapped class
     */
    public MapperClass(String qualifiedMapperClassName, String type) {
        this.qualifiedMapperClassName = qualifiedMapperClassName;
        this.type = type;
    }

    /**
     * Adds a variable, which represents a parameter of the constructor of the mapped class.
     *
     * @param variable the variable which should be added
     */
    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    /**
     * Returns the variables which represent the parameters of the constructor of the mapped class.
     *
     * @return the variables
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Returns the qualified class name of the mapper class.
     *
     * @return the qualified class name
     */
    public String getQualifiedMapperClassName() {
        return qualifiedMapperClassName;
    }

    /**
     * Returns the type of the mapped class.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MapperClass{" +
                "variables=" + variables +
                ", qualifiedMapperClassName='" + qualifiedMapperClassName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

}
