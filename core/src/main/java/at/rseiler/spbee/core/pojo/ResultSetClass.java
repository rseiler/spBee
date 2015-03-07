package at.rseiler.spbee.core.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a ResultSet.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class ResultSetClass implements Serializable {

    private static final long serialVersionUID = -7325698206162246529L;

    private final String qualifiedClassName;
    private final List<ResultSetVariable> resultSetVariables;

    /**
     * Constructs a new ResultSetClass.
     *
     * @param qualifiedClassName the qualified class name or the ResultSet
     * @param resultSetVariables the variables of the ResultSet
     */
    public ResultSetClass(String qualifiedClassName, List<ResultSetVariable> resultSetVariables) {
        this.qualifiedClassName = qualifiedClassName;
        this.resultSetVariables = resultSetVariables;
    }

    /**
     * Returns the qualified class name.
     *
     * @return the qualified class name
     */
    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    /**
     * Returns the variables of the ResultSet.
     *
     * @return the variables
     */
    public List<ResultSetVariable> getResultSetVariables() {
        return resultSetVariables;
    }

    @Override
    public String toString() {
        return "ResultSetClass{" +
                "qualifiedClassName='" + qualifiedClassName + '\'' +
                ", resultSetVariables=" + resultSetVariables +
                '}';
    }
}
