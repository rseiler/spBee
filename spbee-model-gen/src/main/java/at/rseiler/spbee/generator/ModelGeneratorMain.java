package at.rseiler.spbee.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class ModelGeneratorMain {

    private static final Logger LOG = Logger.getLogger(ModelGeneratorMain.class);

    public static void main(String[] args) throws IOException, SQLException, JClassAlreadyExistsException {
        if (args.length >= 2) {
            AbstractApplicationContext ctx = new FileSystemXmlApplicationContext("context.xml");
            DataSource dataSource = (DataSource) ctx.getBean("dataSource");

//        args = new String[]{"at.willhaben.iad.autowired", "pr_adstructure_readattributes", "67"};

            String[] spArgs = Arrays.copyOfRange(args, 2, args.length);
            String spArgString = String.join(",", spArgs);
            String packageName = args[0];
            String storedProcedureName = args[1];

            new ModelGenerator(dataSource, packageName, storedProcedureName, spArgString).execute().forEach(classData -> {
                String path = "target" + File.separatorChar + classData.getClassPackage().substring(packageName.length() + 1, classData.getClassPackage().length()) + File.separatorChar + classData.getClassName() + ".java";
                try {
                    FileUtils.write(new File(path), classData.getClassBody());
                } catch (IOException e) {
                    LOG.error("Failed to write file: " + classData.getClassName(), e);
                }
            });
        } else {
            usage();
        }
    }

    private static void usage() {
        System.out.println("package storedProcedureName storedProcedureArguments...");
    }
}
