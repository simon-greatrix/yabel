package yabel.attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yabel.ClassData;
import yabel.constants.ConstantPool;
import yabel.io.IO;

/**
 * A simple attribute that acts as a marker. Examples are "Synthetic" and
 * "Deprecated".
 * 
 * @author Simon Greatrix
 * 
 */
public class MarkerAttribute extends Attribute {

    /**
     * Create a marker attribute from its specification
     * 
     * @param cp
     *            the constant pool associated with this attribute
     * @param cd
     *            the class data defining this attribute
     */
    public MarkerAttribute(ConstantPool cp, ClassData cd) {
        super(cp, cd);
    }


    /**
     * Create marker attribute of the given name
     * 
     * @param cp
     *            the constant pool
     * @param name
     *            the attribute's name
     */
    public MarkerAttribute(ConstantPool cp, String name) {
        super(cp, name);
    }


    /**
     * Create a new marker attribute such as a Deprecated or a Synthetic.
     * 
     * @param cp
     *            the class's constant pool
     * @param idName
     *            the attribute's name
     * @param input
     *            the input stream
     * @throws IOException
     */
    MarkerAttribute(ConstantPool cp, String idName, InputStream input)
            throws IOException {
        super(cp, idName);
        int len = IO.readS4(input);
        if( len != 0 )
            throw new IOException("Marker attribute " + idName
                    + " length is not zero but " + len);
    }


    /** {@inheritDoc} */
    @Override
    public ClassData toClassData() {
        return makeClassData();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrId_.getIndex());
        IO.writeS4(baos, 0);
    }
}