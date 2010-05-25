package yabel.attributes;

import yabel.io.IO;


import yabel.constants.ConstantClass;
import yabel.constants.ConstantPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Exceptions attribute
 * 
 * @author Simon Greatrix
 * 
 */
public class Exceptions extends Attribute {
    /** The associated constant pool */
    private final ConstantPool cp_;

    /** Exception handlers */
    private final List<ConstantClass> excepts_ = new ArrayList<ConstantClass>();


    /**
     * New exceptions attribute.
     * 
     * @param cp
     *            associated constant pool
     */
    public Exceptions(ConstantPool cp) {
        super(cp, Attribute.ATTR_EXCEPTIONS);
        cp_ = cp;
    }


    /**
     * Read exceptions attribute from input
     * 
     * @param cp
     *            associated constant pool
     * @param input
     *            the stream
     */
    Exceptions(ConstantPool cp, InputStream input) throws IOException {
        super(cp, Attribute.ATTR_EXCEPTIONS);
        cp_ = cp;
        int len = IO.readS4(input);
        if( (len < 2) || (len % 2 != 0) )
            throw new IllegalArgumentException(
                    "Exceptions attribute has length " + len
                            + ". Should be at least 2 and even.");
        int len2 = IO.readU2(input);
        if( (len2 * 2 + 2) != len )
            throw new IllegalArgumentException(
                    "Exception attribute of length " + len + " has " + len2
                            + " exceptions.");
        for(int i = 0;i < len2;i++) {
            int c = IO.readU2(input);
            ConstantClass cc = cp.validate(c, ConstantClass.class);
            excepts_.add(cc);
        }
    }


    /**
     * Add a named exception
     * 
     * @param e
     *            the exception
     */
    public void addException(String e) {
        ConstantClass c = new ConstantClass(cp_, e);
        if( !excepts_.contains(c) ) excepts_.add(c);
    }


    /**
     * Does the method have the declared exception?
     * 
     * @param e
     *            the exception
     * @return true if the method is declared to throw the exception
     */
    public boolean hasException(String e) {
        ConstantClass c = new ConstantClass(cp_, e);
        return excepts_.contains(c);
    }


    /**
     * Removes a named exception
     * 
     * @param e
     *            the exception
     * @return true if the exception was present and removed
     */
    public boolean removeException(String e) {
        ConstantClass c = new ConstantClass(cp_, e);
        return excepts_.remove(c);
    }


    /**
     * Write Exceptions attribute to stream
     * 
     * @param baos
     *            output stream
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrId_);
        IO.writeS4(baos, 2 + 2 * excepts_.size());
        int s = excepts_.size();
        IO.writeU2(baos, s);
        for(int i = 0;i < s;i++) {
            ConstantClass c = excepts_.get(i);
            IO.writeU2(baos, c.getIndex());
        }
    }
}