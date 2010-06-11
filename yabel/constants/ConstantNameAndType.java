package yabel.constants;


import java.io.ByteArrayOutputStream;

import yabel.io.IO;

/**
 * Name and Type constant
 * 
 * @author Simon Greatrix
 * 
 */
class ConstantNameAndType extends Constant {
    /** Name reference */
    final ConstantUtf8 name_;

    /** Type reference */
    final ConstantUtf8 type_;


    /**
     * New name and type constant
     * 
     * @param cp
     *            constant pool
     * @param value value to resolve
     */
    ConstantNameAndType(ConstantPool cp, Unresolved value) {
        name_ = cp.validate(value.getValue1(),ConstantUtf8.class);
        type_ = cp.validate(value.getValue2(),ConstantUtf8.class);
        index_ = value.index_;
    }


    /**
     * New name and type constant
     * 
     * @param cp
     *            constant pool
     * @param name
     *            name
     * @param type
     *            type
     */
    public ConstantNameAndType(ConstantPool cp, String name, String type) {
        this(cp, new ConstantUtf8(cp,name), new ConstantUtf8(cp,type));
    }


    /**
     * New name and type constant
     * 
     * @param cp
     *            constant pool
     * @param name
     *            name
     * @param type
     *            type
     */
    public ConstantNameAndType(ConstantPool cp, ConstantUtf8 name, ConstantUtf8 type) {
        name_ = name;
        type_ = type;
        canonicalize(cp);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj instanceof ConstantNameAndType ) {
            ConstantNameAndType other = (ConstantNameAndType) obj;
            return other.name_.equals(name_) && other.type_.equals(type_);
        }
        return false;
    }


    /**
     * Get the name from the constant pool
     * 
     * @return the name
     */
    public ConstantUtf8 getName() {
        return name_;
    }


    /**
     * Get the type from the constant pool
     * 
     * @return the type
     */
    public ConstantUtf8 getType() {
        return type_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ((name_.getIndex() << 16) | type_.getIndex()) ^ 12;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":NameAndType[ " + name_ + "," + type_ + " ]";
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(12);
        IO.writeU2(baos, name_.getIndex());
        IO.writeU2(baos, type_.getIndex());
    }
}