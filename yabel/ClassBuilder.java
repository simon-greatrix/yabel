package yabel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import yabel.attributes.AttributeList;
import yabel.constants.ConstantClass;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;
import yabel.io.IO;

/**
 * <p>
 * This class provides a way to create or amend the byte-code for a class. The
 * basic process for creating a class is:
 * </p>
 * 
 * <ol>
 * <li>Create a ClassBuilder instance for the class
 * <li>Add the fields to the class using <code>addField</code>.
 * <li>Add a method using <code>addMethod</code>
 * <li>Get the Code attribute from the Method.
 * <li>Call <code>compile</code> on the Code to build the method
 * <li>Set exception handlers for method
 * <li>Add more methods
 * <li>Get class for bytes
 * </ol>
 * 
 * For more information on the JVM class file structure and op-code mnemonics,
 * please see <a href="http://java.sun.com/docs/books/jvms/">the JVM
 * specification</a>.
 * 
 * @author Simon Greatrix
 * 
 */
public class ClassBuilder {

    /** JVM access modifier */
    public static final int ACC_ABSTRACT = 0x0400;

    /** JVM access modifier */
    public static final int ACC_FINAL = 0x0010;

    /** JVM access modifier */
    public static final int ACC_INTERFACE = 0x0200;

    /** Map of access names to their masks */
    private static final Map<String, Integer> ACC_MASKS;

    /** Map of access masks to their names */
    private static final String[] ACC_NAMES = new String[16];

    /** JVM access modifier */
    public static final int ACC_NATIVE = 0x0100;

    /** JVM access modifier */
    public static final int ACC_PRIVATE = 0x0002;

    /** JVM access modifier */
    public static final int ACC_PROTECTED = 0x0004;

    /** JVM access modifier */
    public static final int ACC_PUBLIC = 0x0001;

    /** JVM access modifier */
    public static final int ACC_STATIC = 0x0008;

    /** JVM access modifier */
    public static final int ACC_STRICT = 0x0800;

    /** JVM access modifier */
    public static final int ACC_SUPER = 0x0020;

    /** JVM access modifier */
    public static final int ACC_SYNCH = 0x0020;

    /** JVM access modifier */
    public static final int ACC_TRANSIENT = 0x0080;

    /** JVM access modifier */
    public static final int ACC_VOLATILE = 0x0040;

    /**
     * If set to true, produce more output
     * 
     * @internal
     */
    public static boolean DEBUG = false;

    /** Version code for Java 1.1 */
    public static final int JAVA_VERSION_1_1 = 0x0003002d;

    /** Version code for Java 1.2 */
    public static final int JAVA_VERSION_1_2 = 0x0000002e;

    /** Version code for Java 1.3 */
    public static final int JAVA_VERSION_1_3 = 0x0000002f;

    /** Version code for Java 1.4 */
    public static final int JAVA_VERSION_1_4 = 0x00000030;

    /** Version code for Java 1.5 */
    public static final int JAVA_VERSION_1_5 = 0x00000031;

    /** Version code for Java 1.6 */
    public static final int JAVA_VERSION_1_6 = 0x00000032;

    static {
        Map<String, Integer> s2i = new HashMap<String, Integer>();

        String s = "abstract";
        Integer i = Integer.valueOf(ACC_ABSTRACT);
        s2i.put(s, i);

        s = "final";
        i = Integer.valueOf(ACC_FINAL);
        s2i.put(s, i);

        s = "interface";
        i = Integer.valueOf(ACC_INTERFACE);
        s2i.put(s, i);

        s = "native";
        i = Integer.valueOf(ACC_NATIVE);
        s2i.put(s, i);

        s = "private";
        i = Integer.valueOf(ACC_PRIVATE);
        s2i.put(s, i);

        s = "protected";
        i = Integer.valueOf(ACC_PROTECTED);
        s2i.put(s, i);

        s = "public";
        i = Integer.valueOf(ACC_PUBLIC);
        s2i.put(s, i);

        s = "static";
        i = Integer.valueOf(ACC_STATIC);
        s2i.put(s, i);

        s = "strictfp";
        i = Integer.valueOf(ACC_STRICT);
        s2i.put(s, i);

        s = "synchronized";
        i = Integer.valueOf(ACC_SYNCH);
        s2i.put(s, i);

        s = "transient";
        i = Integer.valueOf(ACC_TRANSIENT);
        s2i.put(s, i);

        s = "volatile";
        i = Integer.valueOf(ACC_VOLATILE);
        s2i.put(s, i);

        ACC_MASKS = Collections.unmodifiableMap(s2i);
        for(Entry<String, Integer> e:ACC_MASKS.entrySet()) {
            int j = 0;
            int m = e.getValue().intValue();
            while( (m & 1) == 0 ) {
                m >>= 1;
                j++;
            }
            ACC_NAMES[j] = e.getKey();
        }
    }


