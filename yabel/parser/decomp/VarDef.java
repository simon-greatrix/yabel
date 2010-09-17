package yabel.parser.decomp;

/**
 * Definition of a variable name.
 * 
 * @author Simon Greatrix
 */
public class VarDef implements Source, Comparable<VarDef> {
    /** The variable being defined. */
    private final Variable variable_;

    /** Location of this definition */
    final int loc_;

    /** Name defined for this variable */
    String name_;


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
    }


    /**
     * Returns a pattern like "$:1:name". {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    public String source() {
        if( name_!=null ) return "$:" + variable_.index_ + ":" + name_;
        return "$:"+variable_.index_;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(VarDef other) {
        int diff = loc_ - other.loc_;
        if( diff != 0 ) return diff;
        diff = variable_.index_ - other.variable_.index_;
        return diff;
    }
}