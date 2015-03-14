package at.rseiler.spbee.core.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The annotation processing context. This context holds all collected data.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class AnnotationProcessingContext implements Serializable {

    private static final long serialVersionUID = 1320439073078987215L;

    private final Properties config;
    private final Map<String, ResultSetClass> resultSetsMap;
    private final List<MapperClass> mapperClasses;
    private final List<DtoClass> dtoClasses;

    public AnnotationProcessingContext(Properties config, Map<String, ResultSetClass> resultSetsMap, List<MapperClass> mapperClasses, List<DtoClass> dtoClasses) {
        this.config = config;
        this.resultSetsMap = resultSetsMap;
        this.mapperClasses = mapperClasses;
        this.dtoClasses = dtoClasses;
    }

    /**
     * Returns the spBee config.
     *
     * @return the spBee config
     */
    public Properties getConfig() {
        return config;
    }

    /**
     * Returns the result sets.
     *
     * @return the result sets
     */
    public Map<String, ResultSetClass> getResultSetsMap() {
        return resultSetsMap;
    }

    /**
     * Returns the mapper classes.
     *
     * @return the mapper classes
     */
    public List<MapperClass> getMapperClasses() {
        return mapperClasses;
    }

    /**
     * Returns the DTO classes.
     *
     * @return the DTO classes
     */
    public List<DtoClass> getDtoClasses() {
        return dtoClasses;
    }

    @Override
    public String toString() {
        return "AnnotationProcessingContext{" +
                "config=" + config +
                ", resultSetsMap=" + resultSetsMap +
                ", mapperClasses=" + mapperClasses +
                ", dtoClasses=" + dtoClasses +
                '}';
    }
}
