package yabel.attributes;

import java.io.ByteArrayOutputStream;

import yabel.ClassData;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;

/**
 * An attribute.
 * 
 * @author Simon Greatrix
 * 
 */
abstract public class Attribute {
    /** The name of the "Code" attribute */
    public static final String ATTR_CODE = "Code";

    /** The name of the "ConstantValue" attribute */
    public static final String ATTR_CONSTANT_VALUE = "ConstantValue";

    /** The name of the "Deprecated" attribute */
    public static final String ATTR_DEPRECATED = "Deprecated";

    /** The name of the "Exceptions" attribute */
    public static final String ATTR_EXCEPTIONS = "Exceptions";

    /** The name of the "InnerClasses" attribute */
    public static final String ATTR_INNER_CLASSES = "InnerClasses";

    /** The name of the "LineNumberTable" attribute */
    public static final String ATTR_LINE_NUMBER_TABLE = "LineNumberTable";

    /** The name of the "LocalVariableTable" attribute */
    public static final String ATTR_LOCAL_VARIABLE_TABLE = "LocalVariableTable";

    /** The name of the "SourceFile" attribute */
    public static final String ATTR_SOURCE_FILE = "SourceFile";

    /** The name of the "Synthetic" attribute */
    public static final String ATTR_SYNTHETIC = "Synthetic";

    /** The index of the attribute name in the ConstantPool */
    protected final ConstantUtf8 attrId_;


    /**
     * Create a new Attribute of the given named type
     * 
     * @param cp
     *            the constant pool associated with the class this attribute is
     *            in
     * @param attrName
     *            the name of this attribute type
     */
    protected Attribute(ConstantPool cp, String attrName) {
        attrId_ = new ConstantUtf8(cp, attrName);
    }


    /**
     * Create a new Attribute of the given named type
     * 
     * @param cp
     *            the constant pool associated with the class this attribute is
     *            in
     * @param cd
     *            the class data specifying the name
     */
    protected Attribute(ConstantPool cp, ClassData cd) {
        String s = cd.get(String.class, "name");
        attrId_ = new ConstantUtf8(cp, s);
    }


    /**
     * Create a new attribute with the given Id in the constant pool.
     * 
     * @param cp
     *            the class's constant pool
     * @param attrId
     *            the attribute id
     */
    protected Attribute(ConstantPool cp, int attrId) {
        attrId_ = cp.validate(attrId, ConstantUtf8.class);
    }


    /**
     * Get the Utf8 constant that holds this attribute's name
     * 
     * @return the id
     */
    ConstantUtf8 getAttrId() {
        return attrId_;
    }


    /**
     * The containing list has changed
     * 
     * @param owner
     *            the containing list
     */
    void listChanged(AttributeList owner) {
    // do nothing
    }


    /**
     * Store this attribute in a ClassData structure
     * 
     * @return the ClassData
     */
    abstract public ClassData toClassData();


    /**
     * Make the initial ClassData structure for this attribute
     * 
     * @return the ClassData
     */
    protected ClassData makeClassData() {
        ClassData cd = new ClassData();
        cd.put("name", attrId_.get());
        return cd;
    }


    /**
     * Write this attribute to the byte array stream. The first element should
     * be a U2 value with the value of <code>attrId_</code>.
     * 
     * @param baos
     *            the stream
     */
    abstract public void writeTo(ByteArrayOutputStream baos);
}