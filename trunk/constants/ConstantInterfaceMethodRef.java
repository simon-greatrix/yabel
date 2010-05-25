package yabel.constants;


import java.io.IOException;
import java.io.InputStream;

/**
 * Interface method reference constant.
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantInterfaceMethodRef extends ConstantRef {
    /**
     * New interface method reference
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
    ConstantInterfaceMethodRef(ConstantPool cp, int clss, int method,
            int type) {
        super(cp, clss, method, type);
    }


    /**
     * New interface method reference
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
    public ConstantInterfaceMethodRef(ConstantPool cp, String clss, String method,
            String type) {
        super(cp, clss, method, type);
    }


    /**
     * Read interface method reference from stream
     * 
     * @param input
     *            stream
     */
    ConstantInterfaceMethodRef(InputStream input) throws IOException {
        super(input);
    }


    /** {@inheritDoc} */
    @Override
    protected int getTag() {
        return 11;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":InterfaceMethodRef[ " + class_ + "," + field_
                + " ]";
    }
}