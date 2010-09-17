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
     * Read an attribute list
     * 
     * @param cp
     *            the constant pool
     * @param data
     *            list of class data defining the attributes
     */
    public AttributeList(ConstantPool cp, List<ClassData> data) {
        if( data != null ) {
            for(ClassData cd:data) {
                attrs_.add(read(cp, cd));
            }
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
        return get(index);
    }


    /**
     * Get an identified attribute from this list.
     * 
     * @param index
     *            the index of the Utf8 constant which holds the name of the
     *            attribute
     * @return the attribute, or null if not matched
     */
    public Attribute get(int index) {
        for(Attribute a:attrs_) {
            if( a.getAttrId().getIndex() == index ) return a;
        }
        return null;
    }


    /**
     * Get a named attribute from this list.
     * 
     * @param cp
     *            the constant pool where the name is stored
     * @param name
     *            the name
     * @return the attributes
     */
    public List<Attribute> getAll(ConstantPool cp, String name) {
        int index = cp.getUtf8(name, false);
        return getAll(index);
    }


    /**
     * Get an identified attribute from this list.
     * 
     * @param index
     *            the index of the Utf8 constant which holds the name of the
     *            attribute
     * @return the attributes
     */
    public List<Attribute> getAll(int index) {
        List<Attribute> attrs = new ArrayList<Attribute>();
        for(Attribute a:attrs_) {
            if( a.getAttrId().getIndex() == index ) attrs.add(a);
        }
        return attrs;
    }


    /**
     * Read an attribute from class data
     * 
     * @param cp
     *            the constant pool associated with this list
     * @param input
     *            the class data
     * @return resolved attribute
     */
    protected Attribute read(ConstantPool cp, ClassData input) {
        String idName = input.getSafe(String.class, "name");

        if( idName.equals(Attribute.ATTR_CODE) ) return new Code(cp, input);
        if( idName.equals(Attribute.ATTR_CONSTANT_VALUE) )
            return new ConstantValue(cp, input);
        if( idName.equals(Attribute.ATTR_EXCEPTIONS) )
            return new Exceptions(cp, input);
        // TODO if( idName.equals(ATTR_INNER_CLASSES) )
        if( idName.equals(Attribute.ATTR_SYNTHETIC) )
            return new MarkerAttribute(cp, input);
        if( idName.equals(Attribute.ATTR_SOURCE_FILE) )
            return new SourceFileAttribute(cp, input);
        if( idName.equals(Attribute.ATTR_LINE_NUMBER_TABLE) )
            return new LineNumberTable(this, cp, input);
        // TODO if( idName.equals(ATTR_LOCAL_VARIABLE_TABLE) )
        if( idName.equals(Attribute.ATTR_DEPRECATED) )
            return new MarkerAttribute(cp, input);

        return new GenericAttribute(cp, input);
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
        if( idName.equals(Attribute.ATTR_LINE_NUMBER_TABLE) )
            return new LineNumberTable(this, cp, input);
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
     * @return true if an attribute with the given name was found
     */
    public boolean remove(ConstantPool cp, String name) {
        int index = cp.getUtf8(name, false);
        for(int i = 0;i < attrs_.size();i++) {
            Attribute a = attrs_.get(i);
            if( a.getAttrId().getIndex() == index ) {
                attrs_.remove(i);
                if( owner_ != null ) owner_.attributeChanged(name, null);
                return true;
            }
        }
        return false;
    }
    

    /**
     * Set an attribute into this list.
     * 
     * @param attr
     *            the attribute
     */
    public void set(Attribute attr) {
        if( attr == null ) return;

        boolean found = false;
        for(int i = 0;i < attrs_.size();i++) {
            Attribute a = attrs_.get(i);
            if( a.getAttrId() == attr.getAttrId() ) {
                attrs_.set(i, attr);
                found = true;
                break;
            }
        }
        if( !found ) attrs_.add(attr);
        if( owner_ != null )
            owner_.attributeChanged(attr.getAttrId().get(), attr);
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
        if( owner_ != null ) {
            for(Attribute a:attrs_) {
                owner_.attributeChanged(a.getAttrId().get(), a);
            }
        }
    }


    /**
     * Get the ClassData representations of all the attributes in this pool
     * 
     * @return the representations
     */
    public List<ClassData> toClassData() {
        List<ClassData> list = new ArrayList<ClassData>(attrs_.size());
        for(Attribute attr:attrs_) {
            list.add(attr.toClassData());
        }
        return list;
    }


    /**
     * Notify any owner of this list that the specified attribute has changed.
     * The attribute must be in this list.
     * 
     * @param attr
     *            the attribute that has changed
     */
    public void update(Attribute attr) {
        if( attr == null )
            throw new IllegalArgumentException("Attribute unspecified");
        if( owner_ == null ) return;

        for(Attribute a:attrs_) {
            if( a == attr ) {
                owner_.attributeChanged(attr.getAttrId().get(), attr);
                return;
            }
        }

        throw new IllegalStateException("Attribute not in list");
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