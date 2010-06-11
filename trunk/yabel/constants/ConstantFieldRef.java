package yabel.constants;

/**
 * A field reference constant
 * 
 * @author Simon Greatrix
 */
public class ConstantFieldRef extends ConstantRef {

    /**
     * New field reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class name
     * @param field
     *            field name
     * @param type
     *            type name
     */
    public ConstantFieldRef(ConstantPool cp, String clss, String field,
            String type) {
        super(cp, clss, field, type);
    }


    /**
     * New field reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class name
     * @param field
     *            field name
     * @param type
     *            type name
     */
    public ConstantFieldRef(ConstantPool cp, ConstantUtf8 clss,
            ConstantUtf8 field, ConstantUtf8 type) {
        super(cp, clss, field, type);
    }


    /**
     * New field reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class name
     * @param type
     *            type name
     */
    public ConstantFieldRef(ConstantPool cp, ConstantClass clss,
            ConstantNameAndType type) {
        super(cp, clss, type);
    }


    /**
     * Create field reference
     * 
     * @param cp
     *            constant pool
     * @param value
     *            constant to resolve
     */
    ConstantFieldRef(ConstantPool cp, Unresolved value) {
        super(cp, value);
    }


    /** {@inheritDoc} */
    @Override
    protected int getTag() {
        return 9;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":FieldRef[ " + class_ + "," + type_ + " ]";
    }
}