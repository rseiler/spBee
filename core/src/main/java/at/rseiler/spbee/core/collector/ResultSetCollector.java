package at.rseiler.spbee.core.collector;

import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.core.annotation.ReturnNull;
import at.rseiler.spbee.core.annotation.RowMapper;
import at.rseiler.spbee.core.pojo.ResultSetClass;
import at.rseiler.spbee.core.pojo.ResultSetVariable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Collects all ResultSets which are annotated with {@link at.rseiler.spbee.core.annotation.ResultSet}.
 * <p>
 * <ul>
 * <li>
 * collects the constructors annotated with @MappingConstructor. If no constructor is annotated
 * with @MappingConstructor the public constructor is used.
 * <ul>
 * <li>collects all parameters</li>
 * <li>reads the @MappingConstructor annotation</li>
 * </ul>
 * </li>
 * <li>with this data a {@link at.rseiler.spbee.core.pojo.ResultSetClass} object is created</li>
 * </ul>
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class ResultSetCollector {

    private final Set<? extends Element> elements;
    private final Map<String, ResultSetClass> resultSetMap = new HashMap<>();

    public ResultSetCollector(Set<? extends Element> elements) {
        this.elements = elements;
    }

    public Map<String, ResultSetClass> getResultSetMap() {
        return resultSetMap;
    }

    /**
     * Collects all ResultSets which are annotated with {@link at.rseiler.spbee.core.annotation.ResultSet}.
     *
     * @return itself
     */
    public ResultSetCollector collect() {
        for (Element resultSetsElement : elements) {
            List<ResultSetVariable> types = resultSetsElement.getEnclosedElements().stream()
                    .filter(isField())
                    .map(element -> new ResultSetVariable(element.getSimpleName().toString(), element.asType().toString(), element.getAnnotation(RowMapper.class), element.getAnnotation(ReturnNull.class), element.getAnnotation(MappingConstructor.class)))
                    .collect(Collectors.toList());

            String name = resultSetsElement.toString();
            resultSetMap.put(name, new ResultSetClass(name, types));
        }

        return this;
    }

    /**
     * Checks if the element is a field.
     *
     * @return the predicate
     */
    private Predicate<Element> isField() {
        return element -> element.getKind() == ElementKind.FIELD;
    }

}
