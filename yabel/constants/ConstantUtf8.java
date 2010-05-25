package yabel.constants;

import yabel.io.IO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

/**
 * Utf8 constant.
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantUtf8 extends Constant {
    /** String value for this constant */
    private final String value_;


    /**
     * New Utf8 constant.
     * 
     * @param cp
     *            the constant pool
     * @param val
     *            the value
     */
    public ConstantUtf8(ConstantPool cp, String val) {
        value_ = val;
        canonicalize(cp);
    }


    /**
     * Create a Utf8 constant.
     * 
     * @param cp
     *            the constant pool
     * @param val
     *            the value
     * @param create
     *            if true, create into the pool. If false and not in the
     *            pool the index will be -1.
     */
    ConstantUtf8(ConstantPool cp, String val, boolean create) {
        value_ = val;
        if( create ) {
            canonicalize(cp);
        } else {
            Constant c = cp.getCanon(this);
            index_ = (c == null) ? -1 : c.index_;
        }
    }


    /**
     * New Utf8 constant.
     * 
     * @param input
     *            stream to read from
     */
    ConstantUtf8(InputStream input) throws IOException {
        int utflen = IO.readU2(input);
        char[] chararr = new char[utflen];

        int pos = 0;
        int b = 0;
        int ch = 0;
        for(int i = 0;i < utflen;i++) {
            int r = input.read();
            if( r < 0 )
                throw new UTFDataFormatException("Only " + i + " bytes of "
                        + utflen + " were read");
            if( b == 0 ) {
                if( r < 0x80 ) {
                    // byte is 0xxx xxxx so single byte character
                    ch = r;
                } else if( r < 0xc0 ) {
                    // no character can start 10xx xxxx so must be less than
                    // 1100 000 (c0)
                    throw new UTFDataFormatException(
                            "Invalid character start " + r);
                } else if( r < 0xe0 ) {
                    // byte is 110x xxxx so double byte
                    b = 1;
                    ch = (r & 0x1f);
                } else if( r < 0xf0 ) {
                    // byte is 1110 xxxx so triple byte
                    b = 2;
                    ch = (r & 0x0f);
                } else {
                    // no character can start 1111 xxxx (f0)
                    throw new UTFDataFormatException(
                            "Invalid character start " + r);
                }
            } else {
                b--;
                if( (0x80 <= r) && (r < 0xc0) ) {
                    // additional bytes must be in range 1000 0000 to 1011
                    // 1111 (bf)
                    ch = (ch << 6) + (r & 0x3f);
                } else {
                    throw new UTFDataFormatException(
                            "Invalid character additional byte " + r);
                }
            }

            if( b == 0 ) {
                chararr[pos] = (char) ch;
                pos++;
            }
        }

        value_ = new String(chararr, 0, pos);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj instanceof ConstantUtf8 ) {
            return value_.equals(((ConstantUtf8) obj).value_);
        }
        return false;
    }


    /**
     * Get this Utf8 constants value.
     * 
     * @return the value
     */
    public String get() {
        return value_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return value_.hashCode();
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":Utf8[ " + value_ + " ]";
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        int len = 0;
        for(int i = 0;i < value_.length();i++) {
            char c = value_.charAt(i);
            if( c == 0 ) {
                len += 2;
            } else if( c <= 0x7f ) {
                len += 1;
            } else if( c <= 0x7fff ) {
                len += 2;
            } else {
                len += 3;
            }
        }

        // write tag
        baos.write(1);

        // write length
        baos.write(len >> 8);
        baos.write(len & 0xff);

        // write chars
        for(int i = 0;i < value_.length();i++) {
            char c = value_.charAt(i);
            if( c == 0 ) {
                baos.write(0xc0);
                baos.write(0x80);
            } else if( c <= 0x7f ) {
                baos.write(c);
            } else if( c <= 0x7fff ) {
                baos.write(0xc0 | (c >> 6));
                baos.write(0x80 | (c & 0x3f));
            } else {
                baos.write(0xe0 | (c >> 12));
                baos.write(0x80 | ((c >> 6) & 0x3f));
                baos.write(0x80 | (c & 0x3f));
            }
        }
    }
}