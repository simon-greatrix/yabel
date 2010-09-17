package yabel.parser.decomp;

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
         * Get this label's name
         * 
         * @return the name
         */
        public String getName() {
            return name_;
        }


        /**
         * {@inheritDoc}
         * 
         * @see yabel.parser.decomp.Source#source()
         */
        @Override
        public String source() {
            return "#:" + name_;
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
            return "#4:" + name_;
        }
    }

    /** Label's name */
    String name_;

    /** Position of this label */
    private Integer pos_;

    /** The reference to this label */
    private Ref ref_;

    /** The reference to this label */
    private Ref4 ref4_;


    /**
     * Create new label
     * 
     * @param pos
     *            label's position
     */
    public Label(Integer pos) {
        pos_ = pos;
        name_ = String.format("lbl%04x", pos_);
        ref_ = new Ref();
        ref4_ = new Ref4();
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
     * Set the name of this label
     * 
     * @param name
     *            the new name
     */
    public void setName(String name) {
        if( name != null ) {
            name_ = name;
        } else {
            name_ = String.format("lbl%04x", pos_);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    @Override
    public String source() {
        return "@:" + name_;
    }
}
