package yabel.constants;


import java.io.ByteArrayOutputStream;

/**
 * An entry in the class constant pool
 * 
 * @author Simon Greatrix
 */
abstract public class Constant implements Comparable<Constant> {
    /** The index in the constant pool */
    protected int index_ = -1;


    /**
     * Canonicalize this so that there is only one instance in the pool. All
     * subclasses should call this method.
     * 
     * @param cp
     *            the constant pool
     */
    protected void canonicalize(ConstantPool cp) {
        Constant c = cp.canonicalize(this);
        index_ = c.index_;
    }


    /**
     * Compare to other constants. Sorts into constant pool index order.
     * 
     * @param c
     *            the other constant
     * @return -1, 0 or 1 to indicate order
     */
    public int compareTo(Constant c) {
        if( index_ < c.index_ ) return -1;
        if( index_ > c.index_ ) return 1;
        return 0;
    }


    /**
     * Get the index in the constant pool
     * 
     * @return the index
     */
    public int getIndex() {
        return index_;
    }


    /**
     * Get the number of slots used by this in the constant pool
     * 
     * @return slots used
     */
    int getPoolSize() {
        return 1;
    }


    /**
     * Validate that any constants referenced from this constant are the
     * correct type.
     * 
     * @param cp
     *            constant pool that contains this
     */
    void validate(ConstantPool cp) {
    // do nothing
    }


    /**
     * Output this to the stream
     * 
     * @param baos
     *            output stream
     */
    abstract public void writeTo(ByteArrayOutputStream baos);
}