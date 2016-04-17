package at.rseiler.spbee.core;

import at.rseiler.spbee.core.annotation.Dao;
import at.rseiler.spbee.core.annotation.Entity;
import at.rseiler.spbee.core.annotation.ResultSet;
import at.rseiler.spbee.core.collector.DtoCollector;
import at.rseiler.spbee.core.collector.EntityClassCollector;
import at.rseiler.spbee.core.collector.ResultSetCollector;
import at.rseiler.spbee.core.generator.DtoGenerator;
import at.rseiler.spbee.core.generator.MapperGenerator;
import at.rseiler.spbee.core.generator.StoredProcedureGenerator;
import at.rseiler.spbee.core.pojo.AnnotationProcessingContext;
import at.rseiler.spbee.core.pojo.DtoClass;
import at.rseiler.spbee.core.pojo.MapperClass;
import at.rseiler.spbee.core.pojo.ResultSetClass;
import com.sun.codemodel.JClassAlreadyExistsException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.*;

/**
 * The Annotation Processor which will be invoked in the pre-compile phase.
 * <p>
 * Registers for the following annotations:
 * <ul>
 * <li>at.rseiler.spbee.core.annotation.ResultSet</li>
 * <li>at.rseiler.spbee.core.annotation.Entity</li>
 * <li>at.rseiler.spbee.core.annotation.Dao</li>
 * </ul>
 * Analysis the mentioned annotations. Based on the collected data the Java code will be generated.
 * The collected data will be saved to allow partial recompilations.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(value = {
        "at.rseiler.spbee.core.annotation.ResultSet",
        "at.rseiler.spbee.core.annotation.Entity",
        "at.rseiler.spbee.core.annotation.Dao"
})
public class SPBeeAnnotationProcessor extends AbstractProcessor {

    public static final String SPBEE_ANNOTATION_PREFIX = "at.rseiler.spbee.core.annotation";
    private static final String DATA_FILE = "at/rseiler/spbee/context.data";
    private static final String CONFIG = "spbee.properties";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        try {
            if (!annotations.isEmpty()) {
                Properties config = loadConfig();
                AnnotationProcessingContext context = loadPreviousContext(config);
                context = collectData(config, annotations, roundEnvironment, context);
                generateCode(context);
                storeContext(context);
            }
        } catch (RuntimeException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, getStackTrace(e));
        }

        return true;
    }

    private Properties loadConfig() {
        Properties config = new Properties();

        try {
            FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", CONFIG);
            if (new File(resource.toUri()).exists()) {
                config.load(resource.openInputStream());
            }
        } catch (IOException ignore) {
        }

        return config;
    }

    /**
     * Loads the previous context if it exists. Otherwise an empty context is created.
     * The old context is needed for partial recompilations.
     *
     * @return the context
     */
    private AnnotationProcessingContext loadPreviousContext(Properties config) {
        try {
            FileObject resource = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "", DATA_FILE);
            if (new File(resource.toUri()).exists()) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(resource.openInputStream())) {
                    return (AnnotationProcessingContext) objectInputStream.readObject();
                }
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to read file:" + getStackTrace(e));
        } catch (ClassNotFoundException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to deserialize the data file: " + getStackTrace(e));
        }

        return new AnnotationProcessingContext(config, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Collects the data from the source code and stores the data into the context.
     *
     * @param annotations      the annotations
     * @param roundEnvironment the environment
     * @param context          the old context
     * @return the new context
     */
    private AnnotationProcessingContext collectData(Properties config, Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment, AnnotationProcessingContext context) {
        Map<String, ResultSetClass> resultSetsMap = new HashMap<>();
        List<MapperClass> mapperClasses = new ArrayList<>();
        List<DtoClass> dtoClasses = new ArrayList<>();

        for (TypeElement typeElement : annotations) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(typeElement);

            if (typeElement.toString().equals(ResultSet.class.getCanonicalName())) {
                resultSetsMap = new ResultSetCollector(elements).collect().getResultSetMap();
            } else if (typeElement.toString().equals(Entity.class.getCanonicalName())) {
                mapperClasses = new EntityClassCollector(processingEnv, elements).collect().getMapperClasses();
            } else if (typeElement.toString().equals(Dao.class.getCanonicalName())) {
                dtoClasses = new DtoCollector(elements).collect().getDtoClasses();
            }
        }

        for (Map.Entry<String, ResultSetClass> entry : context.getResultSetsMap().entrySet()) {
            resultSetsMap.putIfAbsent(entry.getKey(), entry.getValue());
        }

        context = new AnnotationProcessingContext(config, resultSetsMap, mapperClasses, dtoClasses);
        return context;
    }

    /**
     * Generates the code based on the context.
     *
     * @param context the context
     */
    private void generateCode(AnnotationProcessingContext context) {
        try {
            new MapperGenerator(processingEnv).generateMappers(context.getMapperClasses());
            new StoredProcedureGenerator(processingEnv, context.getResultSetsMap()).generateStoredProcedureClasses(context.getDtoClasses());
            new DtoGenerator(processingEnv, context.getConfig(), context.getResultSetsMap()).generateDtoClasses(context.getDtoClasses());
        } catch (ClassNotFoundException | JClassAlreadyExistsException | IOException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, getStackTrace(e));
        }
    }

    /**
     * Store the context for partial recompilations.
     *
     * @param context the context to store
     */
    private void storeContext(AnnotationProcessingContext context) {
        try {
            FileObject resource = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", DATA_FILE);
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(resource.openOutputStream())) {
                objectOutputStream.writeObject(context);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, getStackTrace(e));
        }
    }

    /**
     * Transform the exception to a string with the complete stack trace.
     *
     * @param exception the exception which should be transformed
     * @return the stack trace of the exception
     */
    private String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        try {
            exception.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } finally {
            try {
                sw.close();
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to print stack-trace.");
            }
        }
    }

}