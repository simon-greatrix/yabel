package yabel.code;

import java.util.List;

import yabel.ClassBuilder;
import yabel.ClassData;
import yabel.OpCodes;

/** Operands related to interfaces
 * 
 * @author Simon Greatrix
 *
 */
public enum CodeInterface implements CodeOperand {
    /** Invoke an interface */
    INVOKEINTERFACE {
        /** {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            code.appendU1(OpCodes.INVOKEINTERFACE);
            super.compile(code,toks,cd);
            int nargs = 1 + ClassBuilder.getArgsForType(toks.get(4));
            code.appendU1((byte) nargs);
            code.appendU1((byte) 0);
        }
    },
    
    /** Reference an interface method */
    INTERFACEMETHOD;

    /** {@inheritDoc} */
    @Override
    public void compile(Code code, List<String> toks, ClassData cd) {
        // ensure we have three elements for the reference
        if( toks.size() != 5 ) {
            throw new YabelWrongTokenCountException(toks, 3, "class, name, and type");
        }

        // got the method details, write out reference
        int val = code.getInterfaceMethodRef(toks.get(2), toks.get(3), toks.get(4));
        code.appendU2(val);
    }

}
