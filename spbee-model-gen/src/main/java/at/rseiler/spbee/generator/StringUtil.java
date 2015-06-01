package at.rseiler.spbee.generator;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * Changes the first char to lower case. No other letters are changed.
     *
     * @param value the String to be changed
     * @return the String with the first char as lower case
     */
    public static String firstCharToLowerCase(String value) {
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Transforms the name to a Java class name.
     * Removes all underline chars (_) and change each following char to upper case.
     * E.g.: sp_get_user will be transformed to SpGetUser
     *
     * @param spName the stored procedure name
     * @return the Java class name
     */
    public static String transformToJavaClassName(String spName) {
        if(spName.isEmpty()) {
            return "UNKNOWN";
        }

        StringBuilder sb = new StringBuilder(spName.length());
        sb.append(Character.toUpperCase(spName.charAt(0)));

        for (int i = 1; i < spName.length(); i++) {
            char c = spName.charAt(i);
            if (c != '_') {
                if (spName.charAt(i - 1) == '_') {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }

        return sb.toString();
    }

    /**
     * Transforms the name to a Java variable name.
     * Removes all underline chars (_) and change each following char to upper case.
     * E.g.: sp_get_user will be transformed to spGetUser
     *
     * @param spName the stored procedure name
     * @return the Java class name
     */
    public static String transformToJavaVariableName(String spName) {
        return firstCharToLowerCase(transformToJavaClassName(spName));
    }

}
