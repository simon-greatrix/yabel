package yabel.code;

import java.util.ArrayList;
import java.util.List;

/**
 * A label in the code.
 * 
 * @author Simon Greatrix
 * 
 */
class Label extends NamedLocation {
    /** Places where this label is used */
    List<LabelUse> usage_ = new ArrayList<LabelUse>();


    /**
     * Create new label
     * 
     * @param id
     *            identifier of this label
     */
    Label(String id) {
        super(id);
    }


    /** {@inheritDoc} */
    @Override
    public void requirePlaced() {
        if( ! isSet() )
            throw new YabelLabelException("Label \"" + id_
                    + "\" is not located");
    }
}