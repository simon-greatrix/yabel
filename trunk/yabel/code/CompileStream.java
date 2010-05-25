package yabel.code;

import yabel.parser.Parser;
import yabel.parser.ParserListener;

import java.io.ByteArrayOutputStream;

/**
 * Stores a stream of byte-code and tracks the starting point of the last
 * operation.
 * 
 * @author Simon Greatrix
 * 
 */
class CompileStream extends ByteArrayOutputStream implements
        ParserListener {
    /** The parser that interprets the byte-code */
    private Parser parser_;


    /** Create new CompileStream */
    public CompileStream() {
        parser_ = new Parser(this);
    }


    /**
     * Get the position the last op code started at.
     * 
     * @return the position of the last op code
     */
    int getLastOpPosition() {
        return parser_.getLastOpPosition();
    }


    /**
     * Receive notification that an op-code has just been completed. The
     * notification is ignored.
     * 
     * @param i
     *            ignored
     * @param b
     *            ignored
     * @param l
     *            ignored
     */
    public void opCodeFinish(int i, byte[] b, int l) {
    // do nothing
    }


    /** {@inheritDoc} */
    @Override
    public synchronized void reset() {
        super.reset();
        parser_ = new Parser(this);
    }


    /** {@inheritDoc} */
    @Override
    public synchronized void write(int b) {
        super.write(b);
        parser_.parse(b);
    }
}