package yabel.code;

import java.util.List;

import yabel.ClassData;
import yabel.OpCodes;

/**
 * Operands for constants.
 * 
 * @author Simon Greatrix
 * 
 */
public enum CodeConstant implements CodeOperand {
    /** Produce an ICONST instruction */
    ICONST {
        /** Compile integer constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            checkSize(toks);
            
            // some ops have special codes
            int i = Code.getInt(cd, toks.get(2), toks.get(0));
            int ic = i + 1;
            if( 0 <= ic && ic < ICONST_VALS.length ) {
                code.appendU1(ICONST_VALS[ic]);
                return;
            }

            // try for byte push
            if( (Byte.MIN_VALUE <= i) && (i <= Byte.MAX_VALUE) ) {
                code.appendU1(OpCodes.BIPUSH);
                code.appendU1((byte) i);
                return;
            }

            // try for short push
            if( (Short.MIN_VALUE <= i) && (i <= Short.MAX_VALUE) ) {
                code.appendU1(OpCodes.SIPUSH);
                code.appendU2(i);
                return;
            }

            // have to use a constant
            CodeLDC.compile(code,code.getConstantRef(Integer.valueOf(i)));
        }
    },

    /** Reference to a numeric constant from the replacements */
    NUMBER {
        /** Compile integer constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            checkSize(toks);
            // compile numeric constant
            String t2 = toks.get(2);
            String r2 = Code.isReplacement(t2);
            if( r2 == null )
                throw new YabelReplacementRequiredException(toks,1);
            Number n = cd.getSafe(Number.class, r2);
            int val = code.getConstantRef(n);
            code.appendU2(val);
        }
    },

    /** A constant from the replacements */
    CONST {
        /** Compile integer constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            // compile type-value constant
            if( toks.size() != 4 )
                throw new YabelWrongTokenCountException(toks,2,"type and value");

            // replacements already handled
            int val = code.getConstantRef(toks.get(0), toks.get(2), toks.get(3));
            code.appendU2(val);
        }
    },

    /** A string constant */
    STRING {
        /** Compile string constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            // compile String. Replacements already handled.
            String k = toks.get(2);
            int val = code.getConstantRef(k);
            code.appendU2(val);
        }
    },

    /** An integer constant */
    INT {
        /** Compile int constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            String t2 = toks.get(2);
            String r2 = Code.isReplacement(t2);
            Integer n = null;
            if( r2 != null ) {
                n = cd.getSafe(Integer.class, r2);
            } else {
                try {
                    n = Integer.valueOf(t2);
                } catch (NumberFormatException nfe) {
                    throw new YabelBadNumberException(toks,2,"integer");
                }
            }
            int val = code.getConstantRef(n);
            code.appendU2(val);
        }
    },
    
    /** A float constant */
    FLOAT {
        /** Compile float constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            String t2 = toks.get(2);
            String r2 = Code.isReplacement(t2);
            Float n = null;
            if( r2 != null ) {
                n = cd.getSafe(Float.class, r2);
            } else {
                try {
                    n = Float.valueOf(t2);
                } catch (NumberFormatException nfe) {
                    throw new YabelBadNumberException(toks,2,"float");

                }
            }
            int val = code.getConstantRef(n);
            code.appendU2(val);
        }
    },
    
    /** A long constant */
    LONG {
        /** Compile long constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            String t2 = toks.get(2);
            String r2 = Code.isReplacement(t2);
            Long n = null;
            if( r2 != null ) {
                n = cd.getSafe(Long.class, r2);
            } else {
                try {
                    n = Long.valueOf(t2);
                } catch (NumberFormatException nfe) {
                    throw new YabelBadNumberException(toks,2,"long");

                }
            }
            int val = code.getConstantRef(n);
            code.appendU2(val);
        }
    },
    
    /** A double constant */
    DOUBLE {
        /** Compile double constant. {@inheritDoc} */
        @Override
        public void compile(Code code, List<String> toks, ClassData cd) {
            String t2 = toks.get(2);
            String r2 = Code.isReplacement(t2);
            Double n = null;
            if( r2 != null ) {
                n = cd.getSafe(Double.class, r2);
            } else {
                try {
                    n = Double.valueOf(t2);
                } catch (NumberFormatException nfe) {
                    throw new YabelBadNumberException(toks,2,"double");

                }
            }
            int val = code.getConstantRef(n);
            code.appendU2(val);
        }
    };

    /** ICONST instructions in order */
    static final byte[] ICONST_VALS = new byte[] { OpCodes.ICONST_M1,
            OpCodes.ICONST_0, OpCodes.ICONST_1, OpCodes.ICONST_2,
            OpCodes.ICONST_3, OpCodes.ICONST_4, OpCodes.ICONST_5 };   

    /**
     * Most of the constants require a single parameter, the constant.
     * 
     * @param toks
     *            the tokens for the operand
     */
    protected static void checkSize(List<String> toks) {
        if( toks.size() != 3 )
            throw new YabelWrongTokenCountException(toks,1,"value");
    }


    /** Compile integer constant. {@inheritDoc} */
    @Override
    abstract public void compile(Code code, List<String> toks, ClassData cd);
}