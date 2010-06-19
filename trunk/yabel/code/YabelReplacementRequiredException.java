package yabel.code;

import java.util.List;

/**
 * A replacement parameter was required.
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelReplacementRequiredException extends IllegalArgumentException {
    /** serial version UID */
    private static final long serialVersionUID = -4743776845085640667L;

    /**
     * A replacement parameter was required
     * 
     * @param toks
     *            the input tokens
     * @param index
     *            the index of the parameter
     */
    public YabelReplacementRequiredException(List<String> toks, int index) {
        super(
                String.format(
                        "Parameter {0} to \"{1}\" must be a replacement, not \"{2}\". Input was \"{3}\".",
                        Integer.valueOf(index), toks.get(1).toUpperCase(),
                        toks.get(index + 1), toks.get(0)));
    }
}
