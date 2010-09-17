package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;
import yabel.code.YabelWrongTokenCountException;

/**
 * Handle explicit values in the input
 * 
 * @author Simon Greatrix
 * 
 */
public enum CodeU1U2 implements CodeOperand {
    /** Append a single byte */
    U1 {
        /** {@inheritDoc} */
        protected void write(CompilerOutput code, int i) {
            code.appendU1((byte) i);
        }
    },
    /** Append a two byte unsigned value */
    U2 {
        /** {@inheritDoc} */
        protected void write(CompilerOutput code, int i) {
            code.appendU2(i);
        }
    },
    /** Append a 4 byte signed value */
    S4 {
        /** {@inheritDoc} */
        protected void write(CompilerOutput code, int i) {
            code.appendS4(i);
        }
    };

    /** {@inheritDoc} */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
        if( toks.size() > 3 )
            throw new YabelWrongTokenCountException(toks, 1, "datum");
        int i = CompilerOutput.getInt(cd, toks.get(2), toks.get(0));
        write(code, i);
    }


    /**
     * Write out the value
     * 
     * @param code
     *            the code to append to
     * @param i
     *            the value
     */
    protected abstract void write(CompilerOutput code, int i);
}
