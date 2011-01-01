package yabel.parser.decomp;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A defined variable in a code block.
 * 
 * @author Simon Greatrix
 */
public class Variable {
    /** The set for the code block */
    private final VariableSet set_;

    /** The definitions of this variable */
    private final SortedMap<Integer, VarDef> defs_ = new TreeMap<Integer, VarDef>();

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
        new VarDef(this, -1, null);
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
        SortedMap<Integer, VarDef> m = defs_.headMap(Integer.valueOf(loc + 1));
        Integer l = m.lastKey();
        return defs_.get(l);
    }


    /**
     * Get the source that defines this variable at the specified location, if
     * any.
     * 
     * @param loc
     *            the location
     * @return the definition at the point, if any
     */
    VarDef getSource(Integer loc) {
        return defs_.get(loc);
    }


    /**
     * Add a definition for this variable
     * 
     * @param def
     *            the new definition
     */
    void addDef(VarDef def) {
        Integer loc = Integer.valueOf(def.loc_);
        defs_.put(loc, def);
    }
}