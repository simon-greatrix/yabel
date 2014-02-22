package yabel.parser.decomp;

import java.util.Collection;
import java.util.Collections;
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
     * Get or create a label at the specified position.
     * 
     * @param pos
     *            the label's position
     * @return the label
     */
    public Label createLabel(int pos) {
        Integer ipos = Integer.valueOf(pos);
        Label lbl = labels_.get(ipos);
        if( lbl == null ) {
            lbl = new Label(ipos);
            labels_.put(ipos, lbl);
        }
        return lbl;
    }


    /**
     * Get all the labels
     * 
     * @return the labels
     */
    public Collection<Label> getAllLabels() {
        return Collections.unmodifiableCollection(labels_.values());
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


    /**
     * Get a 2-byte label reference
     * 
     * @param pos
     *            the position pointed to by the label
     * @return the label reference
     */
    public Label.Ref getRef(int pos) {
        Label lbl = createLabel(pos);
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
        Label lbl = createLabel(pos);
        return lbl.getRef4();
    }
}
