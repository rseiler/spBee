package at.rseiler.spbee.core.collector;

import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.core.pojo.MapperClass;
import at.rseiler.spbee.core.pojo.Variable;
import at.rseiler.spbee.core.util.StringUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Collects all MappingConstructors of all Entity classes which are annotated with {@link at.rseiler.spbee.core.annotation.Entity}.
 * <ul>
 * <li>
 * collects the constructors annotated with @MappingConstructor.
 * If no constructor is annotated with @MappingConstructor the public constructor is used.
 * <ul>
 * <li>collects all parameters</li>
 * <li>reads the @MappingConstructor annotation</li>
 * </ul>
 * </li>
 * <li>with this data a {@link at.rseiler.spbee.core.pojo.MapperClass} object is created</li>
 * </ul>
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class EntityClassCollector {

    private final ProcessingEnvironment processingEnv;
    private final Set<? extends Element> elements;
    private final List<MapperClass> mapperClasses = new ArrayList<>();

    public EntityClassCollector(ProcessingEnvironment processingEnv, Set<? extends Element> elements) {
        this.processingEnv = processingEnv;
        this.elements = elements;
    }

    /**
     * Returns the MapperClass classes.
     *
     * @return the MapperClass classes
     */
    public List<MapperClass> getMapperClasses() {
        return mapperClasses;
    }

    /**
     * Collects all MappingConstructors of all Entity classes which are annotated with {@link at.rseiler.spbee.core.annotation.Entity}.
     *
     * @return itself
     */
    public EntityClassCollector collect() {
        for (Element entityClassElement : elements) {
            String qualifiedClassName = entityClassElement.toString();
            boolean hasMappingConstructor = hasMappingConstructor(entityClassElement);

            List<Element> elementList = entityClassElement.getEnclosedElements().stream()
                    .filter(isApplicableConstructor(hasMappingConstructor))
                    .collect(Collectors.toList());

            if (hasMappingConstructor || elementList.size() == 1) {
                elementList.forEach(collectMappingConstructor(qualifiedClassName));
            } else if (elementList.isEmpty()) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "No public constructor for the Entity class '" + qualifiedClassName + "'");
            } else if (elementList.size() > 1) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "More than one public constructor and none of these has a @MappingConstructor annotation for '" + qualifiedClassName + "'");
            } else {
                processingEnv.getMessager().printMessage(Kind.ERROR, "No applicable constructor for '" + qualifiedClassName + "'");
            }
        }

        return this;
    }

    /**
     * Checks if the element is a constructor which is is public.
     * If an constructor annotated with {@link at.rseiler.spbee.core.annotation.MappingConstructor} exists then all other
     * constructors without such annotation will be ignored.
     * If no other constructor is annotated with {@link at.rseiler.spbee.core.annotation.MappingConstructor} then the
     * constructor without the {@link at.rseiler.spbee.core.annotation.MappingConstructor} is considered.
     *
     * @param annotatedMappingConstructorExists if an constructor with the {@link at.rseiler.spbee.core.annotation.MappingConstructor} annotation exists
     * @return the predicate
     */
    private Predicate<Element> isApplicableConstructor(boolean annotatedMappingConstructorExists) {
        return element -> {
            boolean valid = !annotatedMappingConstructorExists || element.getAnnotation(MappingConstructor.class) != null;
            return element.getKind() == ElementKind.CONSTRUCTOR && element.getModifiers().contains(Modifier.PUBLIC) && valid;
        };
    }

    /**
     * Collects all mapping constructors.
     *
     * @param qualifiedClassName the qualified class name
     * @return the consumer
     */
    private Consumer<Element> collectMappingConstructor(String qualifiedClassName) {
        return element -> {
            MappingConstructor mappingConstructor = element.getAnnotation(MappingConstructor.class);
            String name = mappingConstructor != null ? mappingConstructor.value() : "Default";
            String qualifiedMapperClassName = StringUtil.getQualifiedMapperClassName(qualifiedClassName, name);
            MapperClass mapperClass = new MapperClass(qualifiedMapperClassName, qualifiedClassName);

            for (VariableElement variableElement : ((ExecutableElement) element).getParameters()) {
                mapperClass.addVariable(new Variable(variableElement.getSimpleName().toString(), variableElement.asType().toString()));
            }

            mapperClasses.add(mapperClass);
        };
    }

    /**
     * Checks if an constructor annotated with {@link at.rseiler.spbee.core.annotation.MappingConstructor} exists.
     *
     * @param entityClassElement the element
     * @return true if an {@link at.rseiler.spbee.core.annotation.MappingConstructor} exists
     */
    private boolean hasMappingConstructor(Element entityClassElement) {
        return entityClassElement.getEnclosedElements().stream()
                .anyMatch(element -> isMappingConstructor(element));
    }

    /**
     * Checks if the elements is annotated with {@link at.rseiler.spbee.core.annotation.MappingConstructor}.
     *
     * @param element the element
     * @return true if is annotated with {@link at.rseiler.spbee.core.annotation.MappingConstructor}
     */
    private boolean isMappingConstructor(Element element) {
        return element.getKind() == ElementKind.CONSTRUCTOR && element.getModifiers().contains(Modifier.PUBLIC) &&
                element.getAnnotation(MappingConstructor.class) != null;
    }

}
