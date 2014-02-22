package yabel.constants;


import java.io.ByteArrayOutputStream;

import yabel.io.IO;

/**
 * String constant.
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantString extends Constant {
    /** Index of Utf8 constant */
    private final ConstantUtf8 value_;


    /**
     * New string constant
     * 
     * @param cp
     *            constant pool
     * @param value
     *            value
     */
    public ConstantString(ConstantPool cp, ConstantUtf8 value) {
        value_ = value;
        canonicalize(cp);
    }


    /**
     * New string constant
     * 
     * @param cp
     *            constant pool
     * @param value
     *            value
     */
    public ConstantString(ConstantPool cp, String value) {
        this(cp,new ConstantUtf8(cp,value));
    }


    /**
     * Create new String constant.
     * 
     * @param cp
     *            constant pool
     * @param value
     *            value to resolve
     */
    ConstantString(ConstantPool cp, Unresolved value) {
        value_ = cp.validate(value.getValue1(),ConstantUtf8.class);
        index_ = value.index_;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj == null ) return false;
        if( obj == this ) return true;
        if( obj instanceof ConstantString ) {
            ConstantString other = (ConstantString) obj;
            return other.value_.equals(value_);
        }
        return false;
    }


    /**
     * Get the string this constant holds
     * 
     * @return the string
     */
    public ConstantUtf8 getValue() {
        return value_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return value_.hashCode() ^ 8;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":String[ " + value_ + " ]";
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(8);
        IO.writeU2(baos, value_.getIndex());
    }
}