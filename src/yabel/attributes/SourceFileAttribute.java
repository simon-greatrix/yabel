package yabel.attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yabel.ClassData;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;
import yabel.io.IO;

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
     * Create a marker attribute from its specification
     * 
     * @param cp
     *            the constant pool associated with this attribute
     * @param cd
     *            the class data defining this attribute
     */
    public SourceFileAttribute(ConstantPool cp, ClassData cd) {
        super(cp, cd);
        String sourceFile = cd.getSafe(String.class, "source");
        source_ = new ConstantUtf8(cp, sourceFile);
    }


    /**
     * New source file attribute.
     * 
     * @param cp
     *            Constant pool associated with the class
     * @param input
     *            stream class is being read from
     */
    SourceFileAttribute(ConstantPool cp, InputStream input) throws IOException {
        super(cp, Attribute.ATTR_SOURCE_FILE);
        int len = IO.readS4(input);
        if( len != 2 )
            throw new IOException("SourceFile attribute has length " + len
                    + " not 2");
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


    /** {@inheritDoc} */
    @Override
    public ClassData toClassData() {
        ClassData cd = makeClassData();
        cd.put("source", source_.get());
        return cd;
    }


    /**
     * Write this attribute to the output.
     * 
     * @param baos
     *            the output
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrId_.getIndex());
        IO.writeS4(baos, 2);
        IO.writeU2(baos, source_.getIndex());
    }

}