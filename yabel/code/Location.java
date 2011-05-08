package yabel.code;

/**
 * 
 * @author Simon Greatrix
 */
public class Location {
    /** Location position used to indicate location is not yet set */
    final static int UNSET = -1;
    
    /** The location */
    private int location_;


    /** Create a location that is not yet defined */
    Location() {
        location_ = UNSET;
    }


    /**
     * Create a specified location
     * 
     * @param location
     *            the location
     */
    public Location(int location) {
        location_ = location;
    }


    /**
     * Get an identifier for this location.
     * 
     * @return an object that can identify this location
     */
    public Object getIdentifier() {
        requirePlaced();
        return Integer.valueOf(location_);
    }


    /**
     * Get this locations byte code location, throwing a YabelLabelException if
     * it is not set.
     * 
     * @return the location
     */
    public int getLocationSafe() {
        requirePlaced();
        return location_;
    }


    public int getLocation() {
        return location_;
    }


    /**
     * Check this location is set, and throw a YabelLabelException if not.
     */
    public void requirePlaced() {
        if( !isSet() ) throw new YabelLabelException("Location is undefined");
    }


    public boolean isSet() {
        return location_ != -1;
    }


    public void setLocation(int location) {
        location_ = location;
    }


    /**
     * This location
     * 
     * @return "Location[<i>location</i>]"
     */
    @Override
    public String toString() {
        if( ! isSet() ) {
            return "Location[<unset>]";
        }
        return "Location[" + location_ + "]";
    }
}
