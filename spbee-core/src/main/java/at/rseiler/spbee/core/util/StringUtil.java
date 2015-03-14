package at.rseiler.spbee.core.util;

import at.rseiler.spbee.core.pojo.TypeInfo;

/**
 * Some string util methods.
 *
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
public final class StringUtil {

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
        StringBuilder sb = new StringBuilder(spName.length());
        sb.append(Character.toUpperCase(spName.charAt(0)));

        for (int i = 1; i < spName.length(); i++) {
            char c = spName.charAt(i);
            if (c != '_') {
                if (spName.charAt(i - 1) == '_') {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Create the TypeInfo of a type string.
     *
     * @param type the type string
     * @return the TypeInfo
     */
    public static TypeInfo getTypeInfo(String type) {
        TypeInfo typeInfo;

        if (type.contains("<")) {
            String[] split = type.split("<|>");
            typeInfo = new TypeInfo(split[0], split[1]);
        } else {
            typeInfo = new TypeInfo(type);
        }

        return typeInfo;
    }

    /**
     * Extracts the simple class name of the full qualified class name.
     *
     * @param qualifiedClassName the full qualified class name
     * @return the simple class name
     */
    public static String getSimpleClassName(String qualifiedClassName) {
        return qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
    }

    /**
     * Extracts the package name of the full qualified class name.
     *
     * @param qualifiedClassName the full qualified class name
     * @return the package name
     */
    public static String getPackage(String qualifiedClassName) {
        return qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
    }


    /**
     * Generates the full qualified class name for a RowMapper class.
     *
     * @param qualifiedClassName the full qualified class name
     * @param name               the name of the RowMapper
     * @return the full qualified class name of the RowMapper
     */
    public static String getQualifiedMapperClassName(String qualifiedClassName, String name) {
        return getPackage(qualifiedClassName) + ".mapper." + getSimpleClassName(qualifiedClassName) + transformToJavaClassName(name) + "Mapper";
    }


    /**
     * Generates the full qualified class name for a stored procedure class.
     *
     * @param spPackage   the package of the stored procedure
     * @param spClassName the class name of the stored procedure class name
     * @return the full qualified class name of the stored procedure
     */
    public static String getQualifiedStoredProcedureClassName(String spPackage, String spClassName) {
        return spPackage + ".storedprocedure." + spClassName;
    }

    /**
     * Generates the full qualified class name for a stored procedure class.
     *
     * @param type the type of the DTO
     * @return the full qualified class name fo the DTO
     */
    public static String getQualifiedDtoClassName(String type) {
        return getPackage(type) + "." + getSimpleClassName(type) + "Impl";
    }

}
