package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;

/**
 * A variable definition
 * @author Simon Greatrix
 */
public class CodeVar implements CodeOperand {
    /** The instance of this operand */
    public static final CodeVar INSTANCE = new CodeVar();


    /**
     * {@inheritDoc}
     * 
     * @see yabel.code.CodeOperand#compile(yabel.code.CompilerOutput,
     *      java.util.List, yabel.ClassData)
     */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.code.CodeOperand#name()
     */
    @Override
    public String name() {
        return "$";
    }

}
