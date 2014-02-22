package yabel.parser.decomp;

import yabel.attributes.LocalVariableTable.Scope;

/**
 * 
 * @author Simon Greatrix
 */
class VarScope {
    /** The end location of this variable's scope */
    final int endPC_;

    /** The lowest index variable slot used */
    final int index1_;

    /** The highest index variable slot used */
    final int index2_;

    /** This variable's name */
    final String name_;

    /** The start location of this variable's scope */
    final int startPC_;

    /** The type of this variable */
    final String type_;

    /** The width of this variable */
    final int width_;

    /** Was this a valid scope? */
    final boolean valid_;


    /**
     * Create a variable scope from a LocalVariableTable entry
     * 
     * @param scope
     *            the table entry
     */
    VarScope(Scope scope) {
        endPC_ = scope.getEndPC().getLocationSafe();
        startPC_ = scope.getStartPC().getLocationSafe();

        type_ = scope.getType().get();
        index1_ = scope.getIndex();
        width_ = (type_.equals("J") || type_.equals("D")) ? 2 : 1;
        name_ = scope.getName().get();
        index2_ = index1_ + width_ - 1;

        boolean valid = true;
        if( endPC_ <= startPC_ ) {
            valid = false;
            System.err.println("Scope ends before it starts\n" + this);
        }

        if( valid && (name_.length() > 0) ) {
            valid = Character.isJavaIdentifierStart(name_.charAt(0));
            for(int i = 1;(i < name_.length()) && valid;i++) {
                valid = Character.isJavaIdentifierPart(name_.charAt(i));
            }
            if( !valid ) {
                System.err.println("Scope name is not a valid Java name\n"
                        + this);
            }
        } else {
            valid = false;
        }

        valid_ = valid;
    }


    /**
     * Get a definition for this scope if one is required at the specified
     * location
     * 
     * @param set
     *            the containing variable set
     * @param loc
     *            the location
     * @return definition or null
     */
    Source define(VariableSet set, int loc) {
        if( loc != startPC_ ) return null;

        // ascertain if the variable is allocated in the first available space
        boolean[] inUse = set.inUse(loc);
        boolean needIndex = false;
        for(int i = 0;i < index1_;i++) {
            if( !inUse[i] ) {
                if( width_ == 2 ) {
                    if( !inUse[i + 1] ) {
                        needIndex = true;
                        break;
                    }
                } else {
                    needIndex = true;
                    break;
                }
            }
        }

        return new Simple("$:"
                + (needIndex ? Integer.toString(index1_) + ":" : "") + type_
                + ":" + name_);
    }


    /**
     * Mark the variable slots this scope uses at the specified location
     * 
     * @param loc
     *            the location
     * @param used
     *            the variable slots
     */
    void inUse(int loc, boolean[] used) {
        if( (startPC_ <= loc) && (loc < endPC_) ) {
            used[index1_] = true;
            used[index2_] = true;
        }
    }


    /**
     * Does this scope match a variable usage?
     * 
     * @param loc
     *            the location of the usage
     * @param index
     *            the variable used
     * @return true if this matches
     */
    boolean matches(int loc, int index) {
        return (startPC_ <= loc) && (loc < endPC_) && (index1_ == index);
    }


    /**
     * Does this overlap with another scope definition? Two scopes cannot
     * overlap so this will be invalid if it does.
     * 
     * @param other
     *            the other scope
     * @return true if it overlaps
     */
    boolean overlaps(VarScope other) {
        if( !((other.index1_ == index1_) || (other.index1_ == index2_) || (other.index2_ == index1_)) )
            return false;
        if( startPC_ >= other.endPC_ ) return false;
        if( endPC_ <= other.startPC_ ) return false;

        return true;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VarScope [endPC_=").append(endPC_).append(", index1_=").append(
                index1_).append(", index2_=").append(index2_).append(", name_=").append(
                name_).append(", startPC_=").append(startPC_).append(", type_=").append(
                type_).append(", width_=").append(width_).append(", valid_=").append(
                valid_).append("]");
        return builder.toString();
    }


    /**
     * Get a un-definition for this scope if one is required at the specified
     * location
     * 
     * @param loc
     *            the location
     * @return un-definition or null
     */
    Source undefine(int loc) {
        if( loc != endPC_ ) return null;
        return new Simple("$:" + name_);
    }

}