    /**
     * Get the textual representation of an access code for a given bit-mask
     * 
     * @param m
     *            the bit mask
     * @return the access representation
     */
    public static String accessCode(int m) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0;i < ACC_NAMES.length;i++) {
            if( (m & 1) != 0 ) buf.append(' ').append(ACC_NAMES[i]);
            m >>= 1;
        }
        return buf.substring(1);
    }


    /**
     * Get the bit-mask for a given access modifier description
     * 
     * @param s
     *            the description
     * @return the bit mask
     */
    public static int accessCode(String s) {
        int i = 0;
        String[] sp = s.split("\\s+");
        for(String l:sp) {
            Integer m = ACC_MASKS.get(l);
            if( m == null )
                throw new IllegalArgumentException("Unknown access modifier: "
                        + l);
            i += m.intValue();
        }
        return i;
    }


    /**
     * How many arguments are needed on the stack for a method? Longs and
     * doubles count as 2 as they take two stack slots.
     * 
     * @param type
     *            the method type
     * @return the number of arguments
     */
    public static int getArgsForType(String type) {
        if( !type.startsWith("(") )
            throw new IllegalArgumentException("No starting '(' : " + type);
        int p = type.lastIndexOf(')');
        if( p == -1 )
            throw new IllegalArgumentException("No closing ')' : " + type);
        type = type.substring(1, p);
        int count = 0;
        boolean isArray = false;
        for(int i = 0;i < type.length();i++) {
            char c = type.charAt(i);
            switch (c) {
            case 'Z': // boolean
                count++;
                break;
            case 'B': // byte
                count++;
                break;
            case 'C': // char
                count++;
                break;
            case 'D': // double
                count += isArray ? 1 : 2;
                break;
            case 'F': // float
                count++;
                break;
            case 'I': // int;
                count++;
                break;
            case 'J': // long
                count += isArray ? 1 : 2;
                break;
            case 'S': // short
                count++;
                break;
            case 'L': // object
                count++;
                p = type.indexOf(';', i);
                if( p == -1 )
                    throw new IllegalArgumentException(
                            "No closing ';' to object name at position " + i
                                    + " : " + type);
                i = p;
                break;
            case '[': // array
                isArray = true;
                break;
            }
            if( c != '[' ) isArray = false;
        }

        return count;
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
        if( cls.isArray() ) return cls.getName();

        String nm = cls.getName();
        nm = nm.replace('.', '/');
        return "L" + nm + ";";
    }

    /** Class access modifier */
    private int access_;

    /** The attribute list for this class */
    private final AttributeList attrList_;

    /** Constant pool for this class */
    private ConstantPool cp_ = new ConstantPool();

    /** Fields for this class */
    private List<Field> fields_ = new ArrayList<Field>();

    /** Interfaces for this class */
    private List<ConstantClass> interfaces_ = new ArrayList<ConstantClass>();

    /** Methods for this class */
    private List<Method> methods_ = new ArrayList<Method>();

    /** Super class */
    private final ConstantClass superClass_;

    /** This class */
    private final ConstantClass thisClass_;

    /** Java version this class is compiled to */
    private int version_ = JAVA_VERSION_1_1;


    /**
     * New ClassBuilder.
     * 
     * @param input
     *            existing class file
     */
    public ClassBuilder(InputStream input) throws IOException {
        int magic = IO.readS4(input);
        if( magic != 0xcafebabe )
            throw new IOException("CAFEBABE header bytes missing");
        version_ = IO.readS4(input);
        cp_ = new ConstantPool(input);

        access_ = IO.readU2(input);

        int ci = IO.readU2(input);
        thisClass_ = cp_.validate(ci, ConstantClass.class);

        ci = IO.readU2(input);
        if( ci > 0 ) {
            superClass_ = cp_.validate(ci, ConstantClass.class);
        } else {
            superClass_ = null;
        }

        int icount = IO.readU2(input);
        for(int i = 0;i < icount;i++) {
            ci = IO.readU2(input);
            ConstantClass co = cp_.validate(ci, ConstantClass.class);
            interfaces_.add(co);
        }

        icount = IO.readU2(input);
        for(int i = 0;i < icount;i++) {
            Field f = new Field(cp_, input);
            fields_.add(f);
        }

        icount = IO.readU2(input);
        for(int i = 0;i < icount;i++) {
            Method m = new Method(this, input);
            methods_.add(m);
        }

        attrList_ = new AttributeList(cp_, input);
    }


    /**
     * New ClassBuilder for a class that is a direct sub-class of
     * java.lang.Object
     * 
     * @param access
     *            access modifiers
     * @param className
     *            class name
     */
    public ClassBuilder(int access, String className) {
        this(access, className, "java/lang/Object");
    }


    /**
     * New ClassBuilder.
     * 
     * @param access
     *            access modifiers
     * @param className
     *            class name
     * @param superName
     *            super class name
     */
    public ClassBuilder(int access, String className, String superName) {
        access_ = access;
        thisClass_ = new ConstantClass(cp_, className);
        superClass_ = new ConstantClass(cp_, superName);
        attrList_ = new AttributeList();
    }


    /**
     * Add a field to this class
     * 
     * @param access
     *            access modifier
     * @param name
     *            name of field
     * @param type
     *            type of field
     */
    public void addField(int access, String name, String type) {
        Field f = new Field(cp_, access, name, type, null);
        if( fields_.contains(f) )
            throw new IllegalArgumentException("Field " + name
                    + " already exists");
        fields_.add(f);
    }


    /**
     * Add an interface to this class
     * 
     * @param name
     *            interface
     */
    public void addInterface(String name) {
        ConstantClass iface = new ConstantClass(cp_, name);
        if( interfaces_.contains(iface) ) return;
        interfaces_.add(iface);
    }


    /**
     * Add a method to this class.
     * 
     * @param access
     *            access modifiers
     * @param name
     *            name of method
     * @param type
     *            type of method
     * @return Method instance
     */
    public Method addMethod(int access, String name, String type) {
        Method m = new Method(this, access, name, type);
        methods_.add(m);
        return m;
    }


    /**
     * Get the attributes for this class.
     * 
     * @return the attributes
     */
    public AttributeList getAttributes() {
        return attrList_;
    }


    /**
     * Get the bytes for this class
     * 
     * @return the bytes
     */
    public byte[] getBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeTo(baos);
        return baos.toByteArray();
    }


    /**
     * The constant pool ID of the class this is building
     * 
     * @return the ID
     */
    public int getClassID() {
        return thisClass_.getIndex();
    }


    /**
     * Get this class's constant pool
     * 
     * @return the constant pool
     */
    public ConstantPool getConstantPool() {
        return cp_;
    }


    /**
     * Get the named field
     * 
     * @param name
     *            the name of the field
     * @return the field, or null
     */
    public Field getField(String name) {
        int id = cp_.getUtf8(name, false);
        for(Field f:fields_) {
            if( id == f.name_.getIndex() ) return f;
        }
        return null;
    }


    /**
     * Get all the fields of this class
     * 
     * @return all the fields
     */
    public Field[] getFields() {
        Field[] ret = new Field[fields_.size()];
        return fields_.toArray(ret);
    }


    /**
     * Get the matching method if it exists
     * 
     * @param name
     *            method name
     * @param type
     *            method type
     * @return the method, or null
     */
    public Method getMethod(String name, String type) {
        int nameId = cp_.getUtf8(name, false);
        int typeId = cp_.getUtf8(type, false);
        for(Method m:methods_) {
            if( (nameId == m.getName().getIndex()) && (typeId == m.getType().getIndex()) ) return m;
        }
        return null;
    }


    /**
     * Get all the methods of this class
     * 
     * @return all the methods
     */
    public Method[] getMethods() {
        Method[] ret = new Method[methods_.size()];
        return methods_.toArray(ret);
    }


    /**
     * Get the name of the class this is building.
     * 
     * @return the class name
     */
    public String getName() {
        return thisClass_.getClassName().get();
    }


    /**
     * Get the name of the class this is building.
     * 
     * @return the class name
     */
    public ConstantUtf8 getNameUtf8() {
        return thisClass_.getClassName();
    }


    /**
     * Set the Java version the compiled class will announce itself compatible
     * with. This should be one of the JAVA_VERSION constants defined in this
     * class.
     * 
     * @param version
     *            Java version for this class
     */
    public void setVersion(int version) {
        version_ = version;
    }


    /**
     * Write this class to the output
     * 
     * @param baos
     *            the output stream
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeS4(baos, 0xcafebabe);
        IO.writeS4(baos, version_);
        cp_.writeTo(baos);
        IO.writeU2(baos, access_);
        IO.writeU2(baos, thisClass_.getIndex());
        IO.writeU2(baos, (superClass_ != null) ? superClass_.getIndex() : 0);

        // output interfaces
        int s = interfaces_.size();
        IO.writeU2(baos, s);
        for(int i = 0;i < s;i++) {
            IO.writeU2(baos, interfaces_.get(i).getIndex());
        }

        // output fields
        s = fields_.size();
        IO.writeU2(baos, s);
        for(int i = 0;i < s;i++) {
            fields_.get(i).writeTo(baos);
        }

        // output methods
        s = methods_.size();
        IO.writeU2(baos, s);
        for(int i = 0;i < s;i++) {
            methods_.get(i).writeTo(baos);
        }

        // output attributes
        attrList_.writeTo(baos);
    }
}
