package yabel.constants;


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
     *            class name
     * @param type
     *            type name
     */
    public ConstantInterfaceMethodRef(ConstantPool cp, ConstantClass clss,
            ConstantNameAndType type) {
        super(cp, clss, type);
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
    public ConstantInterfaceMethodRef(ConstantPool cp, ConstantUtf8 clss,
            ConstantUtf8 method, ConstantUtf8 type) {
        super(cp, clss, method, type);
    }


    /**
     * Create interface method reference.
     * 
     * @param cp
     *            constant pool
     * @param value constant to resolve
     */
    ConstantInterfaceMethodRef(ConstantPool cp, Unresolved value) {
        super(cp, value);
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
    public ConstantInterfaceMethodRef(ConstantPool cp, String clss,
            String method, String type) {
        super(cp, clss, method, type);
    }


    /** {@inheritDoc} */
    @Override
    protected int getTag() {
        return 11;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":InterfaceMethodRef[ " + class_ + "," + type_ + " ]";
    }
}