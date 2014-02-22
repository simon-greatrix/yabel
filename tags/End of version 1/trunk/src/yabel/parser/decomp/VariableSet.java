package yabel.parser.decomp;

import java.util.*;

import yabel.OpCodes;
import yabel.attributes.LocalVariableTable.Scope;

/**
 * A set of variables for a code block.
 * 
 * @author Simon Greatrix
 */
public class VariableSet {
    /**
     * The data for an op-code relating to a variable
     * 
     * @author Simon Greatrix
     */
    private static class Decomp {
        /** The op code */
        String opCode_;

        /** The variable */
        int var_;


        Decomp(int b, int var) {
            opCode_ = OpCodes.getOpName(b);
            var_ = var;
        }
    }

    /**
     * Map of op-codes to the de-compilation
     */
    private final static Map<Byte, Decomp> DECOMP = Collections.synchronizedMap(new HashMap<Byte, Decomp>());

    static {
        DECOMP.put(Byte.valueOf(OpCodes.ALOAD_0), new Decomp(OpCodes.ALOAD, 0));
        DECOMP.put(Byte.valueOf(OpCodes.ALOAD_1), new Decomp(OpCodes.ALOAD, 1));
        DECOMP.put(Byte.valueOf(OpCodes.ALOAD_2), new Decomp(OpCodes.ALOAD, 2));
        DECOMP.put(Byte.valueOf(OpCodes.ALOAD_3), new Decomp(OpCodes.ALOAD, 3));

        DECOMP.put(Byte.valueOf(OpCodes.ASTORE_0),
                new Decomp(OpCodes.ASTORE, 0));
        DECOMP.put(Byte.valueOf(OpCodes.ASTORE_1),
                new Decomp(OpCodes.ASTORE, 1));
        DECOMP.put(Byte.valueOf(OpCodes.ASTORE_2),
                new Decomp(OpCodes.ASTORE, 2));
        DECOMP.put(Byte.valueOf(OpCodes.ASTORE_3),
                new Decomp(OpCodes.ASTORE, 3));

        DECOMP.put(Byte.valueOf(OpCodes.DLOAD_0), new Decomp(OpCodes.DLOAD, 0));
        DECOMP.put(Byte.valueOf(OpCodes.DLOAD_1), new Decomp(OpCodes.DLOAD, 1));
        DECOMP.put(Byte.valueOf(OpCodes.DLOAD_2), new Decomp(OpCodes.DLOAD, 2));
        DECOMP.put(Byte.valueOf(OpCodes.DLOAD_3), new Decomp(OpCodes.DLOAD, 3));

        DECOMP.put(Byte.valueOf(OpCodes.DSTORE_0),
                new Decomp(OpCodes.DSTORE, 0));
        DECOMP.put(Byte.valueOf(OpCodes.DSTORE_1),
                new Decomp(OpCodes.DSTORE, 1));
        DECOMP.put(Byte.valueOf(OpCodes.DSTORE_2),
                new Decomp(OpCodes.DSTORE, 2));
        DECOMP.put(Byte.valueOf(OpCodes.DSTORE_3),
                new Decomp(OpCodes.DSTORE, 3));

        DECOMP.put(Byte.valueOf(OpCodes.ILOAD_0), new Decomp(OpCodes.ILOAD, 0));
        DECOMP.put(Byte.valueOf(OpCodes.ILOAD_1), new Decomp(OpCodes.ILOAD, 1));
        DECOMP.put(Byte.valueOf(OpCodes.ILOAD_2), new Decomp(OpCodes.ILOAD, 2));
        DECOMP.put(Byte.valueOf(OpCodes.ILOAD_3), new Decomp(OpCodes.ILOAD, 3));

        DECOMP.put(Byte.valueOf(OpCodes.ISTORE_0),
                new Decomp(OpCodes.ISTORE, 0));
        DECOMP.put(Byte.valueOf(OpCodes.ISTORE_1),
                new Decomp(OpCodes.ISTORE, 1));
        DECOMP.put(Byte.valueOf(OpCodes.ISTORE_2),
                new Decomp(OpCodes.ISTORE, 2));
        DECOMP.put(Byte.valueOf(OpCodes.ISTORE_3),
                new Decomp(OpCodes.ISTORE, 3));

        DECOMP.put(Byte.valueOf(OpCodes.FLOAD_0), new Decomp(OpCodes.FLOAD, 0));
        DECOMP.put(Byte.valueOf(OpCodes.FLOAD_1), new Decomp(OpCodes.FLOAD, 1));
        DECOMP.put(Byte.valueOf(OpCodes.FLOAD_2), new Decomp(OpCodes.FLOAD, 2));
        DECOMP.put(Byte.valueOf(OpCodes.FLOAD_3), new Decomp(OpCodes.FLOAD, 3));

        DECOMP.put(Byte.valueOf(OpCodes.FSTORE_0),
                new Decomp(OpCodes.FSTORE, 0));
        DECOMP.put(Byte.valueOf(OpCodes.FSTORE_1),
                new Decomp(OpCodes.FSTORE, 1));
        DECOMP.put(Byte.valueOf(OpCodes.FSTORE_2),
                new Decomp(OpCodes.FSTORE, 2));
        DECOMP.put(Byte.valueOf(OpCodes.FSTORE_3),
                new Decomp(OpCodes.FSTORE, 3));

        DECOMP.put(Byte.valueOf(OpCodes.LLOAD_0), new Decomp(OpCodes.LLOAD, 0));
        DECOMP.put(Byte.valueOf(OpCodes.LLOAD_1), new Decomp(OpCodes.LLOAD, 1));
        DECOMP.put(Byte.valueOf(OpCodes.LLOAD_2), new Decomp(OpCodes.LLOAD, 2));
        DECOMP.put(Byte.valueOf(OpCodes.LLOAD_3), new Decomp(OpCodes.LLOAD, 3));

        DECOMP.put(Byte.valueOf(OpCodes.LSTORE_0),
                new Decomp(OpCodes.LSTORE, 0));
        DECOMP.put(Byte.valueOf(OpCodes.LSTORE_1),
                new Decomp(OpCodes.LSTORE, 1));
        DECOMP.put(Byte.valueOf(OpCodes.LSTORE_2),
                new Decomp(OpCodes.LSTORE, 2));
        DECOMP.put(Byte.valueOf(OpCodes.LSTORE_3),
                new Decomp(OpCodes.LSTORE, 3));
    }

