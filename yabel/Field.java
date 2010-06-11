package yabel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yabel.attributes.AttributeList;
import yabel.attributes.ConstantValue;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;
import yabel.io.IO;

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
    final ConstantUtf8 name_;

    /** Field type */
    final ConstantUtf8 type_;


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
        name_ = cp.validate(IO.readU2(input), ConstantUtf8.class);
        type_ = cp.validate(IO.readU2(input), ConstantUtf8.class);
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
        name_ = new ConstantUtf8(cp, name);
        type_ = new ConstantUtf8(cp, name);
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
    public ConstantUtf8 getName() {
        return name_;
    }


    /**
     * Get the Constant Pool ID of the type of this field
     * 
     * @return the field's ID
     */
    public ConstantUtf8 getType() {
        return type_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return name_.getIndex() << 8;
    }


    /**
     * Get the ClassData representation of this
     * 
     * @return the representation
     */
    public ClassData toClassData() {
        ClassData cd = new ClassData();
        cd.put("access", ClassBuilder.accessCode(access_));
        cd.put("name", name_.get());
        cd.put("type", name_.get());
        cd.putList(ClassData.class, "attributes", attrList_.toClassData());
        return cd;
    }


    /**
     * Write this Field to the output stream
     * 
     * @param baos
     *            output stream
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, access_);
        IO.writeU2(baos, name_.getIndex());
        IO.writeU2(baos, type_.getIndex());
        attrList_.writeTo(baos);
    }
}