package at.rseiler.spbee.core.pojo;

import at.rseiler.spbee.core.util.StringUtil;

import java.io.Serializable;

/**
 * Holds the information of a variable or parameter.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class Variable implements Serializable {

    private static final long serialVersionUID = 9008463894700364670L;

    private final String name;
    private final TypeInfo typeInfo;

    /**
     * Constructs a new Variable.
     *
     * @param name               the name of the variable
     * @param qualifiedClassName the qualified class name of the variable
     */
    public Variable(String name, String qualifiedClassName) {
        this.name = name;
        this.typeInfo = StringUtil.getTypeInfo(qualifiedClassName);
    }

    /**
     * The name of the variable.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * The TypeInfo of the variable.
     *
     * @return the TypeInfo
     */
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "name='" + name + '\'' +
                ", typeInfo=" + typeInfo +
                '}';
    }
}
