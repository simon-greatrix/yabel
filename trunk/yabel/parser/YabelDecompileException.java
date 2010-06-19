package yabel.parser;

/**
 * Problem when decompiling byte-code
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelDecompileException extends IllegalArgumentException {
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
