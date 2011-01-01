package yabel.code;

import yabel.YabelException;

/**
 * There is a problem with a label.
 * @author Simon Greatrix
 *
 */
public class YabelLabelException extends YabelException {
    /** serial version UID */
    private static final long serialVersionUID = -7880031571704239906L;

    /**
     * There is a problem with a label
     * @param message description of problem
     */
    public YabelLabelException(String message) {
        super(message);
    }
}
