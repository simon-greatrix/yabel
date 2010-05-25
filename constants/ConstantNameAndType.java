package yabel.constants;


import yabel.io.IO;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Name and Type constant
 * 
 * @author Simon Greatrix
 * 
 */
class ConstantNameAndType extends Constant {
    /** Name reference */
    final int name_;

    /** Type reference */
    final int type_;


    /**
     * New name and type constant
     * 
     * @param cp
     *            constant pool
     * @param name
     *            name index
     * @param type
     *            type index
     */
    ConstantNameAndType(ConstantPool cp, int name, int type) {
        name_ = name;
        type_ = type;
        canonicalize(cp);
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
    ConstantNameAndType(ConstantPool cp, String name, String type) {
        name_ = cp.getUtf8(name);
        type_ = cp.getUtf8(type);
        canonicalize(cp);
    }


    /**
     * Read name and type constant
     * 
     * @param input
     *            stream
     */
    ConstantNameAndType(InputStream input) throws IOException {
        name_ = IO.readU2(input);
        type_ = IO.readU2(input);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj instanceof ConstantNameAndType ) {
            ConstantNameAndType other = (ConstantNameAndType) obj;
            return (other.name_ == name_) && (other.type_ == type_);
        }
        return false;
    }


    /**
     * Get the name from the constant pool
     * 
     * @param cp
     *            the pool
     * @return the name
     */
    public String getName(ConstantPool cp) {
        return cp.validate(name_, ConstantUtf8.class).get();
    }


    /**
     * Get the type from the constant pool
     * 
     * @param cp
     *            the pool
     * @return the type
     */
    public String getType(ConstantPool cp) {
        return cp.validate(type_, ConstantUtf8.class).get();
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ((name_ << 16) | type_) ^ 12;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return index_ + ":NameAndType[ " + name_ + "," + type_ + " ]";
    }


    /**
     * Referenced constants must be a UTF8 constants
     * 
     * @param cp
     *            the constant pool
     */
    @Override
    void validate(ConstantPool cp) {
        cp.validate(name_, ConstantUtf8.class);
        cp.validate(type_, ConstantUtf8.class);
    }


    /** {@inheritDoc} */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        baos.write(12);
        IO.writeU2(baos, name_);
        IO.writeU2(baos, type_);
    }
}