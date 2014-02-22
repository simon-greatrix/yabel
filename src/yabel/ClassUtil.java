package yabel;

/**
 * 
 * @author Simon Greatrix
 */
public class ClassUtil {
    /**
     * Get the Java class for the given type name.
     * 
     * @param name
     *            the class type
     * @return the Class
     */
    public static Class<?> getClass(String name) {
        // handle primitives
        if( name.equals("B") ) return Byte.TYPE;
        if( name.equals("C") ) return Character.TYPE;
        if( name.equals("D") ) return Double.TYPE;
        if( name.equals("F") ) return Float.TYPE;
        if( name.equals("I") ) return Integer.TYPE;
        if( name.equals("J") ) return Long.TYPE;
        if( name.equals("S") ) return Short.TYPE;
        if( name.equals("V") ) return Void.TYPE;

        // must be an array or an object
        try {
            if( name.startsWith("L") && name.endsWith(";") ) {
                name = name.substring(1, name.length() - 1).replace('/', '.');
            } else if( !name.startsWith("[") ) {
                throw new YabelException("Class name is not recognised :"
                        + name);
            }

            // load via context class loader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return Class.forName(name, false, cl);
        } catch (ClassNotFoundException cnfe) {
            throw new YabelException("Class not loadable :" + name, cnfe);
        }
    }


    /**
     * Get the Java name for a type
     * 
     * @param name
     *            the type
     * @return the Java name
     */
    public static String getJavaName(String name) {
        if( name.startsWith("[") )
            return getJavaName(name.substring(1)) + "[]";
        if( name.equals("B") ) return "byte";
        if( name.equals("C") ) return "char";
        if( name.equals("D") ) return "double";
        if( name.equals("F") ) return "float";
        if( name.equals("I") ) return "int";
        if( name.equals("J") ) return "long";
        if( name.equals("S") ) return "short";
        if( name.equals("V") ) return "void";

        // must be a class
        assert name.startsWith("L") && name.endsWith(";") : "Class type does not match L...;";

        name = name.substring(1, name.length() - 1);
        return name.replace('/', '.');
    }


    /**
     * Get the internal name of a class
     * 
     * @param cls
     *            the class
     * @return the internal name
     */
    public static String getTypeName(Class<?> cls) {
        if( cls.equals(Byte.TYPE) ) return "B";
        if( cls.equals(Character.TYPE) ) return "C";
        if( cls.equals(Double.TYPE) ) return "D";
        if( cls.equals(Float.TYPE) ) return "F";
        if( cls.equals(Integer.TYPE) ) return "I";
        if( cls.equals(Long.TYPE) ) return "J";
        if( cls.equals(Short.TYPE) ) return "S";
        if( cls.equals(Void.TYPE) ) return "V";
        if( cls.isArray() ) return cls.getName();

        String nm = cls.getName();
        nm = nm.replace('.', '/');
        return "L" + nm + ";";
    }


    /**
     * Get the type name for a Java name
     * 
     * @param name
     *            the java name
     * @return the internal name
     */
    public static String getTypeName(String name) {
        if( name.endsWith("[]") )
            return "[" + getTypeName(name.substring(0, name.length() - 2));
        if( name.equals("byte") ) return "B";
        if( name.equals("char") ) return "C";
        if( name.equals("double") ) return "D";
        if( name.equals("float") ) return "F";
        if( name.equals("int") ) return "I";
        if( name.equals("long") ) return "J";
        if( name.equals("short") ) return "S";
        if( name.equals("void") ) return "V";

        return "L" + name.replace('.', '/') + ";";
    }
}
