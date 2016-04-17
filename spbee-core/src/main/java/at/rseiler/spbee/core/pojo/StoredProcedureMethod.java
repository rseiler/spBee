package at.rseiler.spbee.core.pojo;

import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.core.annotation.ReturnNull;
import at.rseiler.spbee.core.annotation.RowMapper;
import at.rseiler.spbee.core.annotation.StoredProcedure;
import at.rseiler.spbee.core.pojo.annotation.MappingConstructorData;
import at.rseiler.spbee.core.pojo.annotation.RowMapperData;
import at.rseiler.spbee.core.pojo.annotation.StoredProcedureData;
import at.rseiler.spbee.core.util.ElementConverter;
import at.rseiler.spbee.core.util.StringUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Represents a stored procedure method.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class StoredProcedureMethod implements Serializable {

    private static final long serialVersionUID = 3118412026258156516L;

    private String qualifiedDtoClassName;
    private String methodName;
    private TypeInfo returnTypeInfo;
    private List<AnnotationInfo> annotations = new ArrayList<>();
    private boolean returnNull;
    private StoredProcedureData storedProcedure;
    private RowMapperData rowMapper;
    private MappingConstructorData mappingConstructor;
    private final List<Variable> arguments = new LinkedList<>();

    private StoredProcedureMethod() {
    }

    /**
     * Adds a variable to the stored procedure method.
     *
     * @param argument the argument of the method
     * @return returns itself
     */
    public StoredProcedureMethod addArgument(Variable argument) {
        arguments.add(argument);
        return this;
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

    /**
     * The qualified class name of the stored procedure method.
     *
     * @return the qualified class name
     */
    public String getQualifiedClassName() {
        StringBuilder sb = new StringBuilder(storedProcedure.getName());

        if (mappingConstructor != null) {
            String name = StringUtil.transformToJavaClassName(mappingConstructor.getName());
            sb.append("With").append(name);
        }

        if (rowMapper != null) {
            String name = StringUtil.getSimpleClassName(rowMapper.getType());
            sb.append("With").append(name);
        }

        String spClassName = StringUtil.transformToJavaClassName(sb.toString());
        String spPackage = StringUtil.getPackage(qualifiedDtoClassName);
        return StringUtil.getQualifiedStoredProcedureClassName(spPackage, spClassName);
    }

    /**
     * The package of the of the stored procedure method.
     *
     * @return the package
     */
    public String getPackage() {
        return StringUtil.getPackage(getQualifiedClassName());
    }

    /**
     * The simple class name.
     *
     * @return simple class name
     */
    public String getSimpleClassName() {
        String spName = StringUtil.transformToJavaClassName(getStoredProcedureName());
        StringBuilder sb = new StringBuilder(spName);

        if (mappingConstructor != null) {
            String name = StringUtil.transformToJavaClassName(mappingConstructor.getName());
            sb.append("With").append(name);
        }

        if (rowMapper != null) {
            String name = StringUtil.getSimpleClassName(rowMapper.getType());
            sb.append("With").append(name);
        }

        return sb.toString();
    }

    /**
     * The @StoredProcedure annotation.
     *
     * @return the @StoredProcedure annotation
     */
    public String getStoredProcedureName() {
        return storedProcedure.getName();
    }

    /**
     * Generates the name of the variable name which should be used for the DTO class.
     *
     * @return the field name
     */
    public String getDtoFieldName() {
        return StringUtil.firstCharToLowerCase(getSimpleClassName());
    }

    /**
     * The method name.
     *
     * @return method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * The TypeInfo of the return value of the stored procedure method.;
     *
     * @return the TypeInfo
     */
    public TypeInfo getReturnTypeInfo() {
        return returnTypeInfo;
    }

    /**
     * The arguments of the stored procedure method.
     *
     * @return the arguments
     */
    public List<Variable> getArguments() {
        return arguments;
    }

    /**
     * Returns the qualified class name of the RowMapper.
     *
     * @return the qualified class name
     */
    public String getQualifiedRowMapperClass() {
        if (rowMapper != null) {
            return rowMapper.getType();
        }

        String name = mappingConstructor != null ? mappingConstructor.getName() : "Default";
        String returnType = returnTypeInfo.getGenericType().orElse(returnTypeInfo.getType());
        return StringUtil.getQualifiedMapperClassName(returnType, name);
    }

    /**
     * The annotations of the stored procedure method.
     *
     * @return the annotations
     */
    public List<AnnotationInfo> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return "StoredProcedureMethod{" +
                "qualifiedDtoClassName='" + qualifiedDtoClassName + '\'' +
                ", annotations=" + annotations +
                ", storedProcedure=" + storedProcedure +
                ", rowMapper=" + rowMapper +
                ", returnNull=" + returnNull +
                ", mappingConstructor=" + mappingConstructor +
                ", methodName='" + methodName + '\'' +
                ", returnTypeInfo=" + returnTypeInfo +
                ", arguments=" + arguments +
                '}';
    }

    public static class Builder {

        private final StoredProcedureMethod storedProcedureMethod;

        public Builder() {
            this.storedProcedureMethod = new StoredProcedureMethod();
        }

        public Builder dtoClassName(String dtoClassName) {
            storedProcedureMethod.qualifiedDtoClassName = dtoClassName;
            return this;
        }

        public Builder methodName(String methodName) {
            storedProcedureMethod.methodName = methodName;
            return this;
        }

        public Builder returnTypeInfo(TypeInfo returnTypeInfo) {
            storedProcedureMethod.returnTypeInfo = returnTypeInfo;
            return this;
        }

        public Builder annotations(List<AnnotationMirror> annotations) {
            List<AnnotationInfo> annotationInfos = new LinkedList<>();

            for (AnnotationMirror annotation : annotations) {
                AnnotationInfo annotationInfo = new AnnotationInfo(annotation.getAnnotationType().toString());

                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
                    String simpleName = entry.getKey().getSimpleName().toString();
                    Object value = entry.getValue().getValue();
                    String type = entry.getValue().toString();
                    annotationInfo.addAnnotationValueInfo(ElementConverter.convert(simpleName, value, type));
                }

                annotationInfos.add(annotationInfo);
            }

            storedProcedureMethod.annotations = annotationInfos;
            return this;
        }

        public Builder annotationInfos(List<AnnotationInfo> annotations) {
            storedProcedureMethod.annotations = annotations;
            return this;
        }

        public Builder storedProcedure(StoredProcedure storedProcedure) {
            storedProcedureMethod.storedProcedure = new StoredProcedureData(storedProcedure);
            return this;
        }

        public Builder rowMapper(RowMapper rowMapper) {
            storedProcedureMethod.rowMapper = rowMapper != null ? new RowMapperData(rowMapper) : null;
            return this;
        }

        public Builder returnNull(ReturnNull returnNull) {
            storedProcedureMethod.returnNull = returnNull != null;
            return this;
        }

        public Builder mappingConstructor(MappingConstructor mappingConstructor) {
            storedProcedureMethod.mappingConstructor = mappingConstructor != null ? new MappingConstructorData(mappingConstructor) : null;
            return this;
        }

        public StoredProcedureMethod build() {
            return storedProcedureMethod;
        }

    }

}
