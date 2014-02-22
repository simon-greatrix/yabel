package yabel.io;

import java.io.IOException;

/**
 * An object that can write out a ClassData object
 * 
 * @author Simon Greatrix
 * 
 */
public interface ClassDataWriter {
    /**
     * Finish writing.
     * 
     * @throws IOException
     */
    void finish() throws IOException;


    /**
     * Start writing.
     * 
     * @throws IOException
     */
    void start() throws IOException;


    /**
     * Write a simple object.
     * 
     * @param v
     *            the object
     * @throws IOException
     */
    void write(Object v) throws IOException;
}
