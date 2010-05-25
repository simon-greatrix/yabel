package yabel.constants;


import yabel.io.IO;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Class Reference constant
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantClass extends Constant {
    /** Reference to Utf8 class name */
    final int name_;


    /**
     * Create new Class Reference.
     * 
     * @param cp
     *            constant pool
     * @param name
     *            index of class name
     */
    public ConstantClass(ConstantPool cp, int name) {
        name_ = name;
        canonicalize(cp);
    }


    /**
     * Create new Class Reference.
     * 
     * @param cp
     *            constant pool
     * @param name
     *            class name
     */
    public ConstantClass(ConstantPool cp, String name) {
        name_ = cp.getUtf8(name);
        canonicalize(cp);
    }


    /**
     * Create new Class Reference.
     * 
     * @param in
     *            input stream
     */
    public ConstantClass(InputStream in) throws IOException {
        name_ = IO.readU2(in);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj instanceof ConstantClass ) {
            ConstantClass other = (ConstantClass) obj;
            return name_ == other.name_;
        }
        return false;
    }


    /**
     * Get the name constant index.
     * 
     * @return the index of the Utf8 name
     */
    public int get() {
        return name_;
    }


    /**
     * Get the class name from the constant pool
     * 
     * @param cp
     *            the pool
     * @return the name
     */
    public String getClass(ConstantPool cp) {
        return cp.validate(name_, ConstantUtf8.class).get();
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return name_ ^ 7;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":Class[ " + name_ + " ]";
    }


    /**
     * Referenced constant must be a UTF8 constant
     * 
     * @param cp
     *            the constant pool
     */
    @Override
    void validate(ConstantPool cp) {
        cp.validate(name_, ConstantUtf8.class);
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(7);
        IO.writeU2(baos, name_);
    }
}