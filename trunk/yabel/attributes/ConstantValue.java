package yabel.attributes;

import yabel.ClassData;
import yabel.io.IO;

import yabel.constants.Constant;
import yabel.constants.ConstantNumber;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A ConstantValue attribute.
 * 
 * @author Simon Greatrix
 */
public class ConstantValue extends Attribute {
    /** The value */
    private final Constant value_;


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, boolean val) {
        this(cp, new ConstantNumber(cp, Integer.valueOf(val ? 1 : 0)));
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, byte val) {
        this(cp, new ConstantNumber(cp, Byte.valueOf(val)));
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, char val) {
        this(cp, new ConstantNumber(cp, Integer.valueOf(val)));
    }


    /**
     * Create a new ConstantValue from its class data representation
     * 
     * @param cp
     *            the constant pool associated with this constant
     * @param cd
     *            the class data
     */
    public ConstantValue(ConstantPool cp, ClassData cd) {
        super(cp, cd);
        Object v = cd.getSafe(Object.class, "value");
        if( v instanceof Number ) {
            value_ = new ConstantNumber(cp, (Number) v);
        } else if( v instanceof String ) {
            value_ = new ConstantString(cp, (String) v);
        } else
            throw new IllegalArgumentException(
                "ConstantValue must be String or Number, not class " + v.getClass());
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    private ConstantValue(ConstantPool cp, Constant val) {
        super(cp, Attribute.ATTR_CONSTANT_VALUE);
        value_ = val;
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, double val) {
        this(cp, new ConstantNumber(cp, Double.valueOf(val)));
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, float val) {
        this(cp, new ConstantNumber(cp, Float.valueOf(val)));
    }


    /**
     * Read a ConstantValue attribute from the input stream
     * 
     * @param cp
     *            the constant pool
     * @param input
     *            the stream
     * @throws IOException
     */
    public ConstantValue(ConstantPool cp, InputStream input) throws IOException {
        super(cp, Attribute.ATTR_CONSTANT_VALUE);
        int len = IO.readS4(input);
        if( len != 2 )
            throw new IOException(
                    "Length of ConstantValue attribute is " + len + " not 2");
        int val = IO.readU2(input);
        value_ = cp.get(val);
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, int val) {
        this(cp, new ConstantNumber(cp, Integer.valueOf(val)));
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, long val) {
        this(cp, new ConstantNumber(cp, Long.valueOf(val)));
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, Number val) {
        this(cp, new ConstantNumber(cp, val));
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, short val) {
        this(cp, new ConstantNumber(cp, Short.valueOf(val)));
    }


    /**
     * New ConstantValue attribute
     * 
     * @param cp
     *            constant pool
     * @param val
     *            value
     */
    public ConstantValue(ConstantPool cp, String val) {
        this(cp, new ConstantString(cp, val));
    }


    /**
     * Get the numeric value of this constant. If this constant is a String
     * type, return null
     * 
     * @return the numeric value or null
     */
    public Number getNumericValue() {
        if( value_ instanceof ConstantNumber ) {
            return ((ConstantNumber) value_).getValue();
        }
        return null;
    }


    /**
     * Get the String value of this constant. If this constant is a numeric
     * type, return null
     * 
     * @return the String value or null
     */
    public String getStringValue() {
        if( value_ instanceof ConstantString ) {
            return ((ConstantString) value_).getValue().get();
        }
        return null;
    }


    /** {@inheritDoc} */
    @Override
    public ClassData toClassData() {
        ClassData cd = makeClassData();
        Object o = getNumericValue();
        if( o == null ) o = getStringValue();
        cd.put("value", o);
        return cd;
    }


    /**
     * Save this attribute to the stream
     * 
     * @param baos
     *            output stream
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrId_.getIndex());
        IO.writeS4(baos, 2);
        IO.writeU2(baos, value_.getIndex());
    }
}