package yabel.code;

import java.util.List;

import yabel.YabelException;

/**
 * Failed to convert input to the required numeric type
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelBadNumberException extends YabelException {
    /** serial version UID */
    private static final long serialVersionUID = 6189092301550095502L;


    /**
     * Unable to convert the input to the required numeric type
     * 
     * @param toks
     *            the input tokens
     * @param index
     *            the parameter index in the input tokens
     * @param type
     *            the required type
     */
    public YabelBadNumberException(List<String> toks, int index, String type) {
        this(toks.get(0), toks.get(index), type);
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
                "Unable to convert \"%s\" to an \"%s\". Input was \"%s\".",
                fld, type, raw));
    }
}
