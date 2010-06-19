package yabel.code;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;

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
    public void compile(Code code, List<String> toks, ClassData cd) {
        int size = toks.size();
        if( size != 4 )
            throw new YabelWrongTokenCountException(toks, 2,
                    "variable and amount");

        // size is 4, so have type and value
        String raw = toks.get(0);
        int iincIndex = Code.getInt(cd, toks.get(2), raw);
        int iincConst = Code.getInt(cd, toks.get(3), raw);

        // output IINC or WIDE IINC as needed
        if( (iincIndex < 0x100) && (iincConst < 0x100) ) {
            code.appendU1(OpCodes.IINC);
            code.appendU1((byte) iincIndex);
            code.appendU1((byte) iincConst);
        } else {
            code.appendU1(OpCodes.WIDE);
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