package at.rseiler.spbee.core.collector;

import at.rseiler.spbee.core.SPBeeAnnotationProcessor;
import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.core.annotation.ReturnNull;
import at.rseiler.spbee.core.annotation.RowMapper;
import at.rseiler.spbee.core.annotation.StoredProcedure;
import at.rseiler.spbee.core.pojo.DtoClass;
import at.rseiler.spbee.core.pojo.StoredProcedureMethod;
import at.rseiler.spbee.core.pojo.StoredProcedureMethod.Builder;
import at.rseiler.spbee.core.pojo.Variable;
import at.rseiler.spbee.core.util.StringUtil;
import com.sun.codemodel.JClassAlreadyExistsException;

import javax.lang.model.element.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Collects all DTO classes and searches for all methods which are annotated with {@link at.rseiler.spbee.core.annotation.StoredProcedure}.
 * <p>
 * <ul>
 * <li>gets all methods annotated with @StoredProcedure
 * <ul>
 * <li>extracts the method name</li>
 * <li>extracts the return type</li>
 * <li>collects all parameters</li>
 * <li>collects all non spBee annotations</li>
 * <li>reads the @StoredProcedure annotation</li>
 * <li>reads the @ReturnNull annotation</li>
 * <li>reads the @MappingConstructor annotation</li>
 * </ul>
 * </li>
 * <li>with this data a {@link at.rseiler.spbee.core.pojo.DtoClass} object is created</li>
 * </ul>
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class DtoCollector {

    private final Set<? extends Element> elements;
    private List<DtoClass> dtoClasses = new ArrayList<>();

    public DtoCollector(Set<? extends Element> elements) {
        this.elements = elements;
    }

    /**
     * The DTO classes.
     *
     * @return the list of DTO classes
     */
    public List<DtoClass> getDtoClasses() {
        return dtoClasses;
    }

    /**
     * Collects all DTO classes and searches for all methods which are annotated with {@link at.rseiler.spbee.core.annotation.StoredProcedure}.
     *
     * @return itself
     * @throws JClassAlreadyExistsException
     * @throws IOException
     */
    public DtoCollector collect() throws JClassAlreadyExistsException, IOException {
        for (Element element : elements) {
            boolean hasDataSourceConstructor = element.getEnclosedElements().stream()
                    .anyMatch(hasDataSourceConstructor());

            DtoClass dtoClass = new DtoClass(element.toString(), element.getKind().isInterface(), hasDataSourceConstructor);
            dtoClasses.add(dtoClass);

            element.getEnclosedElements().stream()
                    .filter(isStoredProcedureMethod())
                    .forEach(collectDtoMethod(dtoClass));
        }

        return this;
    }

    /**
     * Checks if the element is an constructor and has a DataSource as parameter.
     *
     * @return the predicate
     */
    private Predicate<Element> hasDataSourceConstructor() {
        return element -> {
            if(element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement executableElement = (ExecutableElement) element;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                return parameters.size() == 1 && "javax.sql.DataSource".equals(parameters.get(0).asType().toString());
            }

            return false;
        };
    }

    /**
     * Checks if the element is a method and it's annotated with {@link at.rseiler.spbee.core.annotation.StoredProcedure}.
     *
     * @return the predicate
     */
    private Predicate<Element> isStoredProcedureMethod() {
        return element -> element.getKind() == ElementKind.METHOD && element.getAnnotation(StoredProcedure.class) != null;
    }

    /**
     * Checks if the method is annotated with {@link at.rseiler.spbee.core.annotation.StoredProcedure}.
     * If it is than the method is added to the DtoClass.
     *
     * @param dtoClass the DTO class to which the method belongs
     */
    private Consumer<Element> collectDtoMethod(DtoClass dtoClass) {
        return element -> {
            ExecutableElement executableElement = (ExecutableElement) element;
            StoredProcedureMethod storedProcedureMethod = new Builder()
                    .dtoClassName(dtoClass.getQualifiedClassName())
                    .methodName(executableElement.getSimpleName().toString())
                    .returnTypeInfo(StringUtil.getTypeInfo(executableElement.getReturnType().toString()))
                    .annotations(getAnnotations(executableElement))
                    .storedProcedure(executableElement.getAnnotation(StoredProcedure.class))
                    .rowMapper(executableElement.getAnnotation(RowMapper.class))
                    .returnNull(executableElement.getAnnotation(ReturnNull.class))
                    .mappingConstructor(executableElement.getAnnotation(MappingConstructor.class))
                    .build();

            for (VariableElement parameter : executableElement.getParameters()) {
                storedProcedureMethod.addArgument(getVariable(parameter));
            }

            dtoClass.add(storedProcedureMethod);
        };
    }

    /**
     * Gets all non spBee related annotations.
     *
     * @param executableElement the element
     * @return the list of annotations
     */
    private List<AnnotationMirror> getAnnotations(ExecutableElement executableElement) {
        return executableElement.getAnnotationMirrors().stream()
                .filter(annotationMirror -> !annotationMirror.getAnnotationType().toString().startsWith(SPBeeAnnotationProcessor.SPBEE_ANNOTATION_PREFIX))
                .collect(Collectors.toList());
    }

    /**
     * Converts a VariableElement object to a  Variable object.
     *
     * @param variableElement the variable element
     * @return the variable
     */
    private Variable getVariable(VariableElement variableElement) {
        return new Variable(variableElement.getSimpleName().toString(), variableElement.asType().toString());
    }

}
