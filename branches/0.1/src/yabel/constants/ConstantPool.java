package yabel.constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yabel.ClassBuilder;
import yabel.io.IO;

/**
 * A Constant Pool in a class
 * 
 * @author Simon Greatrix
 * 
 */
public class ConstantPool {
    /** Indexed constants */
    private final List<Constant> index_ = new ArrayList<Constant>();

    /** The class that owns this constant pool */
    private final ClassBuilder owner_;

    /** Canonical constants */
    private final Map<Constant, Constant> pool_ = new HashMap<Constant, Constant>();

    /** Size of the constant pool */
    private int size_ = 0;


    /**
     * Create new empty constant pool.
     * 
     * @param owner
     *            the owning class builder
     */
    public ConstantPool(ClassBuilder owner) {
        owner_ = owner;
    }


    /**
     * Read a constant pool from a stream.
     * 
     * @param owner
     *            the owning class builder
     * @param input
     *            the stream
     * @throws IOException
     */
    public ConstantPool(ClassBuilder owner, InputStream input)
            throws IOException {
        owner_ = owner;

        // how many constants?
        int s = IO.readU2(input);

        while( s > 1 ) {
            int tag = IO.readU1(input);
            Constant c = null;
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
                // falls through
            case 8:
                // falls through
            case 9:
                // falls through
            case 10:
                // falls through
            case 11:
                // falls through
            case 12:
                c = new Unresolved(tag, input);
                break;
            default:
                throw new IOException("Constant tag type " + tag + " unknown");
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
            if( p == null ) pool_.put(c, c);
        }

        s = index_.size();
        for(int ph = 0;ph < 3;ph++) {
            for(int i = 0;i < s;i++) {
                Constant c = index_.get(i);
                if( c == null ) continue;
                if( !(c instanceof Unresolved) ) continue;
                Unresolved u = (Unresolved) c;
                c = u.resolve(this, ph);
                if( c != null ) {
                    c.index_ = u.index_;
                    pool_.remove(u);
                    index_.set(c.index_, c);
                    Constant p = pool_.get(c);
                    if( p == null ) pool_.put(c, c);
                }
            }
        }
    }


    /**
     * Canonicalize a constant in this pool
     * 
     * @param val
     *            The constant to canonicalize. The index field will be correct
     *            on return.
     * @return the canonical constant
     */
    Constant canonicalize(Constant val) {
        Constant canon = pool_.get(val);
        if( canon == null ) {
            pool_.put(val, val);
            canon = val;
            if( canon.index_ == -1 ) {
                // new constant, give it an id
                canon.index_ = size_ + 1;
                size_ += canon.getPoolSize();
            } else {
                // replacement constant - ensure it is not already assigned
                Constant c = index_.get(canon.index_);
                if( (c != null) && !c.equals(canon) ) {
                    throw new AssertionError("Cannot reassign constant "
                            + canon.index_ + " from " + c + " to " + canon);
                }
            }

            while( index_.size() <= canon.index_ ) {
                index_.add(null);
            }
            index_.set(canon.index_, canon);
        } else {
            // just set the id
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
            throw new YabelConstantException(
                    "Constant indexes are in the range 0 to "
                            + (index_.size() - 1) + " not " + i);
        Constant c = index_.get(i);
        if( c == null ) {
            // this should only happen if (i-1) is a long or double
            if( i > 0 ) {
                c = index_.get(i - 1);
                if( c != null )
                    throw new YabelConstantException("Constant " + i
                            + " is not defined. Previous constant is " + c);
            }
            throw new YabelConstantException("Constant " + i
                    + " is not defined");
        }
        return c;
    }


    /**
     * Get the canonical version of a constant
     * 
     * @param val
     *            The constant to get.
     * @return the canonical constant or null if there is no such constant yet
     */
    Constant getCanon(Constant val) {
        return pool_.get(val);
    }


    public ClassBuilder getOwner() {
        return owner_;
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


    /** {@inheritDoc} */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Pool [");
        for(int i = 0;i < index_.size();i++) {
            Constant c = index_.get(i);
            if( c == null ) continue;
            buf.append("    ").append(i).append(": ").append(c).append('\n');
        }
        buf.append("]");
        return buf.toString();
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
            throw new YabelConstantException("References constant " + c
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