package yabel.attributes;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import yabel.ClassData;
import yabel.code.Code;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;
import yabel.io.IO;

/**
 * List of attributes associated with part of the class.
 * 
 * @author Simon Greatrix
 * 
 */
public class AttributeList {
    /** The attributes in this list */
    private List<Attribute> attrs_ = new ArrayList<Attribute>();

    /** The owner of this list */
    private AttributeListListener owner_ = null;


    /** Create an empty attribute list */
    public AttributeList() {
    // do nothing
    }


    /**
     * Read an attribute list
     * 
     * @param cp
     *            the constant pool
     * @param input
     *            the stream
     * @throws IOException
     */
    public AttributeList(ConstantPool cp, InputStream input) throws IOException {
        int cnt = IO.readU2(input);
        for(int i = 0;i < cnt;i++) {
            attrs_.add(read(cp, input));
        }
    }


    /**
     * Get a named attribute from this list.
     * 
     * @param cp
     *            the constant pool where the name is stored
     * @param name
     *            the name
     * @return the attribute, or null if not matched
     */
    public Attribute get(ConstantPool cp, String name) {
        int index = cp.getUtf8(name, false);
        for(Attribute a:attrs_) {
            if( a.getAttrId().getIndex() == index ) return a;
        }
        return null;
    }


    /**
     * Read an attribute from the input stream
     * 
     * @param cp
     *            the constant pool associated with this list
     * @param input
     *            the stream
     * @return the attribute that was read
     * @throws IOException
     */
    protected Attribute read(ConstantPool cp, InputStream input) throws IOException {
        int id = IO.readU2(input);
        String idName = cp.validate(id, ConstantUtf8.class).get();

        if( idName.equals(Attribute.ATTR_CODE) ) return new Code(cp, input);
        if( idName.equals(Attribute.ATTR_CONSTANT_VALUE) )
            return new ConstantValue(cp, input);
        if( idName.equals(Attribute.ATTR_EXCEPTIONS) )
            return new Exceptions(cp, input);
        // TODO if( idName.equals(ATTR_INNER_CLASSES) )
        if( idName.equals(Attribute.ATTR_SYNTHETIC) )
            return new MarkerAttribute(cp, idName, input);
        if( idName.equals(Attribute.ATTR_SOURCE_FILE) )
            return new SourceFileAttribute(cp, input);
        // TODO if( idName.equals(ATTR_LINE_NUMBER_TABLE) )
        // TODO if( idName.equals(ATTR_LOCAL_VARIABLE_TABLE) )
        if( idName.equals(Attribute.ATTR_DEPRECATED) )
            return new MarkerAttribute(cp, idName, input);

        return new GenericAttribute(cp, id, input);
    }


    /**
     * Remove an attribute from this list. Note that the attribute's name
     * remains in the constant pool.
     * 
     * @param cp
     *            the constant pool where the attribute's name is stored
     * @param name
     *            the attribute's name
     */
    public void remove(ConstantPool cp, String name) {
        int index = cp.getUtf8(name, false);
        for(int i = 0;i < attrs_.size();i++) {
            Attribute a = attrs_.get(i);
            if( a.getAttrId().getIndex() == index ) {
                attrs_.remove(i);
                return;
            }
        }
        if( owner_ != null ) owner_.attributesChanged();
    }


    /**
     * Set an attribute into this list.
     * 
     * @param attr
     *            the attribute
     */
    public void set(Attribute attr) {
        for(int i = 0;i < attrs_.size();i++) {
            Attribute a = attrs_.get(i);
            if( a.getAttrId() == attr.getAttrId() ) {
                attrs_.set(i, attr);
            }
        }
        attrs_.add(attr);
        if( owner_ != null ) owner_.attributesChanged();
    }


    /**
     * Set the owner of this list. The owner is notified every time the
     * attribute list is changed.
     * 
     * @param owner
     *            the owner of this list
     */
    public void setOwner(AttributeListListener owner) {
        owner_ = owner;
        if( owner_ != null ) owner_.attributesChanged();
    }
    
    
    /**
     * Get the ClassData representations of all the attributes in this pool
     * @return the representations
     */
    public List<ClassData> toClassData() {
        List<ClassData> list = new ArrayList<ClassData>(attrs_.size());
        for(Attribute attr : attrs_) {
            list.add(attr.toClassData());
        }
        return list;
    }


    /**
     * Write this attribute list to the stream
     * 
     * @param baos
     *            the stream
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrs_.size());
        for(Attribute a:attrs_) {
            a.writeTo(baos);
        }
    }
}