package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;
import yabel.code.YabelWrongTokenCountException;

/**
 * Compile array related operations
 * 
 * @author Simon Greatrix
 * 
 */
public enum CodeArrays implements CodeOperand {
    /** NEWARRAY operation */
    NEWARRAY {
        /** {@inheritDoc} */
        @Override
        public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
            if( toks.size() > 3 )
                throw new YabelWrongTokenCountException(toks, 1, "array type");
            code.appendU1(OpCodes.NEWARRAY);
            String p = toks.get(2);
            int i = OpCodes.getArrayType(p);
            if( i == -1 ) i = CompilerOutput.getInt(cd, p, toks.get(0));
            code.appendU1((byte)i);
        }
    },

    /** MULTIANEWARRAY operation */
    MULTIANEWARRAY{
        /** {@inheritDoc} */
        @Override
        public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
            if( toks.size() != 4 )
                throw new YabelWrongTokenCountException(toks, 2, "class and dimensions");
            code.appendU1(OpCodes.MULTIANEWARRAY);
            code.appendU2(code.getClassRef(toks.get(2)));
            code.appendU1((byte) CompilerOutput.getInt(cd, toks.get(3), toks.get(0))); 
        }
    };
}
