package yabel.parser.decomp;

import yabel.OpCodes;

/**
 * A simple bit of source code
 * 
 * @author Simon Greatrix
 */
public class Simple implements Source {
    /** The source code */
    private final String source_;


    /**
     * Create a simple source code segment from a single op-code
     * 
     * @param code
     *            the op code
     */
    public Simple(byte code) {
        source_ = OpCodes.getOpName(code);
    }


    /**
     * Create a simple source code segment
     * 
     * @param source
     *            the source code
     */
    public Simple(String source) {
        source_ = source;
    }


    /** {@inheritDoc} */
    public String source() {
        return source_;
    }
}
