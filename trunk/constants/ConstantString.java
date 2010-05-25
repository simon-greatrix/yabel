package yabel.constants;


import yabel.io.IO;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * String constant.
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantString extends Constant {
    /** The actual string value */
    private String strValue_ = null;

    /** Index of Utf8 constant */
    private final int value_;


    /**
     * New string constant
     * 
     * @param cp
     *            constant pool
     * @param value
     *            value
     */
    public ConstantString(ConstantPool cp, String value) {
        value_ = cp.getUtf8(value);
        strValue_ = value;
        canonicalize(cp);
    }


    /**
     * Create new String constant.
     * 
     * @param in
     *            input stream
     */
    ConstantString(InputStream in) throws IOException {
        value_ = IO.readU2(in);
        strValue_ = null;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj instanceof ConstantString ) {
            ConstantString other = (ConstantString) obj;
            return other.value_ == value_;
        }
        return false;
    }


    /**
     * Get the string this constant holds
     * 
     * @return the string
     */
    public String getValue() {
        return strValue_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return value_ ^ 8;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":String[ " + value_ + " ]";
    }


    /**
     * Referenced constant must be a UTF8 constant
     * 
     * @param cp
     *            the constant pool
     */
    @Override
    void validate(ConstantPool cp) {
        ConstantUtf8 utf8 = cp.validate(value_, ConstantUtf8.class);
        strValue_ = utf8.get();
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(8);
        IO.writeU2(baos, value_);
    }
}