package yabel.attributes;

import yabel.io.IO;


import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The name of the source file that a class was compiled from.
 * 
 * @author Simon Greatrix
 * 
 */
public class SourceFileAttribute extends Attribute {
    /** The Utf8 constant that holds the source file name */
    private ConstantUtf8 source_;


    /**
     * New source file attribute.
     * 
     * @param cp
     *            Constant pool associated with the class
     * @param input
     *            stream class is being read from
     */
    SourceFileAttribute(ConstantPool cp, InputStream input)
            throws IOException {
        super(cp, Attribute.ATTR_SOURCE_FILE);
        int len = IO.readS4(input);
        if( len != 2 )
            throw new IllegalArgumentException(
                    "SourceFile attribute has length " + len + " not 2");
        int val = IO.readU2(input);
        source_ = cp.validate(val, ConstantUtf8.class);
    }


    /**
     * Create a new SourceFile attribute
     * 
     * @param cp
     *            the constant pool the source file is stored in
     * @param sourceFile
     *            the source file
     */
    public SourceFileAttribute(ConstantPool cp, String sourceFile) {
        super(cp, Attribute.ATTR_SOURCE_FILE);
        source_ = new ConstantUtf8(cp, sourceFile);
    }


    /**
     * Get the source file
     * 
     * @return the source file
     */
    public String getValue() {
        return source_.get();
    }


    /**
     * Set the source file
     * 
     * @param cp
     *            the constant pool the source file is stored in
     * @param sourceFile
     *            the source file
     */
    public void setValue(ConstantPool cp, String sourceFile) {
        source_ = new ConstantUtf8(cp, sourceFile);
    }


    /**
     * Write this attribute to the output.
     * 
     * @param baos
     *            the output
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrId_);
        IO.writeS4(baos, 2);
        IO.writeU2(baos, source_.getIndex());
    }

}