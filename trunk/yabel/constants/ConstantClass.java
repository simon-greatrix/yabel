package yabel.constants;

import java.io.ByteArrayOutputStream;

import yabel.io.IO;

/**
 * A Class Reference constant
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantClass extends Constant {
    /** Reference to Utf8 class name */
    final ConstantUtf8 name_;


    /**
     * Create new Class Reference.
     * 
     * @param cp
     *            constant pool
     * @param name
     *            class name
     */
    public ConstantClass(ConstantPool cp, ConstantUtf8 name) {
        name_ = name;
        canonicalize(cp);
    }


    /**
     * Create new Class Reference.
     * 
     * @param cp
     *            constant pool
     * @param value
     *            value to resolve
     */
    ConstantClass(ConstantPool cp, Unresolved value) {
        name_ = cp.validate(value.getValue1(), ConstantUtf8.class);
        index_ = value.index_;
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
        this(cp,new ConstantUtf8(cp, name));
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj == null ) return false;
        if( obj == this ) return true;
        if( obj instanceof ConstantClass ) {
            ConstantClass other = (ConstantClass) obj;
            return name_.equals(other.name_);
        }
        return false;
    }


    /**
     * Get the name constant index.
     * 
     * @return the index of the Utf8 name
     */
    public ConstantUtf8 get() {
        return name_;
    }


    /**
     * Get the class name from the constant pool
     * 
     * @return the name
     */
    public ConstantUtf8 getClassName() {
        return name_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return name_.getIndex() ^ 7;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":Class[ " + name_ + " ]";
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(7);
        IO.writeU2(baos, name_.getIndex());
    }
}