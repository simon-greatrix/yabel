package yabel.code;

/**
 * 
 * @author Simon Greatrix
 */
class Location {
    /** The location */
    int location_;


    /** Create a location that is not yet defined */
    Location() {
        location_ = -1;
    }


    /**
     * Create a specified location
     * 
     * @param location
     *            the location
     */
    Location(int location) {
        location_ = location;
    }


    /**
     * Check this location is set, and throw a YabelLabelException if not.
     */
    void requirePlaced() {
        if( location_ == -1 )
            throw new YabelLabelException("Location is undefined");
    }


    /**
     * Get an identifier for this location.
     * 
     * @return an object that can identify this location
     */
    Object getIdentifier() {
        requirePlaced();
        return Integer.valueOf(location_);
    }
}
