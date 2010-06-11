package yabel.constants;


import yabel.io.IO;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A numberical constant. May be integer, float, double, or long.
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantNumber extends Constant {
    /** Tag for the specific numeric type */
    private final int tag_;

    /** The value */
    private final Number value_;


    /**
     * New numeric constant
     * 
     * @param cp
     *            constant pool
     * @param numb
     *            value
     */
    public ConstantNumber(ConstantPool cp, Number numb) {
        value_ = numb;
        int tag;
        if( value_ instanceof Integer ) {
            tag = 3;
        } else if( value_ instanceof Float ) {
            tag = 4;
        } else if( value_ instanceof Long ) {
            tag = 5;
        } else if( value_ instanceof Double ) {
            tag = 6;
        } else {
            throw new IllegalArgumentException("Number cannot be "
                    + value_.getClass());
        }
        tag_ = tag;
        canonicalize(cp);
    }


    /**
     * New numeric constant
     * 
     * @param tag
     *            the constant tag type
     * @param input
     *            the input stream
     * @throws IOException
     */
    ConstantNumber(int tag, InputStream input) throws IOException {
        tag_ = tag;
        switch (tag_) {
        case 3: // integer
            value_ = Integer.valueOf(IO.readS4(input));
            break;
        case 4: // float
            value_ = Float.valueOf(Float.intBitsToFloat(IO.readS4(input)));
            break;
        case 5: // long
            value_ = Long.valueOf(IO.readS8(input));
            break;
        case 6: // double
            value_ = Double.valueOf(Double.longBitsToDouble(IO.readS8(input)));
            break;
        default:
            throw new IOException("Numeric constant tag cannot be " + tag);
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj == null ) return false;
        if( obj == this ) return true;
        if( obj instanceof ConstantNumber ) {
            ConstantNumber other = (ConstantNumber) obj;
            return (other.tag_ == tag_) && other.value_.equals(value_);
        }
        return false;
    }


    /** {@inheritDoc} */
    @Override
    int getPoolSize() {
        return ((tag_ == 5) || (tag_ == 6)) ? 2 : 1;
    }


    /**
     * Get the value of this constant
     * 
     * @return the value
     */
    public Number getValue() {
        return value_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return value_.hashCode() ^ tag_;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":Number(" + tag_ + ")[ " + value_ + " ]";
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        // write tag
        baos.write(tag_);

        switch (tag_) {
        case 3:
            IO.writeS4(baos, value_.intValue());
            break;
        case 4:
            IO.writeS4(baos, Float.floatToRawIntBits(value_.floatValue()));
            break;
        case 5:
            IO.writeS8(baos, value_.longValue());
            break;
        case 6:
            IO.writeS8(baos, Double.doubleToRawLongBits(value_.doubleValue()));
            break;
        }
    }
}