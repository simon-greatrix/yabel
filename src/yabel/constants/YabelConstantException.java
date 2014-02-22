package yabel.constants;

import yabel.YabelException;

/**
 * Problem with a constant in the pool
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelConstantException extends YabelException {
    /** serial version UID */
    private static final long serialVersionUID = 8614186946978319610L;


    /**
     * Problem with a constant in the pool.
     * 
     * @param message
     *            description of problem
     */
    public YabelConstantException(String message) {
        super(message);
    }


    /**
     * Problem with a constant in the pool.
     * 
     * @param message
     *            description of problem
     * @param cause
     *            the causative exception
     */
    public YabelConstantException(String message, Throwable cause) {
        super(message, cause);
    }
}
