package yabel.code;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;

/**
 * Compile a method reference or call.
 * @author Simon Greatrix
 *
 */
public enum CodeMethod implements CodeOperand {
    /** A reference to a method constant */
    METHOD,
    
    /** An INVOKESPECIAL operation */
    INVOKESPECIAL,
    
    /** An INVOKEVIRTUAL operation */
    INVOKEVIRTUAL,
    
    /** An INVOKESTATIC operation */
    INVOKESTATIC;

    /**
     * The op code for this operation
     */
    private final byte opCode_;
    
    private CodeMethod() {
        opCode_ = OpCodes.getOpCode(name());
    }

    /** {@inheritDoc} */
    @Override
    public void compile(Code code, List<String> toks,
            ClassData cd) {
        if( opCode_ != (byte) 0xff ) code.appendU1(opCode_);


        // 1 argument - look up in configuration
        // 2 arguments - local method name and type
        // 3 arguments - class method and type

        int i;
        switch (toks.size()) {
        case 3: {
            // try for a reference id in the ClassData
            String p = toks.get(2);
            i = Code.getInt(cd, p, toks.get(0));
            break;
        }
        case 4: {
            i = code.getMethodRef(toks.get(2), toks.get(3));
            break;
        }
        case 5: {
            i = code.getMethodRef(toks.get(2), toks.get(3), toks.get(4));
            break;
        }
        default:
            throw new YabelWrongTokenCountException(toks, 1, "constant reference", 2, "name and type", 3, "class, name and type");
        }
        code.appendU2(i);
    }

}
