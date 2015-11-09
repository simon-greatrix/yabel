package yabel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;

import yabel.attributes.AttributeList;
import yabel.attributes.ConstantValue;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;
import yabel.io.IO;
import yabel2.Access;

/**
 * Field in a class
 * 
 * @author Simon Greatrix
 * 
 */
public class Field {
    /**
     * Get an inherited named field if it exists.
     * 
     * @param cp
     *            the ConstantPool for this class
     * @param name
     *            the field's name
     * @param cls
     *            the super class
     * @param samePackage
     *            true if the super class is in the same package
     * @return the Field or null
     */
    public static Field getInheritedField(ConstantPool cp, String name,
            Class<?> cls, boolean samePackage) {
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        for(java.lang.reflect.Field f:fields) {
            if( !f.getName().equals(name) ) continue;

            // check it is inheritable
            int m = f.getModifiers();

            // private members are not inherited
            if( Modifier.isPrivate(m) ) continue;

            // must be either public, protected or in same package
            if( !(samePackage || Modifier.isProtected(m) || Modifier.isPublic(m)) )
                continue;

            // have found it
            return new Field(cp, f);
        }

        Class<?> superCls = cls.getSuperclass();
        if( superCls != null )
            return getInheritedField(cp, name, superCls, samePackage
                    && cls.getPackage().equals(superCls.getPackage()));

        // not found
        return null;
    }

    /** Field access modifier */
    final int access_;

    /** Attribute list including ConstantValue if any */
    final AttributeList attrList_;

    /** Field name */
    final ConstantUtf8 name_;

    /** Field type */
    final ConstantUtf8 type_;


    /**
     * New field from a field description
     * 
     * @param cp
     *            the constant pool
     * @param data
     *            the description of the field
     */
    Field(ConstantPool cp, ClassData data) {
        access_ = Access.accessCode(data.get(String.class, "access", ""));
        name_ = new ConstantUtf8(cp, data.getSafe(String.class, "name"));
        type_ = new ConstantUtf8(cp, data.getSafe(String.class, "type"));
        attrList_ = new AttributeList(cp, data.getList(ClassData.class,
                "attributes"));
    }


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


    /**
     * New field from a <code>java.lang.reflect.Field</code>
     * 
     * @param cp
     *            the constant pool
     * @param fld
     *            the <code>java.lang.reflect.Field</code>
     */
    Field(ConstantPool cp, java.lang.reflect.Field fld) {
        access_ = Access.accessCode(fld);
        name_ = new ConstantUtf8(cp, fld.getName());
        type_ = new ConstantUtf8(cp, ClassUtil.getTypeName(fld.getType()));
        attrList_ = new AttributeList();
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
        cd.put("access", Access.accessCode(access_));
        cd.put("name", name_.get());
        cd.put("type", type_.get());
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