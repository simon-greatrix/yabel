package yabel.code.operand;

import java.util.List;

import yabel.ClassData;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;
import yabel.code.YabelBadVariableException;
import yabel.code.YabelWrongTokenCountException;

/**
 * A variable definition. The possible formats are:
 * <p>
 * <dl>
 * <dt>$:type:name
 * <dd>Define a variable in the first available slot
 * <dt>$:index:type:name
 * <dd>Define a variable in the specified slot
 * <dt>$:name
 * <dd>Undefine the variable
 * </dl>
 * The types "-" and "=" may be used to indicate a single or double width
 * variable of no specific type.
 * <p>
 * 
 * @author Simon Greatrix
 */
public class CodeVar implements CodeOperand {
    /** Variable definition */
    public static class Var {
        /**
         * Does the specified type indicate a double width variable?
         * 
         * @param type
         *            the type
         * @return true if double width
         */
        public static boolean isDouble(String type) {
            return type.equals("J") || type.equals("D") || type.equals("=");
        }

        /** Variable slot used by this variable */
        int index_;

        /** Name of this variable */
        final String name_;

        /** Type of this variable */
        final String type_;


        /**
         * 
         * @param index
         *            this variable's index
         * @param name
         *            this variable's name
         * @param type
         *            this variable's type
         */
        public Var(int index, String name, String type) {
            super();
            this.index_ = index;
            this.name_ = name;
            this.type_ = type;

            if( (name == null) || (name.length() == 0) )
                throw new YabelBadVariableException(
                        "Variable name cannot be null nor empty");
            if( !Character.isJavaIdentifierStart(name.charAt(0)) )
                throw new YabelBadVariableException(
                        "Variable name must start with a valid Java identifier start, not '"
                                + name.charAt(0) + "'");
            for(int i = 1;i < name.length();i++) {
                if( !Character.isJavaIdentifierPart(name.charAt(i)) )
                    throw new YabelBadVariableException(
                            "Variable name must use valid Java identifier characters, not '"
                                    + name.charAt(i) + "'");
            }
        }


        public int getIndex() {
            return index_;
        }


        public String getName() {
            return name_;
        }


        public String getType() {
            return type_;
        }


        public boolean isDouble() {
            return isDouble(type_);
        }


        public void setIndex(int index) {
            index_ = index;
        }


        /** {@inheritDoc} */
        @Override
        public String toString() {
            return String.format("Var [index_%s, name=%s, type=%s]",
                    Integer.valueOf(index_), name_, type_);
        }

    }

    /** The instance of this operand */
    public static final CodeVar INSTANCE = new CodeVar();


    /**
     * {@inheritDoc}
     * 
     * @see yabel.code.CodeOperand#compile(yabel.code.CompilerOutput,
     *      java.util.List, yabel.ClassData)
     */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
        // raw : $ : [[ index : ] type : ] name
        switch (toks.size()) {
        case 3:
            String name = toks.get(2);
            code.undefineVariable(name);
            break;
        case 4:
            String type = toks.get(2);
            name = toks.get(3);
            code.defineVariable(-1, name, type);
            break;
        case 5:
            String fld = toks.get(2);
            int index = CompilerOutput.getInt(fld, toks.get(0));
            type = toks.get(3);
            name = toks.get(4);
            code.defineVariable(index, name, type);
            break;
        default:
            throw new YabelWrongTokenCountException(toks, 1, "name", 2,
                    "type and name", 3, "index, type and name");
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.code.CodeOperand#name()
     */
    @Override
    public String name() {
        return "$";
    }
}
