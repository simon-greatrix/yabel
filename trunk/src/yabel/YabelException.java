package yabel;

/**
 * Super class for all Yabel's run-time exceptions.
 * 
 * @author Simon Greatrix
 */
public class YabelException extends IllegalArgumentException {

    /** serial version UID */
    private static final long serialVersionUID = 3293418743749985601L;


    /** Exception with no details */
    public YabelException() {
        super();
    }


    /**
     * Exception with an explanatory message
     * 
     * @param message
     *            the message
     */
    public YabelException(String message) {
        super(message);
    }


    /**
     * Exception with an explanatory message and a cause
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public YabelException(String message, Throwable cause) {
        super(message, cause);
    }
}
