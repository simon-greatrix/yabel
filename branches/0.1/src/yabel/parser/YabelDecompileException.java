package yabel.parser;

import yabel.YabelException;

/**
 * Problem when decompiling byte-code
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelDecompileException extends YabelException {
    /** serial version UID */
    private static final long serialVersionUID = -373007230970501876L;


    /**
     * Problem when decompiling byte-code.
     * 
     * @param message
     *            description of problem
     */
    public YabelDecompileException(String message) {
        super(message);
    }
}
