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
    /**
     * ClassBuilder this is a method of
     */
    private final ClassBuilder classBuilder_;

    /** Access modifier */
    private final int accessMeth_;

    /** Attributes of this method */
    private final AttributeList methAttrList_;

    /** Method name */
    int name_;

    /** Method type */
    final int type_;


    /**
     * Read a method from the input.
     * 
     * @param input
     *            the stream
     * @param classBuilder TODO
     * @throws IOException
     */
    Method(ClassBuilder classBuilder, InputStream input) throws IOException {
        this.classBuilder_ = classBuilder;
        accessMeth_ = IO.readU2(input);
        name_ = IO.readU2(input);
        this.classBuilder_.cp_.validate(name_, ConstantUtf8.class);
        type_ = IO.readU2(input);
        this.classBuilder_.cp_.validate(type_, ConstantUtf8.class);
        methAttrList_ = new AttributeList(this.classBuilder_.cp_, input);
        methAttrList_.setOwner(this);
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
     * @param classBuilder TODO
     */
    Method(ClassBuilder classBuilder, int access, String name, String type) {
        this.classBuilder_ = classBuilder;
        accessMeth_ = access;
        name_ = this.classBuilder_.cp_.getUtf8(name);
        type_ = this.classBuilder_.cp_.getUtf8(type);
        methAttrList_ = new AttributeList();
        methAttrList_.setOwner(this);
    }


    /**
     * The attributes of this method have changed. The "Code" attribute
     * needs to be told which class and method it is part of.
     */
    public void attributesChanged() {
        Code c = (Code) methAttrList_.get(this.classBuilder_.cp_, Attribute.ATTR_CODE);
        if( c != null ) c.setOwner(this.classBuilder_, this);
    }


    /**
     * Get the access modifier.
     * 
     * @return the modifier
     */
    public int getAccess() {
        return accessMeth_;
    }


    /**
     * Get the attributes for this method.
     * 
     * @return the attributes
     */
    public AttributeList getAttributes() {
        return methAttrList_;
    }


    /**
     * Get associated code attribute
     * 
     * @return code
     */
    public Code getCode() {
        Code code = (Code) methAttrList_.get(this.classBuilder_.cp_, Attribute.ATTR_CODE);
        if( code == null ) {
            code = new Code(this.classBuilder_.cp_);
            methAttrList_.set(code);
        }
        return code;
    }


    /**
     * Get the constant pool for this method
     * 
     * @return the constant pool
     */
    public ConstantPool getConstantPool() {
        return this.classBuilder_.cp_;
    }


    /**
     * Get associated exceptions attribute
     * 
     * @return exceptions
     */
    public Exceptions getExceptions() {
        Exceptions excep = (Exceptions) methAttrList_.get(this.classBuilder_.cp_,
                Attribute.ATTR_EXCEPTIONS);
        if( excep == null ) {
            excep = new Exceptions(this.classBuilder_.cp_);
            methAttrList_.set(excep);
        }
        return excep;
    }


    /**
     * Get the name of this method.
     * 
     * @return this method's name
     */
    public String getName() {
        ConstantUtf8 co = this.classBuilder_.cp_.validate(name_, ConstantUtf8.class);
        return co.get();
    }


    /**
     * Get the type of this method.
     * 
     * @return this method's name
     */
    public String getType() {
        ConstantUtf8 co = this.classBuilder_.cp_.validate(type_, ConstantUtf8.class);
        return co.get();
    }


    /**
     * Set the name of this method. Note that the original method name
     * remains in the constant pool.
     * 
     * @param newName
     *            the new name for this method
     */
    public void setName(String newName) {
        name_ = this.classBuilder_.cp_.getUtf8(newName);
    }


    /**
     * Write Method to output
     * 
     * @param baos
     *            output
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, accessMeth_);
        IO.writeU2(baos, name_);
        IO.writeU2(baos, type_);
        methAttrList_.writeTo(baos);
    }
}