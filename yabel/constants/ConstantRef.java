package yabel.constants;

import yabel.io.IO;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract reference in the constant pool
 * 
 * @author Simon Greatrix
 * 
 */
public abstract class ConstantRef extends Constant {
    /** The referenced class */
    protected final int class_;

    /** The referenced NameAndType of the class */
    protected final int field_;


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
    ConstantRef(ConstantPool cp, int clss, int ref, int type) {
        class_ = clss;
        Constant cnt = new ConstantNameAndType(cp, ref, type);
        field_ = cnt.getIndex();
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
    ConstantRef(ConstantPool cp, String clss, String ref, String type) {
        Constant cc = new ConstantClass(cp, clss);
        class_ = cc.getIndex();
        Constant cnt = new ConstantNameAndType(cp, ref, type);
        field_ = cnt.getIndex();
        canonicalize(cp);
    }


    /**
     * Read reference constant
     * 
     * @param input
     *            input stream
     * @throws IOException
     */
    ConstantRef(InputStream input) throws IOException {
        class_ = IO.readU2(input);
        field_ = IO.readU2(input);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj instanceof ConstantRef ) {
            ConstantRef other = (ConstantRef) obj;
            return (other.class_ == class_) && (other.field_ == field_);
        }
        return false;
    }


    /**
     * Get the class name this reference constants refers to.
     * 
     * @param cp
     *            the ConstantPool where the class name is stored
     * @return the class name
     */
    public String getClass(ConstantPool cp) {
        return cp.validate(class_, ConstantClass.class).getClass(cp);
    }


    /**
     * Get the method or field name this reference constants refers to.
     * 
     * @param cp
     *            the ConstantPool where the class name is stored
     * @return the class name
     */
    public String getName(ConstantPool cp) {
        return cp.validate(field_, ConstantNameAndType.class).getName(cp);
    }


    /**
     * Get the tag used for this reference type.
     * 
     * @return tag used
     */
    abstract protected int getTag();


    /**
     * Get the name of the type of the field or method this reference refers
     * to.
     * 
     * @param cp
     *            the ConstantPool containing the type name
     * @return the type name
     */
    public String getType(ConstantPool cp) {
        return cp.validate(field_, ConstantNameAndType.class).getType(cp);
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (class_ << 16) ^ field_ ^ getTag();
    }


    /** {@inheritDoc} */
    @Override
    void validate(ConstantPool cp) {
        cp.validate(class_, ConstantClass.class);
        cp.validate(field_, ConstantNameAndType.class);
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(getTag());
        IO.writeU2(baos, class_);
        IO.writeU2(baos, field_);
    }
}