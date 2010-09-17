package yabel.parser.decomp;

/**
 * A reference to a variable.
 * 
 * @author Simon Greatrix
 */
public class VarRef implements Source {
    /** The variable this is associated with */
    private final Variable variable_;

    /**
     * The location of this reference
     */
    final int loc_;


    /**
     * New reference
     * 
     * @param variable
     *            the variable
     * @param loc
     *            the location
     */
    VarRef(Variable variable, int loc) {
        variable_ = variable;
        loc_ = loc;
    }


    /**
     * The source code for this reference {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    public String source() {
        VarDef def = variable_.getDef(loc_);
        if( (def!=null) && (def.name_ != null) ) return "$" + def.name_;
        return Integer.toString(variable_.index_);
    }
}