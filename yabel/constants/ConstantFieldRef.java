package yabel.constants;


import java.io.IOException;
import java.io.InputStream;

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
     *            class reference
     * @param field
     *            field reference
     * @param type
     *            type reference
     */
    public ConstantFieldRef(ConstantPool cp, int clss, int field, int type) {
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
    public ConstantFieldRef(ConstantPool cp, String clss, String field, String type) {
        super(cp, clss, field, type);
    }


    /**
     * Read field reference from stream
     * 
     * @param input
     *            stream
     */
    ConstantFieldRef(InputStream input) throws IOException {
        super(input);
    }


    /** {@inheritDoc} */
    @Override
    protected int getTag() {
        return 9;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":FieldRef[ " + class_ + "," + field_ + " ]";
    }
}