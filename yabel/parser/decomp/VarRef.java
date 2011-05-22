package yabel.parser.decomp;

/**
 * A reference to a variable.
 * 
 * @author Simon Greatrix
 */
class VarRef implements Source {
    /** Associated variable set */
    final VariableSet set_;

    /** The location of this reference */
    final int loc_;

    /** The variable this is associated with */
    final int variable_;


    /**
     * New reference
     * 
     * @param set
     *            the associated variable set
     * @param variable
     *            the variable
     * @param loc
     *            the location
     */
    VarRef(VariableSet set, int variable, int loc) {
        set_ = set;
        variable_ = variable;
        loc_ = loc;
    }


    /**
     * The source code for this reference {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    @Override
    public String source() {
        VarScope scope = set_.getScope(loc_, variable_);
        if( scope != null ) {
            return scope.name_;
        }
        return Integer.toString(variable_);
    }
}