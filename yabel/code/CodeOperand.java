package yabel.code;

import java.util.List;

import yabel.ClassData;

/**
 * An object that can be compiled into byte code
 * 
 * @author Simon Greatrix
 * 
 */
public interface CodeOperand {
    /**
     * Return the name of this operand
     * 
     * @return the name of this operand
     */
    public String name();


    /**
     * Compile this operand on to the provided stream
     * 
     * @param code
     *            the code block being compiled
     * @param toks
     *            the tokens for this operand
     * @param cd
     *            the ClassData associated with this compilation
     */
    public void compile(Code code, List<String> toks, ClassData cd);
}
