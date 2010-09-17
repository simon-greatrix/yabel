package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;
import yabel.code.CodeOperand;
import yabel.code.CodeTokenizer;
import yabel.code.CompilerOutput;
import yabel.code.YabelWrongTokenCountException;
import yabel.constants.Constant;
import yabel.constants.ConstantNumber;

/**
 * Compile an LDC instruction
 * 
 * @author Simon Greatrix
 * 
 */
public class CodeLDC implements CodeOperand {
    /** Singleton instance */
    public static final CodeLDC INSTANCE = new CodeLDC();


    /**
     * Compile an LDC for a known entry in the constant pool
     * 
     * @param code
     *            the associated code block
     * @param val
     *            the entry in the constant pool
     */
    public static void compile(CompilerOutput code, int val) {
        Constant con = code.getConstant(val);
        if( con instanceof ConstantNumber ) {
            // longs and doubles must use LDC2_W
            Number n = ((ConstantNumber) con).getValue();
            if( (n instanceof Double) || (n instanceof Long) ) {
                code.appendU1(OpCodes.LDC2_W);
                code.appendU2(val);
                return;
            }
        }

        if( val < 256 ) {
            code.appendU1(OpCodes.LDC);
            code.appendU1((byte) val);
        } else {
            code.appendU1(OpCodes.LDC_W);
            code.appendU2(val);
        }
    }


    private CodeLDC() {
    // private constructor
    }


    /** {@inheritDoc} */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
        int i = -1;
        switch (toks.size()) {
        case 3: {
            // raw, LDC, property
            String t2 = toks.get(2);
            String r2 = CodeTokenizer.isReplacement(t2);
            Object o = t2;
            if( r2 != null ) o = cd.get(Object.class, r2, t2);
            if( o instanceof Number ) {
                // it is a numeric constant
                Number n = (Number) o;
                i = code.getConstantRef(n);
            } else if( o instanceof String ) {
                // it is a String constant
                String s = (String) o;
                i = code.getConstantRef(s);
            } else if( o instanceof Class<?> ) {
                // undocumented, but constants can be classes
                String nm = ((Class<?>) o).getName();
                nm = nm.replace('.', '/');
                i = code.getClassRef(nm);
            }
            break;
        }
        case 4: {
            // raw, LDC, type, value
            String v = toks.get(3);
            i = code.getConstantRef(toks.get(0), toks.get(2), v);
            break;
        }
        default: // wrong
            throw new YabelWrongTokenCountException(toks, 1, "replacement", 2, "type and value");
        }

        // try for a bare int
        if( i == -1 ) i = CompilerOutput.getInt(cd, toks.get(2), toks.get(0));
        compile(code, i);
    }


    /** {@inheritDoc} */
    @Override
    public String name() {
        return "LDC";
    }

}
