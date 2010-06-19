package yabel.io;

/**
 * Problem reading or writing an XML ClassData structure.
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelXMLException extends IllegalArgumentException {
    /** serial version UID */
    private static final long serialVersionUID = 1094393683455256874L;

    /**
     * Problem reading or writing an XML ClassData structure
     * 
     * @param message
     *            the problem description
     */
    public YabelXMLException(String message) {
        super(message);
    }
}
