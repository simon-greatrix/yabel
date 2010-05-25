package yabel.attributes;

import yabel.io.IO;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** A generic attribute in the class */
public class GenericAttribute extends Attribute {
    /** The raw data for this attribute */
    private byte[] data_;


    /**
     * Create generic attribute
     * 
     * @param id
     *            the attribute name id
     * @param input
     *            the input stream
     * @throws IOException
     */
    GenericAttribute(int id, InputStream input) throws IOException {
        super(id);
        int len = IO.readS4(input);
        data_ = new byte[len];
        for(int i = 0;i < len;i++) {
            data_[i] = (byte) IO.readU1(input);
        }
    }


    /**
     * Get a copy of the data this attribute holds.
     * 
     * @return a copy of the data held
     */
    public byte[] getData() {
        byte[] newData = new byte[data_.length];
        System.arraycopy(data_, 0, newData, 0, data_.length);
        return newData;
    }


    /**
     * Set the data held by this attribute. Note the input is copied.
     * 
     * @param data
     *            the new data for this attribute
     */
    public void setData(byte[] data) {
        byte[] newData = new byte[data.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        data_ = newData;
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
        IO.writeS4(baos, data_.length);
        for(int i = 0;i < data_.length;i++) {
            baos.write(data_[i]);
        }
    }

}