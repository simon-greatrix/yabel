package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;
import yabel.code.YabelWrongTokenCountException;

/**
 * Compile label related operations
 * 
 * @author Simon Greatrix
 * 
 */
abstract public class CodeLabel implements CodeOperand {
    /**
     * Get the operations supported
     * 
     * @return the operations
     */
    public static CodeOperand[] values() {
        return new CodeOperand[] { HASH, HASH4, AT };
    }

    /** Two byte offset value */
    final static CodeLabel HASH = new CodeLabel() {
        protected void compile(CompilerOutput code, String label) {
            code.compileJump(label, 2);
        }


        public String name() {
            return "#";
        }
    };

    /** Four byte offset value */
    final static CodeLabel HASH4 = new CodeLabel() {
        protected void compile(CompilerOutput code, String label) {
            code.compileJump(label, 4);
        }


        public String name() {
            return "#4";
        }
    };

    /** Place label */
    final static CodeLabel AT = new CodeLabel() {
        protected void compile(CompilerOutput code, String label) {
            code.setLabel(label);
        }


        public String name() {
            return "@";
        }
    };


    /** {@inheritDoc} */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
        if( toks.size() != 3 ) {
            throw new YabelWrongTokenCountException(toks, 1, "label name");
        }
        String p = toks.get(2);
        compile(code, p);
    }


    /**
     * Handle the named label.
     * 
     * @param code
     *            the associated code block
     * @param label
     *            the label's name
     */
    abstract protected void compile(CompilerOutput code, String label);
}
