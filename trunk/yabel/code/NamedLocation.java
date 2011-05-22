package yabel.code;

/**
 * 
 * @author Simon Greatrix
 */
public class NamedLocation extends Location {
    /** ID of this location */
    final String id_;


    /**
     * Create a named location
     * 
     * @param name
     *            the name of this location
     */
    public NamedLocation(String name) {
        id_ = name;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if( o == null ) return false;
        if( o == this ) return true;
        if( o instanceof NamedLocation ) {
            return id_.equals(((NamedLocation) o).id_);
        }
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public Object getIdentifier() {
        return id_;
    }


    String getName() {
        return id_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id_.hashCode();
    }


    /** {@inheritDoc} */
    @Override
    public void requirePlaced() {
        if( !isSet() )
            throw new YabelLabelException("Location \"" + id_
                    + "\" is not located");
    }


    /**
     * This location
     * 
     * @return "Location[<i>location</i>]"
     */
    @Override
    public String toString() {
        if( !isSet() ) {
            return "Location[" + id_ + " @ <unset>]";
        }
        return "Location[" + id_ + " @ " + getLocation() + "]";
    }
}
