package yabel.constants;


import java.io.IOException;
import java.io.InputStream;

/**
 * Method reference constant
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantMethodRef extends ConstantRef {
    /**
     * New method reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class reference
     * @param method
     *            method reference
     * @param type
     *            type reference
     */
    public ConstantMethodRef(ConstantPool cp, int clss, int method, int type) {
        super(cp, clss, method, type);
    }


    /**
     * New method reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class name
     * @param method
     *            method name
     * @param type
     *            type name
     */
    public ConstantMethodRef(ConstantPool cp, String clss, String method,
            String type) {
        super(cp, clss, method, type);
    }


    /**
     * Read method reference from stream
     * 
     * @param input
     *            stream
     */
    ConstantMethodRef(InputStream input) throws IOException {
        super(input);
    }


    /** {@inheritDoc} */
    @Override
    protected int getTag() {
        return 10;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":MethodRef[ " + class_ + "," + field_ + " ]";
    }
}