    /** The variables */
    private final List<VarScope> vars_ = new ArrayList<VarScope>();

    /**
     * Number of variable slots known to this set
     */
    private int varCount_ = 0;


    /**
     * Add a variable scope
     * 
     * @param scope
     *            the scope
     */
    public void addScope(Scope scope) {
        VarScope var = new VarScope(scope);
        if( !var.valid_ ) {
            System.err.println("Scope " + var + " is not valid");
            return;
        }

        for(VarScope v:vars_) {
            if( v.overlaps(var) ) {
                System.err.println("Scope " + var + " overlaps " + v);
                return;
            }
        }
        varCount_ = Math.max(var.index2_, varCount_);
        vars_.add(var);
    }


    /**
     * Decompile an operation relating to a variable
     * 
     * @param b
     *            the op code
     * @param position
     *            the position
     * @return the decompiled source, or null
     */
    public Source decomp(byte b, int position) {
        Byte bb = Byte.valueOf(b);
        Decomp d = DECOMP.get(bb);
        if( d == null ) return null;
        return new Multi(d.opCode_, getRef(d.var_, position));
    }


    /**
     * Get the definitions at a specified location
     * 
     * @param loc
     *            the location
     * @return list of definitions, empty if none
     */
    public List<Source> getDefs(int loc) {
        List<Source> defs = new ArrayList<Source>(vars_.size());
        for(VarScope v:vars_) {
            Source d = v.undefine(loc);
            if( d != null ) defs.add(d);
        }
        for(VarScope v:vars_) {
            Source d = v.define(this, loc);
            if( d != null ) defs.add(d);
        }
        return defs;
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
    public Source getRef(int index, int location) {
        varCount_ = Math.max(index, varCount_);
        return new VarRef(this, index, location);
    }


    /**
     * Get the applicable scope
     * 
     * @param loc
     *            location of usage
     * @param variable
     *            variable used
     * @return matching scope or null
     */
    VarScope getScope(int loc, int variable) {
        for(VarScope v:vars_) {
            if( v.matches(loc, variable) ) return v;
        }
        return null;
    }


    /**
     * Get the variable slots in use at the specified location
     * 
     * @param loc
     *            the location
     * @return new indicating whether a slot is in use or not
     */
    boolean[] inUse(int loc) {
        boolean[] ret = new boolean[varCount_ + 1];
        for(VarScope v:vars_) {
            v.inUse(loc, ret);
        }
        return ret;
    }
}
