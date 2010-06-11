package yabel.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * General purpose methods for read and writing byte-code.
 * 
 * @author Simon Greatrix
 * 
 */
public class IO {

    /** The 6-bit block to use for each character. */
    private final static byte[] BASE64BLOCKS = new byte[127];

    /** The character to use for each 6-bit block */
    private final static char[] BASE64CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    /** The pad character for 6-bit encoding */
    private final static char BASE64PAD = '=';

    /** Initialise the BASE64BLOCKS */
    static {
        for(int i = 0;i < 127;i++) {
            BASE64BLOCKS[i] = (byte) -1;
        }
        for(int i = 0;i < 64;i++) {
            BASE64BLOCKS[BASE64CHARS[i]] = (byte) i;
        }
    }


    /**
     * Decode the provided textual representation back into binary data.
     * 
     * @param text
     *            textual representation
     * @return binary data
     */
    public static byte[] decode(String text) {
        if( text == null ) return null;

        text = removeWhitespace(text);

        // we should have an exact multiple of 4 characters
        int len = text.length();
        if( (len & 0x3) != 0 )
            throw new IllegalArgumentException(
                    "Input text is not Base64 encoded (wrong length): " + text);

        if( len == 0 ) return new byte[0];

        // We need to work out how many bytes we have. Every 4 characters
        // represents 3 bytes, except the last 4, which may look like:
        // VV== -> one byte in last 4 characters
        // VVV= -> two bytes in last 4 characters
        // VVVV -> three bytes in last 4 characters

        if( text.charAt(len - 1) == BASE64PAD ) {
            if( text.charAt(len - 2) == BASE64PAD ) {
                // two pad characters, so just 1 byte in last 3
                len = 3 * (len >> 2) - 2;
            } else {
                // one pad characters, so 2 byte in last 3
                len = 3 * (len >> 2) - 1;
            }
        } else {
            // no pad characters, so 3 bytes in last 3
            len = 3 * (len >> 2);
        }

        byte[] data = new byte[len];

        // extract data from text
        int v = 0; // current 24 bit block
        int j = 0; // current byte within 24 bit block
        int k = 0; // position in text
        for(int i = 0;i < len;i++) {
            switch (j) {
            case 0:
                v = getBlock64(text.charAt(k));
                v = (v << 6) + getBlock64(text.charAt(k + 1));
                v = (v << 6) + getBlock64(text.charAt(k + 2));
                v = (v << 6) + getBlock64(text.charAt(k + 3));
                k = k + 4;
                data[i] = (byte) ((v & 0xff0000) >> 16);
                j = 1;
                break;
            case 1:
                data[i] = (byte) ((v & 0x00ff00) >> 8);
                j = 2;
                break;
            case 2:
                data[i] = (byte) ((v & 0x0000ff) >> 0);
                j = 0;
                break;
            }
        }

        return data;
    }


    /**
     * Encode the provided binary data in a textual form.
     * 
     * @param bytes
     *            binary data
     * @return textual representation
     */
    public static String encode(byte[] bytes) {
        // every three bytes requires 4 characters of output
        int fullBlocks = bytes.length / 3;
        int extraBytes = bytes.length - 3 * fullBlocks;

        char[] output = new char[4 * fullBlocks + ((extraBytes == 0) ? 0 : 4)];

        for(int i = 0;i < fullBlocks;i++) {
            int j = i * 3;
            getBlock64(output, i * 4, bytes[j], bytes[j + 1], bytes[j + 2]);
        }

        int i = fullBlocks * 4;
        int j = fullBlocks * 3;
        switch (extraBytes) {
        case 0:
            break;
        case 1:
            getBlock64(output, i, bytes[j], (byte) 0, (byte) 0);
            output[i + 2] = BASE64PAD;
            output[i + 3] = BASE64PAD;
            break;
        case 2:
            getBlock64(output, i, bytes[j], bytes[j + 1], (byte) 0);
            output[i + 3] = BASE64PAD;
            break;
        }

        return new String(output);
    }


    /**
     * Get the bit pattern represented by a given Base64 character.
     * 
     * @param c
     *            character to decode
     * @return bit pattern as byte
     */
    private final static byte getBlock64(char c) {
        if( c == BASE64PAD ) return 0;

        byte b = (byte) -1;
        if( c < 127 ) b = BASE64BLOCKS[c];
        if( b == -1 )
            throw new IllegalArgumentException("Character 0x"
                    + Integer.toHexString(c)
                    + " is not a valid Base64 character");
        return b;
    }


