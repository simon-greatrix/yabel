package yabel.constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yabel.io.IO;

/**
 * An unresolved constant. This constant will depend on other constants already
 * having been loaded.
 * 
 * @author Simon Greatrix
 * 
 */
class Unresolved extends Constant {
    /** The tag for this constant */
    private final int tag_;

    /** The first U2 value */
    private final int val1_;

    /** The second U2 value, if it exists */
    private final int val2_;


    /**
     * An unresolved constant.
     * 
     * @param tag
     *            the constant's tag
     * @param input
     *            the input to read from
     * @throws IOException
     */
    Unresolved(int tag, InputStream input) throws IOException {
        tag_ = tag;

        val1_ = IO.readU2(input);

        // Class(7) and String(8) constants have just one value
        if( (tag_ != 7) && (tag_ != 8) ) {
            val2_ = IO.readU2(input);
        } else {
            val2_ = -1;
        }
    }


    /**
     * Resolve this constant.
     * 
     * @param cp
     *            the constant pool
     * @param phase
     *            the phase of resolution (0, 1, or 2)
     * @return the constant or null
     */
    Constant resolve(ConstantPool cp, int phase) {
        Constant c=null;
        switch (phase) {
        case 0: // can resolve Class(7) and String(8) which rely on a single
            // Utf8 constant
            if( tag_ == 7 ) {
                c = new ConstantClass(cp, this);
            } else if( tag_ == 8 ) {
                c = new ConstantString(cp, this);
            }
            break;
        case 1: // can resolve name-and-type (12)
            if( tag_ == 12 ) c = new ConstantNameAndType(cp, this);
            break;
        case 2: // can resolve Field(9), Method(10) and Interface(11)
            if( tag_ == 9 ) {
                c = new ConstantFieldRef(cp, this);
            } else if( tag_ == 10 ) {
                c = new ConstantMethodRef(cp, this);
            } else if( tag_ == 11 ) {
                c = new ConstantInterfaceMethodRef(cp, this);
            }
            break;
        }
        if( c!=null ) c.index_ = index_;
        return c;
    }


    /** {@inheritDoc} */
    public int hashCode() {
        return tag_ ^ index_;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj == null ) return false;
        if( obj == this ) return true;
        if( obj instanceof Unresolved ) {
            Unresolved other = (Unresolved) obj;
            return other.tag_ == tag_ && other.val1_ == val1_
                    && other.val2_ == val2_;
        }
        return false;
    }


    /** {@inheritDoc} */
    public String toString() {
        return "Unresolved[" + tag_ + ", " + val1_ + ", " + val2_ + "]";
    }


    /**
     * Not supported.
     * 
     * @param baos
     *            ignored
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        throw new UnsupportedOperationException();
    }


    public int getValue1() {       
        return val1_;
    }


    public int getValue2() {       
        return val2_;
    }
}
