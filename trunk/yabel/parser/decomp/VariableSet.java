package yabel.parser.decomp;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A set of variables for a code block.
 * 
 * @author Simon Greatrix
 */
public class VariableSet {
    /** The variables */
    private final List<Variable> vars_ = new ArrayList<Variable>();

    /** The definitions for this variable */
    private final SortedSet<VarDef> defs_ = new TreeSet<VarDef>();


    /**
     * Get the specified variable definition
     * 
     * @param index
     *            the index of the variable
     * @return the variable
     */
    public Variable getVariable(int index) {
        for(int i = vars_.size();i <= index;i++) {
            vars_.add(new Variable(this, i));
        }
        return vars_.get(index);
    }


    /**
     * Get a reference to a variable
     * 
     * @param index
     *            the variable's index
     * @param location
     *            the location of the reference
     * @return the reference
     */
    public VarRef getRef(int index, int location) {
        Variable var = getVariable(index);
        VarDef def = var.getDef(location);
        if( def == null ) {
            // no def created yet
            def = new VarDef(var, location, "var" + var.index_);
            defs_.add(def);
            var.addDef(def);
        }

        return new VarRef(var, location);
    }
}