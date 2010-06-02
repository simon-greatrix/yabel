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

}
