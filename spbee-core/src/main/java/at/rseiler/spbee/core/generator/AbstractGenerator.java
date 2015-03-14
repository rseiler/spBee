package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.util.StringCodeWriter;
import com.sun.codemodel.JCodeModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

/**
 * Base class for the code generators.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public abstract class AbstractGenerator {

    private final ProcessingEnvironment processingEnv;

    public AbstractGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * Generates the Java class based on the parameters.
     *
     * @param model              the code of the class
     * @param qualifiedClassName the name of the class
     * @throws IOException if the the Java class couldn't be created
     */
    public void generateClass(JCodeModel model, String qualifiedClassName) throws IOException {
        StringCodeWriter codeWriter = new StringCodeWriter();
        model.build(codeWriter);
        String javaCode = codeWriter.getJavaCode();

        JavaFileObject f = processingEnv.getFiler().createSourceFile(qualifiedClassName);
        Writer w = f.openWriter();
        w.write(javaCode);
        w.close();
    }

}
