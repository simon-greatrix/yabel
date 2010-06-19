package yabel.constants;

/**
 * Problem with a constant in the pool
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelConstantException extends IllegalArgumentException {
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
}
