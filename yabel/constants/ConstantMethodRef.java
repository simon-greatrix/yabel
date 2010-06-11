package yabel.constants;

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
     *            class name
     * @param type
     *            method name and type
     */
    public ConstantMethodRef(ConstantPool cp, ConstantClass clss,
            ConstantNameAndType type) {
        super(cp, clss, type);
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
    public ConstantMethodRef(ConstantPool cp, ConstantUtf8 clss,
            ConstantUtf8 method, ConstantUtf8 type) {
        super(cp, clss, method, type);
    }


    /**
     * Create method reference.
     * 
     * @param cp
     *            constant pool
     * @param value
     *            constant to resolve
     */
    ConstantMethodRef(ConstantPool cp, Unresolved value) {
        super(cp, value);
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


    /** {@inheritDoc} */
    @Override
    protected int getTag() {
        return 10;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":MethodRef[ " + class_ + "," + type_ + " ]";
    }
}