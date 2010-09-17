package yabel.parser.decomp;

import java.util.HashMap;
import java.util.Map;

/**
 * List of labels in a code block.
 * 
 * @author Simon Greatrix
 */
public class LabelList {
    /** The labels in the code block */
    private Map<Integer, Label> labels_ = new HashMap<Integer, Label>();


    /**
     * Get a 2-byte label reference
     * 
     * @param pos
     *            the position pointed to by the label
     * @return the label reference
     */
    public Label.Ref getRef(int pos) {
        Integer ipos = Integer.valueOf(pos);
        Label lbl = labels_.get(ipos);
        if( lbl == null ) {
            lbl = new Label(ipos);
            labels_.put(ipos, lbl);
        }
        return lbl.getRef();
    }


    /**
     * Get a 4-byte label reference
     * 
     * @param pos
     *            the position pointed to by the label
     * @return the label reference
     */
    public Label.Ref4 getRef4(int pos) {
        Integer ipos = Integer.valueOf(pos);
        Label lbl = labels_.get(ipos);
        if( lbl == null ) {
            lbl = new Label(ipos);
            labels_.put(ipos, lbl);
        }
        return lbl.getRef4();
    }


    /**
     * Get the label at the given position, if it has been created.
     * 
     * @param pos
     *            the position
     * @return the label if it exists, or null
     */
    public Label getLabel(int pos) {
        Integer ipos = Integer.valueOf(pos);
        return labels_.get(ipos);
    }
}
