package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;
import yabel.code.YabelWrongTokenCountException;

/**
 * Compile the IINC operand
 * 
 * @author Simon Greatrix
 * 
 */
public class CodeIINC implements CodeOperand {
    /** Singleton instance */
    public static final CodeOperand INSTANCE = new CodeIINC();


    private CodeIINC() {
        // private constructor
    }


    /** {@inheritDoc} */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
        int size = toks.size();
        if( size != 4 )
            throw new YabelWrongTokenCountException(toks, 2,
                    "variable and amount");

        // size is 4, so have index and constant
        String raw = toks.get(0);
        int iincIndex = code.getVariable(toks.get(2), raw);
        int iincConst = CompilerOutput.getInt(toks.get(3), raw);

        // output IINC or WIDE IINC as needed
        if( (iincIndex < 0x100) && (iincConst < 0x100) && !code.wasLastWide() ) {
            code.appendU1(OpCodes.IINC);
            code.appendU1((byte) iincIndex);
            code.appendU1((byte) iincConst);
        } else {
            code.appendWide();
            code.appendU1(OpCodes.IINC);
            code.appendU2(iincIndex);
            code.appendU2(iincConst);
        }
    }


    /** {@inheritDoc} */
    @Override
    public String name() {
        return "IINC";
    }
}