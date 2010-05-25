package yabel.attributes;

import yabel.io.IO;


import yabel.constants.ConstantPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple attribute that acts as a marker. Examples are "Synthetic" and
 * "Deprecated".
 * 
 * @author Simon Greatrix
 * 
 */
public class MarkerAttribute extends Attribute {

    /**
     * Create marker attribtue of the given name
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
     * @param id
     *            the attribute's name constant Id
     * @param idName
     *            the attribute's name
     * @param input
     *            the input stream
     * @throws IOException
     */
    MarkerAttribute(int id, String idName, InputStream input)
            throws IOException {
        super(id);
        int len = IO.readS4(input);
        if( len != 0 )
            throw new IllegalArgumentException("Marker attribute " + idName
                    + " length is not zero but " + len);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrId_);
        IO.writeS4(baos, 0);
    }
}