package yabel.constants;

import java.io.ByteArrayOutputStream;

import yabel.io.IO;

/**
 * An abstract reference in the constant pool
 * 
 * @author Simon Greatrix
 * 
 */
public abstract class ConstantRef extends Constant {
    /** The referenced class */
    protected final ConstantClass class_;

    /** The referenced NameAndType of the class */
    protected final ConstantNameAndType type_;


    /**
     * New reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class
     * @param type
     *            referenced thing
     */
    ConstantRef(ConstantPool cp, ConstantClass clss, ConstantNameAndType type) {
        class_ = clss;
        type_ = type;
        canonicalize(cp);
    }


    /**
     * New reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class
     * @param ref
     *            referenced thing
     * @param type
     *            type
     */
    ConstantRef(ConstantPool cp, ConstantUtf8 clss, ConstantUtf8 ref,
            ConstantUtf8 type) {
        class_ = new ConstantClass(cp, clss);
        type_ = new ConstantNameAndType(cp, ref, type);
        canonicalize(cp);
    }


    /**
     * New reference
     * 
     * @param cp
     *            constant pool
     * @param value
     *            constant to resolve
     */
    ConstantRef(ConstantPool cp, Unresolved value) {
        class_ = cp.validate(value.getValue1(), ConstantClass.class);
        type_ = cp.validate(value.getValue2(), ConstantNameAndType.class);
        index_ = value.index_;
    }


    /**
     * New reference
     * 
     * @param cp
     *            constant pool
     * @param clss
     *            class
     * @param ref
     *            referenced thing
     * @param type
     *            type
     */
    ConstantRef(ConstantPool cp, String clss, String ref, String type) {
        class_ = new ConstantClass(cp, clss);
        type_ = new ConstantNameAndType(cp, ref, type);
        canonicalize(cp);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj == null ) return false;
        if( obj == this ) return true;
        if( obj instanceof ConstantRef ) {
            ConstantRef other = (ConstantRef) obj;
            return other.class_.equals(class_) && other.type_.equals(type_);
        }
        return false;
    }


    /**
     * Get the class name this reference constants refers to.
     * 
     * @return the class name
     */
    public ConstantUtf8 getClassName() {
        return class_.getClassName();
    }


    /**
     * Get the name of the type of the field or method this reference refers to.
     * 
     * @return the type name
     */
    public ConstantUtf8 getName() {
        return type_.getName();
    }


    /**
     * Get the name of the type of the field or method this reference refers to.
     * 
     * @return the type name
     */
    public ConstantNameAndType getNameAndType() {
        return type_;
    }


    /**
     * Get the tag used for this reference type.
     * 
     * @return tag used
     */
    abstract protected int getTag();


    /**
     * Get the name of the type of the field or method this reference refers to.
     * 
     * @return the type name
     */
    public ConstantUtf8 getType() {
        return type_.getType();
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (class_.hashCode() << 16) ^ type_.hashCode() ^ getTag();
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(getTag());
        IO.writeU2(baos, class_.getIndex());
        IO.writeU2(baos, type_.getIndex());
    }
}