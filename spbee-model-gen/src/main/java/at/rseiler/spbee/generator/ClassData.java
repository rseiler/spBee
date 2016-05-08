package at.rseiler.spbee.generator;

public class ClassData {

    private final String className;
    private final String classPackage;
    private final String classBody;

    public ClassData(String className, String classPackage, String classBody) {
        this.className = className;
        this.classPackage = classPackage;
        this.classBody = classBody;
    }

    public String getClassPackage() {
        return classPackage;
    }

    public String getClassName() {
        return className;
    }

    public String getClassBody() {
        return classBody;
    }

    @Override
    public String toString() {
        return classBody;
    }
}
