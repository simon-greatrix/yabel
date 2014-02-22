package yabel.code;

import yabel.YabelException;

/**
 * A bad variable declaration
 * 
 * @author Simon Greatrix
 */
public class YabelBadVariableException extends YabelException {
    /** Serial version UID */
    private static final long serialVersionUID = 1L;


    /**
     * @param message
     *            description of the problem
     */
    public YabelBadVariableException(String message) {
        super(message);
    }


    /**
     * @param message
     *            description of the problem
     * @param cause
     *            cause of the problem
     */
    public YabelBadVariableException(String message, Throwable cause) {
        super(message, cause);
    }

}
