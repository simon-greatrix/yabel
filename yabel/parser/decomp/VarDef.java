package yabel.parser.decomp;

/**
 * Definition of a variable name.
 * 
 * @author Simon Greatrix
 */
public class VarDef implements Source {
    /** Location of this definition */
    final int loc_;

    /** Name defined for this variable */
    String name_;

    /** The variable being defined. */
    private final Variable variable_;


    /**
     * Define a variable name
     * 
     * @param variable
     *            the variable
     * @param loc
     *            the location
     * @param name
     *            name for this variable. To undefine, pass null
     */
    VarDef(Variable variable, int loc, String name) {
        variable_ = variable;
        loc_ = loc;
        name_ = name;
        variable_.addDef(this);
    }


    /**
     * Returns a pattern like "$:1:name". {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    public String source() {
        if( name_ != null ) return "$:" + variable_.index_ + ":" + name_;
        return "$:" + variable_.index_;
    }


    /** {@inheritDoc} */
    public String toString() {
        return "VarDef[loc=" + loc_ + ", name=" + name_ + "]";
    }
}