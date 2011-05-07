package yabel.parser.decomp;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A label in a code block
 * 
 * @author Simon Greatrix
 */
public class Label implements Source {
    /**
     * Reference to a label in a code block
     * 
     * @author Simon Greatrix
     */
    public class Ref implements Source {
        /**
         * Get the name of the label referenced by this
         * 
         * @return the label's name
         */
        public String getName() {
            return Label.this.getName();
        }


        /**
         * {@inheritDoc}
         * 
         * @see yabel.parser.decomp.Source#source()
         */
        @Override
        public String source() {
            return "#:" + getName();
        }
    }



    /**
     * Reference to a label in a code block
     * 
     * @author Simon Greatrix
     */
    public class Ref4 extends Ref {
        /**
         * {@inheritDoc}
         * 
         * @see yabel.parser.decomp.Source#source()
         */
        @Override
        public String source() {
            return "#4:" + getName();
        }
    }

    /** Label's name */
    private String defaultName_;

    /** Alternative names for this label */
    private SortedSet<String> names_ = new TreeSet<String>();

    /** Position of this label */
    private final Integer pos_;

    /** The reference to this label */
    private final Ref ref_;

    /** The reference to this label */
    private final Ref4 ref4_;


    /**
     * Create new label
     * 
     * @param pos
     *            label's position
     */
    public Label(Integer pos) {
        pos_ = pos;
        defaultName_ = null;
        ref_ = new Ref();
        ref4_ = new Ref4();
    }


    /**
     * Set the name of this label
     * 
     * @param name
     *            the new name
     */
    public void addName(String name) {
        if( name==null ) return;
        names_.add(name);
    }


    /**
     * Remove a name from this label
     * 
     * @param name
     *            the name to remove
     */
    public void removeName(String name) {
        if( name==null ) return;
        names_.remove(name);
        if( name.equals(defaultName_) ) defaultName_ = null;
    }


    /**
     * Get this label's name
     * 
     * @return the name
     */
    public String getName() {
        if( defaultName_ != null ) return defaultName_;
        if( !names_.isEmpty() ) return names_.first();
        return String.format("lbl%04x", pos_);
    }


    /**
     * Get all of this label's name
     * 
     * @return the names
     */
    public SortedSet<String> getAllName() {
        SortedSet<String> names = names_;
        if( names_.isEmpty() ) {
            String nm = String.format("lbl%04x", pos_);
            names = new TreeSet<String>();
            names.add(nm);
        }
        return Collections.unmodifiableSortedSet(names);
    }


    /**
     * Get a reference to this label
     * 
     * @return the reference
     */
    public Ref getRef() {
        return ref_;
    }


    /**
     * Get a reference to this label
     * 
     * @return the reference
     */
    public Ref4 getRef4() {
        return ref4_;
    }


    /**
     * Set the default name of this label
     * 
     * @param name
     *            the default name
     */
    public void setDefaultName(String name) {
        names_.add(name);
        defaultName_ = name;
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    @Override
    public String source() {
        if( names_.isEmpty() ) return "@:" + getName();

        StringBuilder buf = new StringBuilder();
        for(String n:names_) {
            buf.append(" @:").append(n);
        }
        return buf.toString().substring(1);
    }
}
