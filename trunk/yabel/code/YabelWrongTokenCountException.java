package yabel.code;

import java.util.List;

/**
 * Input to the compiler was invalid
 * 
 * @author Simon Greatrix
 * 
 */
public class YabelWrongTokenCountException extends IllegalArgumentException {
    /** serial version UID */
    private static final long serialVersionUID = 6445605414761921385L;


    /**
     * The input contained the wrong number of arguments
     * 
     * @param toks
     *            Compiler input tokens
     * @param requiredSize
     *            required number of tokens
     * @param desc
     *            description of required parameters
     */
    public YabelWrongTokenCountException(List<String> toks, int requiredSize,
            String desc) {
        super(
                String.format(
                        "Operation \"{0}\" accepts {1,choice,1#a single|1#{1} arguments}({2}) not {3}. Input was \"{4}\".",
                        toks.get(1).toUpperCase(),
                        Integer.valueOf(requiredSize), desc,
                        Integer.valueOf(toks.size()), toks.get(0)));
    }


    /**
     * The input contained the wrong number of arguments when there were two possibilities
     * 
     * @param toks
     *            Compiler input tokens
     * @param reqSize1
     *            required number of tokens for option 1
     * @param desc1
     *            description of required parameters for option 1
     * @param reqSize2
     *            required number of tokens for option 2
     * @param desc2
     *            description of required parameters for option 2
     */
    public YabelWrongTokenCountException(List<String> toks, int reqSize1,
            String desc1, int reqSize2, String desc2) {
        super(
                String.format(
                        "Operation \"{0}\" accepts either {1,choice,1#a single|1#{1} arguments}({2}) or {3,choice,1#a single|1#{3} arguments}({4}) not {5}. Input was \"{6}\".",
                        toks.get(1).toUpperCase(),
                        Integer.valueOf(reqSize1), desc1,
                        Integer.valueOf(reqSize2), desc2,
                        Integer.valueOf(toks.size()), toks.get(0)));
    }    


    /**
     * The input contained the wrong number of arguments when there were three possibilities
     * 
     * @param toks
     *            Compiler input tokens
     * @param reqSize1
     *            required number of tokens for option 1
     * @param desc1
     *            description of required parameters for option 1
     * @param reqSize2
     *            required number of tokens for option 2
     * @param desc2
     *            description of required parameters for option 2
     * @param reqSize3
     *            required number of tokens for option 3
     * @param desc3
     *            description of required parameters for option 3
     */
    public YabelWrongTokenCountException(List<String> toks, int reqSize1,
            String desc1, int reqSize2, String desc2, int reqSize3, String desc3) {
        super(
                String.format(
                        "Operation \"{0}\" accepts either {1,choice,1#a single|1#{1} arguments}({2}), {3,choice,1#a single|1#{3} arguments}({4}) or {5,choice,1#a single|1#{5} arguments}({6}) not {7}. Input was \"{8}\".",
                        toks.get(1).toUpperCase(),
                        Integer.valueOf(reqSize1), desc1,
                        Integer.valueOf(reqSize2), desc2,
                        Integer.valueOf(reqSize3), desc3,
                        Integer.valueOf(toks.size()), toks.get(0)));
    }    


    /**
     * The input contained the wrong number of arguments when there were many possibilities
     * 
     * @param toks
     *            Compiler input tokens
     * @param format
     *            required format
     */
    public YabelWrongTokenCountException(List<String> toks, String format) {
        super(
                String.format(
                        "Operation \"{0}\" should be \"{0} {1}\", not {2}.",
                        toks.get(1).toUpperCase(),format,toks.get(0)));
    }
}