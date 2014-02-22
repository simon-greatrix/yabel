package yabel.parser;

/**
 * Recieve notifications of code parsing
 * 
 * @author Simon Greatrix
 * 
 */
public interface ParserListener {
    /**
     * An op-code has just finished
     * 
     * @param position
     *            the location of the op-code in the code
     * @param buffer
     *            the bytes of the op-code
     * @param length
     *            the number of bytes for the op-code
     */
    public void opCodeFinish(int position, byte[] buffer, int length);
}