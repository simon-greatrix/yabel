package yabel;

import yabel.io.IO;

import yabel.attributes.AttributeList;
import yabel.attributes.ConstantValue;
import yabel.constants.ConstantPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Field in a class
 * 
 * @author Simon Greatrix
 * 
 */
public class Field {
    /** Field access modifier */
    final int access_;

    /** Attribute list including ConstantValue if any */
    final AttributeList attrList_;

    /** Field name */
    final int name_;

    /** Field type */
    final int type_;


    /**
     * Load a field definition from the input
     * 
     * @param cp
     *            the associated constant pool
     * @param input
     *            the stream
     * @throws IOException
     */
    Field(ConstantPool cp, InputStream input) throws IOException {
        access_ = IO.readU2(input);
        name_ = IO.readU2(input);
        type_ = IO.readU2(input);
        attrList_ = new AttributeList(cp, input);
    }


    /**
     * New field.
     * 
     * @param cp
     *            constant pool
     * @param access
     *            access modifier
     * @param name
     *            name
     * @param type
     *            type
     * @param value
     *            ConstantValue
     */
    Field(ConstantPool cp, int access, String name, String type,
            ConstantValue value) {
        access_ = access;
        name_ = cp.getUtf8(name);
        type_ = cp.getUtf8(type);
        attrList_ = new AttributeList();
        attrList_.set(value);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if( obj instanceof Field ) {
            Field other = (Field) obj;
            return other.name_ == name_;
        }
        return false;

    }


    /**
     * Get the access code of this field
     * 
     * @return the access code
     */
    public int getAccessCode() {
        return access_;
    }


    /**
     * Get the attributes of this field.
     * 
     * @return the attributes
     */
    public AttributeList getAttributes() {
        return attrList_;
    }


    /**
     * Get the Constant Pool ID of the name of this field
     * 
     * @return the name's ID
     */
    public int getNameID() {
        return name_;
    }


    /**
     * Get the Constant Pool ID of the type of this field
     * 
     * @return the field's ID
     */
    public int getTypeID() {
        return type_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return name_ << 8;
    }


    /**
     * Write this Field to the output stream
     * 
     * @param baos
     *            output stream
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, access_);
        IO.writeU2(baos, name_);
        IO.writeU2(baos, type_);
        attrList_.writeTo(baos);
    }
}