package yabel.code;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;

/**
 * Operations that load and store in local variables.
 * 
 * @author Simon Greatrix
 */
public enum CodeLoadStore implements CodeOperand {
    /** Object load */
    ALOAD,
    /** Object store */
    ASTORE,

    /** Double load */
    DLOAD,
    /** Double store */
    DSTORE,

    /** Float load */
    FLOAD,
    /** Float store */
    FSTORE,
    /** Integer load */

    ILOAD,
    /** Integer store */
    ISTORE,
    /** Long load */

    LLOAD,
    /** Long store */
    LSTORE,
    /** Return instruction */

    RET {
        /** {@inheritDoc} */
        @Override
        protected void compile(Code code, int i) {
            if( i < 0x100 ) {
                code.appendU1(OpCodes.RET);
                code.appendU1((byte) i);
            } else {
                code.appendU1(OpCodes.WIDE);
                code.appendU1(OpCodes.RET);
                code.appendU2(i);
            }
        }
    };

    /** OpCode accessing variable 0 */
    private final byte op0_;

    /** OpCode accessing variable 1 */
    private final byte op1_;

    /** OpCode accessing variable 2 */
    private final byte op2_;

    /** OpCode accessing variable 3 */
    private final byte op3_;

    /** OpCode accessing variable specified */
    private final byte op_;


    private CodeLoadStore() {
        String name = name();
        op0_ = OpCodes.getOpCode(name + "_0");
        op1_ = OpCodes.getOpCode(name + "_1");
        op2_ = OpCodes.getOpCode(name + "_2");
        op3_ = OpCodes.getOpCode(name + "_3");
        op_ = OpCodes.getOpCode(name);
    }


    /** {@inheritDoc} */
    @Override
    public void compile(Code code, List<String> toks, ClassData cd) {
        if( toks.size() > 3 )
            throw new YabelWrongTokenCountException(toks, 1, "variable");
        int i = Code.getInt(cd, toks.get(2), toks.get(0));
        compile(code, i);
    }


    /**
     * Compile the instruction given the local variable ID
     * 
     * @param code
     *            the code being compiled
     * @param i
     *            the local variable ID
     */
    protected void compile(Code code, int i) {
        switch (i) {
        case 0:
            code.appendU1(op0_);
            break;
        case 1:
            code.appendU1(op1_);
            break;
        case 2:
            code.appendU1(op2_);
            break;
        case 3:
            code.appendU1(op3_);
            break;
        default:
            if( i < 0x100 ) {
                code.appendU1(op_);
                code.appendU1((byte) i);
            } else {
                code.appendU1(OpCodes.WIDE);
                code.appendU1(op_);
                code.appendU2(i);
            }
        }
    }
}
