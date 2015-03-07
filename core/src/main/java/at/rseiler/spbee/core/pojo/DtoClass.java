package at.rseiler.spbee.core.pojo;

import at.rseiler.spbee.core.util.StringUtil;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds the information to generate the DTO class.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class DtoClass implements Serializable {

    private static final long serialVersionUID = -7146790314269625019L;

    private final String superQualifiedClassName;
    private final boolean anInterface;
    private final boolean hasDataSourceConstructor;
    private final List<StoredProcedureMethod> storedProcedureMethods = new LinkedList<>();

    /**
     * Constructs a new DtoClass.
     *
     * @param superQualifiedClassName  the qualified class name of the interface or the abstract class.
     * @param anInterface              specifies if it's an interface. Otherwise it's handled as an abstract class.
     * @param hasDataSourceConstructor specifies if the class has an constructor with a DataSource as parameter.
     */
    public DtoClass(String superQualifiedClassName, boolean anInterface, boolean hasDataSourceConstructor) {
        this.superQualifiedClassName = superQualifiedClassName;
        this.anInterface = anInterface;
        this.hasDataSourceConstructor = hasDataSourceConstructor;
    }

    /**
     * Adds stored procedure method to the DTO class.
     *
     * @param method the method which should be added
     */
    public void add(StoredProcedureMethod method) {
        storedProcedureMethods.add(method);
    }

    /**
     * Returns the qualified class name of the interface or the abstract class of which this DTO class is derived.
     *
     * @return the qualified class name
     */
    public String getSuperQualifiedClassName() {
        return superQualifiedClassName;
    }

    /**
     * Returns the qualified class name of the DTO class.
     *
     * @return the qualified class name
     */
    public String getQualifiedClassName() {
        return StringUtil.getQualifiedDtoClassName(superQualifiedClassName);
    }

    /**
     * Returns the package of the DTO class.
     *
     * @return the package
     */
    public String getPackage() {
        return StringUtil.getPackage(getQualifiedClassName());
    }

    /**
     * Returns the simple class name of the DTO class.
     *
     * @return the simple class name
     */
    public String getSimpleClassName() {
        return StringUtil.getSimpleClassName(getQualifiedClassName());
    }

    /**
     * Returns true if the DTO class is derived from an interface or an abstract class.
     *
     * @return true if the DTO class is derived from an interface
     */
    public boolean isAnInterface() {
        return anInterface;
    }

    /**
     * Returns true if the DTO class has a constructor with a DataSource as parameter.
     *
     * @return tru if the DTO class has a constructor with a DataSource as parameter
     */
    public boolean hasDataSourceConstructor() {
        return hasDataSourceConstructor;
    }

    /**
     * Returns all stored procedure methods from the DTO class.
     *
     * @return the stored procedure method.
     */
    public List<StoredProcedureMethod> getStoredProcedureMethods() {
        return storedProcedureMethods;
    }

    @Override
    public String toString() {
        return "DtoClass{" +
                "superQualifiedClassName='" + superQualifiedClassName + '\'' +
                ", anInterface=" + anInterface +
                ", hasDataSourceConstructor=" + hasDataSourceConstructor +
                ", storedProcedureMethods=" + storedProcedureMethods +
                '}';
    }

}