    private final static void getBlock64(char[] output, int offset, byte b0,
            byte b1, byte b2) {
        int v = ((0xff & b0) << 16) | ((0xff & b1) << 8) | (0xff & b2);
        output[offset] = BASE64CHARS[((0xfc0000) & v) >> 18];
        output[offset + 1] = BASE64CHARS[((0x03f000) & v) >> 12];
        output[offset + 2] = BASE64CHARS[((0x000fc0) & v) >> 6];
        output[offset + 3] = BASE64CHARS[((0x00003f) & v) >> 0];
    }


    /**
     * Read a signed short
     * 
     * @param buf
     *            the buffer holding the data
     * @param loc
     *            where the data is
     * @return the value
     */
    public static int readS2(byte[] buf, int loc) {
        return (buf[loc] << 8) | (0xff & buf[loc + 1]);
    }


    /**
     * Read a signed int
     * 
     * @param buf
     *            the buffer holding the data
     * @param loc
     *            where the data is
     * @return the value
     */
    public static int readS4(byte[] buf, int loc) {
        return ((buf[loc] & 0xff) << 24) + ((buf[loc + 1] & 0xff) << 16)
                + ((buf[loc + 2] & 0xff) << 8) + (buf[loc + 3] & 0xff);
    }


    /**
     * Read a 4 byte value from stream
     * 
     * @param in
     *            input stream
     * @return value
     */
    public static int readS4(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if( (ch1 | ch2 | ch3 | ch4) < 0 ) throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }


    /**
     * Read an 8-byte value from stream
     * 
     * @param in
     *            input stream
     * @return value
     */
    public static long readS8(InputStream in) throws IOException {
        long v = 0;
        for(int i = 0;i < 8;i++) {
            int ch = in.read();
            if( ch < 0 ) throw new EOFException();
            v = (v << 8) + (ch & 0xff);
        }
        return v;
    }


    /**
     * Read a single unsigned byte
     * 
     * @param buf
     *            the buffer holding the byte
     * @param loc
     *            where the data is
     * @return the value
     */
    public static int readU1(byte[] buf, int loc) {
        return 0xff & buf[loc];
    }


    /**
     * Read a 1 byte value from stream
     * 
     * @param in
     *            input stream
     * @return value
     */
    public static int readU1(InputStream in) throws IOException {
        int ch1 = in.read();
        if( ch1 < 0 ) throw new EOFException();
        return ch1;
    }


    /**
     * Read an unsigned short
     * 
     * @param buf
     *            the buffer holding the data
     * @param loc
     *            where the data is
     * @return the value
     */
    public static int readU2(byte[] buf, int loc) {
        return ((0xff & buf[loc]) << 8) + (0xff & buf[loc + 1]);
    }


    /**
     * Read a 2 byte value from stream
     * 
     * @param in
     *            input stream
     * @return value
     */
    public static int readU2(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if( (ch1 | ch2) < 0 ) throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }


    /**
     * Remove all whitespace from an encoded form.
     * 
     * @param text
     *            encoded form
     * @return encoded form without whitespace
     */
    public static String removeWhitespace(String text) {
        StringBuffer buf = new StringBuffer(text.length());
        for(int i = 0;i < text.length();i++) {
            char ch = text.charAt(i);
            if( Character.isWhitespace(ch) ) continue;
            buf.append(ch);
        }
        return buf.toString();
    }


    /**
     * Write a 4 byte value to stream
     * 
     * @param baos
     *            output stream
     * @param i
     *            value
     */
    public static void writeS4(ByteArrayOutputStream baos, int i) {
        baos.write((i >> 24) & 0xff);
        baos.write((i >> 16) & 0xff);
        baos.write((i >> 8) & 0xff);
        baos.write(i & 0xff);
    }


    /**
     * Write 8-byte value to stream
     * 
     * @param baos
     *            output stream
     * @param i
     *            value
     */
    public static void writeS8(ByteArrayOutputStream baos, long i) {
        baos.write((int) ((i >> 56) & 0xff));
        baos.write((int) ((i >> 48) & 0xff));
        baos.write((int) ((i >> 40) & 0xff));
        baos.write((int) ((i >> 32) & 0xff));
        baos.write((int) ((i >> 24) & 0xff));
        baos.write((int) ((i >> 16) & 0xff));
        baos.write((int) ((i >> 8) & 0xff));
        baos.write((int) ((i) & 0xff));
    }


    /**
     * Write a 1 byte value to stream
     * 
     * @param baos
     *            output stream
     * @param i
     *            value
     */
    public static void writeU1(ByteArrayOutputStream baos, int i) {
        baos.write(i & 0xff);
    }


    /**
     * Write a 2 byte value to stream
     * 
     * @param baos
     *            output stream
     * @param i
     *            value
     */
    public static void writeU2(ByteArrayOutputStream baos, int i) {
        baos.write((i >> 8) & 0xff);
        baos.write(i & 0xff);
    }

}
