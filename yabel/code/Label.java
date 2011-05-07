package yabel.code;

import java.util.ArrayList;
import java.util.List;

/**
 * A label in the code.
 * 
 * @author Simon Greatrix
 * 
 */
class Label extends Location {
    /** ID of this label */
    final String id_;

    /** Places where this label is used */
    List<LabelUse> usage_ = new ArrayList<LabelUse>();


    /**
     * Create new label
     * 
     * @param id
     *            identifier of this label
     */
    Label(String id) {
        id_ = id;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if( o == null ) return false;
        if( o == this ) return true;
        if( o instanceof Label ) {
            return id_.equals(((Label) o).id_);
        }
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id_.hashCode();
    }


    /** {@inheritDoc} */
    @Override
    void requirePlaced() {
        if( location_ == -1 )
            throw new YabelLabelException("Label \"" + id_
                    + "\" is not located");
    }
    
    
    /** {@inheritDoc} */
    @Override
    Object getIdentifier() {
        return id_;
    }
}