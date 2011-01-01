package yabel.code;

import yabel.YabelException;

/**
 * Problem parsing compiler input
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelParseException extends YabelException {
    /** serial version UID */
    private static final long serialVersionUID = -4439358787820231815L;

    /**
     * Problem parsing compiler input
     * 
     * @param message
     *            description of problem
     */
    public YabelParseException(String message) {
        super(message);
    }
}
