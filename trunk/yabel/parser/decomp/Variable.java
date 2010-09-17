package yabel.parser.decomp;

import java.util.ArrayList;
import java.util.List;

/**
 * A defined variable in a code block.
 * 
 * @author Simon Greatrix
 */
public class Variable {
    /** The set for the code block */
    private final VariableSet set_;

    /** The definitions of this variable */
    private final List<VarDef> defs_ = new ArrayList<VarDef>();

    /** The index for this variable */
    final int index_;


    /**
     * New variable
     * 
     * @param variableSet
     *            the set of variables for the code block
     * @param index
     *            the index of this variable
     */
    Variable(VariableSet variableSet, int index) {
        set_ = variableSet;
        index_ = index;
    }


    VariableSet getSet() {
        return set_;
    }


    /**
     * Get the definition of this variable at the specified location
     * 
     * @param loc
     *            the location at which a definition is required for
     * @return the definition
     */
    VarDef getDef(int loc) {
        VarDef r = null;
        for(VarDef d:defs_) {
            if( d.loc_ <= loc ) r = d;
            else
                break;
        }
        return r;
    }


    /**
     * Add a definition for this variable
     * 
     * @param def
     *            the new definition
     */
    void addDef(VarDef def) {
        defs_.add(def);
    }
}