package yabel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static boolean DEBUG = true;

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


    /**
     * Which bit does a value correspond to?
     * 
     * @param val
     *            the value
     * @return the bit
     */
    private static int bit(int val) {
        int v = val;
        int b = 1;
        for(int i = 0;i < 16;i++) {
            if( b == v ) return i;
            b <<= 1;
        }
        throw new IllegalArgumentException(val
                + " could not be matched to a bit");
    }

    static {
        for(int j = 0;j < ACC_NAMES.length;j++) {
            ACC_NAMES[j] = "(unknown:" + j + ")";
        }
        ACC_NAMES[bit(ACC_ABSTRACT)] = "abstract";
        ACC_NAMES[bit(ACC_FINAL)] = "final";
        ACC_NAMES[bit(ACC_INTERFACE)] = "interface";
        ACC_NAMES[bit(ACC_NATIVE)] = "native";
        ACC_NAMES[bit(ACC_PRIVATE)] = "private";
        ACC_NAMES[bit(ACC_PROTECTED)] = "protected";
        ACC_NAMES[bit(ACC_PUBLIC)] = "public";
        ACC_NAMES[bit(ACC_STATIC)] = "static";
        ACC_NAMES[bit(ACC_STRICT)] = "strictfp";
        ACC_NAMES[bit(ACC_SUPER)] = "super";
        ACC_NAMES[bit(ACC_SYNCH)] = "synchronized";
        ACC_NAMES[bit(ACC_TRANSIENT)] = "transient";
        ACC_NAMES[bit(ACC_VOLATILE)] = "volatile";

        Map<String, Integer> s2i = new HashMap<String, Integer>();
        int k = 1;
        for(int j = 0;j < ACC_NAMES.length;j++) {
            s2i.put(ACC_NAMES[j], Integer.valueOf(k));
            k *= 2;
        }
        ACC_MASKS = Collections.unmodifiableMap(s2i);
    }


    /**
     * Get the textual representation of an access code for a given bit-mask
     * 
     * @param m
     *            the bit mask
     * @return the access representation
     */
    public static String accessCode(int m) {
        if( m == 0 ) return "";
        StringBuilder buf = new StringBuilder();
        for(int i = 0;i < ACC_NAMES.length;i++) {
            if( (m & 1) != 0 ) {
                buf.append(' ').append(ACC_NAMES[i]);
            }
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
        s = s.trim();
        if( s.equals("") ) return 0;
        int i = 0;
        String[] sp = s.split("\\s+");
        for(String l:sp) {
            Integer m = ACC_MASKS.get(l);
            if( m == null )
                throw new IllegalArgumentException(
                        "Unknown access modifier: \"" + l + "\" in \"" + s
                                + "\"");
            i += m.intValue();
        }
        return i;
    }


    /** Class access modifier */
    private int access_;

    /** The attribute list for this class */
    private final AttributeList attrList_;

    /** Constant pool for this class */
    private final ConstantPool cp_;

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
     * Create a ClassBuilder from its ClassData representation
     * 
     * @param data
     *            the representation
     */
    public ClassBuilder(ClassData data) {
        cp_ = new ConstantPool(this);
        version_ = data.get(Integer.class, "version",
                Integer.valueOf(JAVA_VERSION_1_1)).intValue();
        access_ = accessCode(data.get(String.class, "access", "")) | ACC_SUPER;

        thisClass_ = new ConstantClass(cp_, data.getSafe(String.class, "name"));
        superClass_ = new ConstantClass(cp_, data.get(String.class, "super",
                "Ljava/lang/Object;"));

        List<String> ifaces = data.getList(String.class, "interfaces");
        if( ifaces != null ) {
            for(String s:ifaces) {
                interfaces_.add(new ConstantClass(cp_, s));
            }
        }

        List<ClassData> fields = data.getList(ClassData.class, "fields");
        if( fields != null ) {
            for(ClassData d:fields) {
                fields_.add(new Field(cp_, d));
            }
        }

        List<ClassData> meths = data.getList(ClassData.class, "methods");
        if( meths != null ) {
            for(ClassData d:meths) {
                methods_.add(new Method(this, d));
            }
        }

        attrList_ = new AttributeList(cp_, data.getList(ClassData.class,
                "attributes"));
    }


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
        cp_ = new ConstantPool(this,input);

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
        access_ = access | ACC_SUPER;
        cp_ = new ConstantPool(this);
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


    public int getAccess() {
        return access_ & ~ACC_SUPER;
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
            if( (nameId == m.getName().getIndex())
                    && (typeId == m.getType().getIndex()) ) return m;
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
     * Convert the class into a ClassData representation.
     * 
     * @return the class data representation
     */
    public ClassData toClassData() {
        ClassData cd = new ClassData();
        cd.put("access", accessCode(access_ & ~ACC_SUPER));
        cd.put("name", thisClass_.getClassName().get());
        cd.put("super", superClass_.getClassName().get());
        cd.put("version", Integer.valueOf(version_));

        // add interfaces
        List<String> ifaces = new ArrayList<String>(interfaces_.size());
        for(ConstantClass cc:interfaces_) {
            ifaces.add(cc.getClassName().get());
        }
        cd.putList(String.class, "interfaces", ifaces);

        // add fields
        List<ClassData> fields = new ArrayList<ClassData>(fields_.size());
        for(Field f:fields_) {
            fields.add(f.toClassData());
        }
        cd.putList(ClassData.class, "fields", fields);

        // add methods
        List<ClassData> meths = new ArrayList<ClassData>(methods_.size());
        for(Method m:methods_) {
            meths.add(m.toClassData());
        }
        cd.putList(ClassData.class, "methods", meths);

        // and the attributes
        cd.putList(ClassData.class, "attributes", attrList_.toClassData());

        return cd;
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
