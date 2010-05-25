package yabel.attributes;

import yabel.constants.ConstantPool;

import java.io.ByteArrayOutputStream;

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
    protected final int attrId_;


    /**
     * Create a new Attribute of the given named type
     * 
     * @param cp
     *            the constant pool associated with the class this attribute
     *            is in
     * @param attrName
     *            the name of this attribute type
     */
    protected Attribute(ConstantPool cp, String attrName) {
        attrId_ = cp.getUtf8(attrName);
    }


    /**
     * Create a new attribute with the given Id in the constant pool.
     * 
     * @param attrId
     *            the attribute id
     */
    protected Attribute(int attrId) {
        attrId_ = attrId;
    }


    /**
     * Get the Id of the Utf8 constant that holds this attribute's name
     * 
     * @return the id
     */
    int getAttrId() {
        return attrId_;
    }


    /**
     * Write this attribute to the byte array stream. The first element
     * should be a U2 value with the value of <code>attrId_</code>.
     * 
     * @param baos
     *            the stream
     */
    abstract public void writeTo(ByteArrayOutputStream baos);
}