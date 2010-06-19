package yabel.code;

import java.util.List;

/**
 * Failed to convert input to the required numeric type
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelBadNumberException extends NumberFormatException {
    /** serial version UID */
    private static final long serialVersionUID = 6189092301550095502L;


    /**
     * Unable to convert the input to the required numeric type
     * 
     * @param toks
     *            the input tokens
     * @param index
     *            the parameter index in toks
     * @param type
     *            the required type
     */
    public YabelBadNumberException(List<String> toks, int index, String type) {
        this(toks.get(0),toks.get(index),type);
    }


    /**
     * Unable to convert the input to the required numeric type
     * 
     * @param raw
     *            the raw input
     * @param fld
     *            the field to convert
     * @param type
     *            the required type
     */
    public YabelBadNumberException(String raw, String fld, String type) {
        super(String.format(
                "Unable to convert \"{0}\" to an \"{1}\". Input was \"{2}\".",
                fld, type, raw));
    }
}
