package yabel;

import yabel.io.IO;

import yabel.code.Code;

import yabel.attributes.*;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Method specification
 * 
 * @author Simon Greatrix
 * 
 */
public class Method implements AttributeListListener {
    /** Access modifier */
    private final int access_;

    /** Attributes of this method */
    private final AttributeList attrList_;

    /**
     * ClassBuilder this is a method of
     */
    private final ClassBuilder classBuilder_;

    /** The constant pool for this class */
    private final ConstantPool cp_;

    /** Method name */
    ConstantUtf8 name_;

    /** Method type */
    final ConstantUtf8 type_;


    /**
     * Create a method from ClassData
     * 
     * @param classBuilder
     *            the class the method will be a part of
     * @param data
     *            the description of this method
     */
    Method(ClassBuilder classBuilder, ClassData data) {
        classBuilder_ = classBuilder;
        cp_ = classBuilder_.getConstantPool();
        access_ = ClassBuilder.accessCode(data.get(String.class, "access",""));
        name_ = new ConstantUtf8(cp_, data.getSafe(String.class, "name"));
        type_ = new ConstantUtf8(cp_, data.getSafe(String.class, "type"));
        attrList_ = new AttributeList(cp_, data.getList(ClassData.class,
                "attributes"));
        attrList_.setOwner(this);
    }


    /**
     * Read a method from the input.
     * 
     * @param input
     *            the stream
     * @param classBuilder
     *            Class containing this method
     * @throws IOException
     */
    Method(ClassBuilder classBuilder, InputStream input) throws IOException {
        classBuilder_ = classBuilder;
        cp_ = classBuilder_.getConstantPool();
        access_ = IO.readU2(input);
        int id = IO.readU2(input);
        name_ = cp_.validate(id, ConstantUtf8.class);
        id = IO.readU2(input);
        type_ = cp_.validate(id, ConstantUtf8.class);
        attrList_ = new AttributeList(cp_, input);
        attrList_.setOwner(this);
    }


    /**
     * New method
     * 
     * @param access
     *            access modifier
     * @param name
     *            name
     * @param type
     *            type
     * @param classBuilder
     *            Class containing this method
     */
    Method(ClassBuilder classBuilder, int access, String name, String type) {
        classBuilder_ = classBuilder;
        cp_ = classBuilder.getConstantPool();
        access_ = access;
        name_ = new ConstantUtf8(cp_, name);
        type_ = new ConstantUtf8(cp_, type);
        attrList_ = new AttributeList();
        attrList_.setOwner(this);
    }


    /**
     * The attributes of this method have changed. The "Code" attribute needs to
     * be told which class and method it is part of.
     */
    public void attributesChanged() {
        Code c = (Code) attrList_.get(cp_, Attribute.ATTR_CODE);
        if( c != null ) c.setOwner(classBuilder_, this);
    }


    /**
     * Get the access modifier.
     * 
     * @return the modifier
     */
    public int getAccess() {
        return access_;
    }


    /**
     * Get the attributes for this method.
     * 
     * @return the attributes
     */
    public AttributeList getAttributes() {
        return attrList_;
    }


    /**
     * Get associated code attribute
     * 
     * @return code
     */
    public Code getCode() {
        Code code = (Code) attrList_.get(cp_, Attribute.ATTR_CODE);
        if( code == null ) {
            code = new Code(cp_);
            attrList_.set(code);
        }
        return code;
    }


    /**
     * Get the constant pool for this method
     * 
     * @return the constant pool
     */
    public ConstantPool getConstantPool() {
        return cp_;
    }


    /**
     * Get associated exceptions attribute
     * 
     * @return exceptions
     */
    public Exceptions getExceptions() {
        Exceptions excep = (Exceptions) attrList_.get(cp_,
                Attribute.ATTR_EXCEPTIONS);
        if( excep == null ) {
            excep = new Exceptions(cp_);
            attrList_.set(excep);
        }
        return excep;
    }


    /**
     * Get the name of this method.
     * 
     * @return this method's name
     */
    public ConstantUtf8 getName() {
        return name_;
    }


    /**
     * Get the type of this method.
     * 
     * @return this method's name
     */
    public ConstantUtf8 getType() {
        return type_;
    }


    /**
     * Set the name of this method. Note that the original method name remains
     * in the constant pool.
     * 
     * @param newName
     *            the new name for this method
     */
    public void setName(String newName) {
        name_ = new ConstantUtf8(cp_, newName);
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
        cd.put("type", type_.get());
        cd.putList(ClassData.class, "attributes", attrList_.toClassData());
        return cd;
    }


    /**
     * Write Method to output
     * 
     * @param baos
     *            output
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, access_);
        IO.writeU2(baos, name_.getIndex());
        IO.writeU2(baos, type_.getIndex());
        attrList_.writeTo(baos);
    }
}