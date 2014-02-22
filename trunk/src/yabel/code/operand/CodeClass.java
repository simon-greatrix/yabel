package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;
import yabel.code.YabelWrongTokenCountException;

/**
 * Operations that take a reference to a class constant as their operand
 * 
 * @author Simon Greatrix
 * 
 */
public enum CodeClass implements CodeOperand {
    /** ANEWARRAY operation */
    ANEWARRAY,

    /** CHECKCAST operation */
    CHECKCAST,

    /** INSTANCEOF operation */
    INSTANCEOF,

    /** NEW operation */
    NEW,

    /** CLASS reference */
    CLASS;

    /**
     * The op code for this operation
     */
    private final byte opCode_;


    private CodeClass() {
        opCode_ = OpCodes.getOpCode(name());
    }


    /** {@inheritDoc} */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
        if( toks.size() != 3 )
            throw new YabelWrongTokenCountException(toks, 1, "class name");

        // the class name is a String so it will already be resolved
        String ref = toks.get(2);
        int val = code.getClassRef(ref);

        if( opCode_ != (byte) 0xff ) code.appendU1(opCode_);
        code.appendU2(val);
    }
}
