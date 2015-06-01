package at.rseiler.spbee.generator;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Helper class to retrieve the Java code as String from the {@link com.sun.codemodel.JCodeModel}.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public class StringCodeWriter extends CodeWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * Retrieves the Java code as String.
     *
     * @return the Java code
     */
    public String getJavaCode() {
        return new String(out.toByteArray(), Charset.defaultCharset());
    }

    @Override
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        return new InternalFilterOutputStream(out);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private static class InternalFilterOutputStream extends FilterOutputStream {

        public InternalFilterOutputStream(ByteArrayOutputStream out) {
            super(out);
        }

        @Override
        public void close() {
            // don't let this stream close
        }

    }
}
