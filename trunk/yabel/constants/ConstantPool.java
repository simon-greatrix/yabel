package yabel.constants;


import yabel.io.IO;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A Constant Pool in a class
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantPool {
    /** Indexed constants */
    private final List<Constant> index_ = new ArrayList<Constant>();

    /** Canonical constants */
    private final Map<Constant, Constant> pool_ = new HashMap<Constant, Constant>();

    /** Size of the constant pool */
    private int size_ = 0;


    /** Create new empty constant pool */
    public ConstantPool() {
    // do nothing
    }


    /**
     * Read a constant pool from a stream.
     * 
     * @param input
     *            the stream
     * @throws IOException
     */
    public ConstantPool(InputStream input) throws IOException {
        // how many constants?
        int s = IO.readU2(input);
        while( s > 1 ) {
            int tag = IO.readU1(input);
            Constant c;
            switch (tag) {
            case 1:
                c = new ConstantUtf8(input);
                break;
            case 3:
                c = new ConstantNumber(3, input);
                break;
            case 4:
                c = new ConstantNumber(4, input);
                break;
            case 5:
                c = new ConstantNumber(5, input);
                break;
            case 6:
                c = new ConstantNumber(6, input);
                break;
            case 7:
                c = new ConstantClass(input);
                break;
            case 8:
                c = new ConstantString(input);
                break;
            case 9:
                c = new ConstantFieldRef(input);
                break;
            case 10:
                c = new ConstantMethodRef(input);
                break;
            case 11:
                c = new ConstantInterfaceMethodRef(input);
                break;
            case 12:
                c = new ConstantNameAndType(input);
                break;
            default:
                throw new IOException("Constant tag type " + tag
                        + " unknown");
            }

            s -= c.getPoolSize();

            c.index_ = size_ + 1;
            size_ += c.getPoolSize();

            // add constant to index
            while( index_.size() <= c.index_ ) {
                index_.add(null);
            }
            index_.set(c.index_, c);

            // constant may not be canonical
            Constant p = pool_.get(c);
            if( p == null ) {
                pool_.put(c, c);
            }
        }

        // validate all the constants
        for(Constant c:index_) {
            if( c != null ) c.validate(this);
        }
    }


    /**
     * Canonicalize a constant in this pool
     * 
     * @param val
     *            The constant to canonicalize. The index field will be
     *            correct on return.
     * @return the canonical constant
     */
    Constant canonicalize(Constant val) {
        Constant canon = pool_.get(val);
        if( canon == null ) {
            pool_.put(val, val);
            canon = val;
            canon.index_ = size_ + 1;
            size_ += canon.getPoolSize();

            while( index_.size() <= canon.index_ ) {
                index_.add(null);
            }
            index_.set(canon.index_, canon);
        } else {
            val.index_ = canon.index_;
        }
        return canon;
    }


    /**
     * Retrieve an indexed constant from the pool
     * 
     * @param i
     *            the index of the constant
     * @return the constant
     */
    public Constant get(int i) {
        if( (i < 0) || (index_.size() <= i) )
            throw new IllegalArgumentException(
                    "Constant indexes are in the range 0 to "
                            + (index_.size() - 1) + " not " + i);
        Constant c = index_.get(i);
        if( c == null ) {
            // this should only happen if (i-1) is a long or double
            if( i > 0 ) {
                c = index_.get(i - 1);
                if( c != null )
                    throw new IllegalArgumentException("Constant " + i
                            + " is not defined. Previous constant is " + c);
            }
            throw new IllegalArgumentException("Constant " + i
                    + " is not defined");
        }
        return c;
    }


    /**
     * Get the canonical version of a constant
     * 
     * @param val
     *            The constant to get.
     * @return the canonical constant or null if there is no such constant
     *         yet
     */
    Constant getCanon(Constant val) {
        return pool_.get(val);
    }


    /**
     * Get the index of a Utf8 constant for a string
     * 
     * @param str
     *            String to get Utf8 constant index for
     * @return index
     */
    public int getUtf8(String str) {
        ConstantUtf8 utf8 = new ConstantUtf8(this, str);
        return utf8.getIndex();
    }


    /**
     * Get the index of a Utf8 constant for a string
     * 
     * @param str
     *            String to get Utf8 constant index for
     * @param create
     *            if true, create the constant
     * @return index, which will be -1 if the Utf8 is not in the pool
     */
    public int getUtf8(String str, boolean create) {
        ConstantUtf8 utf8 = new ConstantUtf8(this, str, create);
        return utf8.getIndex();
    }


    /**
     * Validate a constant type and return a cast constant
     * 
     * @param <T>
     *            the required constant type
     * @param index
     *            the index of the constant in the pool
     * @param required
     *            the required class
     * @return the constant
     */
    public <T> T validate(int index, Class<T> required) {
        Constant c = get(index);
        if( !required.isAssignableFrom(c.getClass()) ) {
            throw new IllegalArgumentException("References constant " + c
                    + " but required " + required.getName());
        }
        return required.cast(c);
    }


    /**
     * Write this constant pool to the stream
     * 
     * @param baos
     *            output stream
     */
    public void writeTo(ByteArrayOutputStream baos) {
        Constant[] pool = new Constant[pool_.size()];
        pool = pool_.values().toArray(pool);
        Arrays.sort(pool);

        int s = 0;
        for(Constant c:pool) {
            s += c.getPoolSize();
        }
        IO.writeU2(baos, s + 1);
        for(int i = 0;i < pool.length;i++) {
            pool[i].writeTo(baos);
        }
    }
}