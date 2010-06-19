package yabel.code;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;

/**
 * Field operations.
 * @author Simon Greatrix
 *
 */
public enum CodeField implements CodeOperand {
    /** A reference to field constant */
    FIELD,
    
    /** A PUTFIELD operation */
    PUTFIELD,
    
    /** A GETFIELD operation */
    GETFIELD,
    
    /** A PUTSTATIC operation */
    PUTSTATIC,
    
    /** A GETSTATIC operation */
    GETSTATIC;

    /**
     * The op code for this operation
     */
    private final byte opCode_;
    
    private CodeField() {
        opCode_ = OpCodes.getOpCode(name());
    }

    /** {@inheritDoc} */
    @Override
    public void compile(Code code, List<String> toks,
            ClassData cd) {
        if( opCode_ != (byte) 0xff ) code.appendU1(opCode_);

        // is it valid? Local field will just be field name. Remote field
        // will be class, name, type.
        int size = toks.size();
        switch (size) {
        case 3: {
            // must be a bare name
            String k = toks.get(2);
            int val = code.getFieldRef(k);
            code.appendU2(val);
            return;
        }
        case 5: {
            // must be class, name, type
            int val = code.getFieldRef(toks.get(2), toks.get(3), toks.get(4));
            code.appendU2(val);
            return;
        }
        default:
            throw new YabelWrongTokenCountException(toks, 1,"name", 3,"class, name and type");
        }
    }
}
