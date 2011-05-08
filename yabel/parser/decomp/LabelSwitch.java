package yabel.parser.decomp;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import yabel.SwitchData;
import yabel.parser.decomp.Label.Ref4;

/**
 * The values and branch destinations for a SWITCH statement.
 * 
 * @author Simon Greatrix
 * 
 */
public class LabelSwitch implements Iterable<Entry<Integer, Ref4>>, Source {
    /**
     * Representation of a case value and label
     * 
     * @author Simon Greatrix
     */
    public static class Case {
        /** The label this case will jump to */
        private final Ref4 label_;

        /** The value this case will match */
        private final Integer val_;


        /**
         * Create new case
         * 
         * @param val
         *            value to match
         * @param label
         *            label to jump to
         */
        public Case(Integer val, Ref4 label) {
            val_ = val;
            label_ = label;
        }


        /** {@inheritDoc} */
        @Override
        public boolean equals(Object other) {
            if( other == null ) return false;
            if( other == this ) return true;
            if( !(other instanceof Case) ) return false;
            Case otherCase = (Case) other;
            return label_.equals(otherCase.label_)
                    && val_.equals(otherCase.val_);
        }


        public Ref4 getLabel() {
            return label_;
        }


        public Integer getValue() {
            return val_;
        }


        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return label_.hashCode();
        }
    }

    /** The destinations for each case */
    private final LinkedHashMap<Integer, Ref4> data_ = new LinkedHashMap<Integer, Ref4>();

    /** The label for the default destination */
    private final Ref4 dflt_;

    /** The maximum case value */
    private int max_ = Integer.MIN_VALUE;

    /** The minimum case value */
    private int min_ = Integer.MAX_VALUE;

    /** The switch type, either a TABLESWITCH or a LOOKUPSWITCH */
    private final String type_;


    /**
     * Create new switch data with the specified default destination
     * 
     * @param type
     *            the switch type
     * @param dflt
     *            the default destination
     */
    public LabelSwitch(String type, Ref4 dflt) {
        type_ = type;
        dflt_ = dflt;
    }


    /**
     * Add a case to this switch statement
     * 
     * @param cse
     *            the case to add
     */
    public void add(Case cse) {
        add(cse.getValue(), cse.getLabel());
    }


    /**
     * Add an extra case to this switch.
     * 
     * @param i
     *            the case
     * @param lbl
     *            the branch destination label
     */
    public void add(Integer i, Ref4 lbl) {
        if( data_.containsKey(i) )
            throw new IllegalStateException("Key " + i + " already mapped");
        data_.put(i, lbl);
        int ii = i.intValue();
        if( ii < min_ ) min_ = ii;
        if( ii > max_ ) max_ = ii;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if( other == null ) return false;
        if( other == this ) return true;
        if( !(other instanceof LabelSwitch) ) return false;
        LabelSwitch otherSwitch = (LabelSwitch) other;
        if( !dflt_.equals(otherSwitch.dflt_) ) return false;
        if( min_ != otherSwitch.min_ ) return false;
        if( max_ != otherSwitch.max_ ) return false;
        return data_.equals(otherSwitch.data_);
    }


    /**
     * Get the branch destination of the given case
     * 
     * @param i
     *            the case
     * @return the destination or null if it is default
     */
    public Ref4 get(Integer i) {
        return data_.get(i);
    }


    /**
     * Get the switch data that can be used to recreate this switch statement
     * 
     * @return the switch data
     */
    public SwitchData getData() {
        SwitchData sw = new SwitchData(dflt_.getName());
        for(Entry<Integer, Ref4> e:this) {
            sw.add(e.getKey(), e.getValue().getName());
        }
        return sw;
    }


    /**
     * Get the default destination
     * 
     * @return the default destination
     */
    public Ref4 getDefault() {
        return dflt_;
    }


    /**
     * Get the maximum case.
     * 
     * @return the maximum
     */
    public int getMax() {
        return max_;
    }


    /**
     * Get the minimum case.
     * 
     * @return the minimum
     */
    public int getMin() {
        return min_;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return dflt_.hashCode() ^ max_ ^ min_ ^ (data_.size() << 16);
    }


    /**
     * Iterate over all the cases. The iteration runs in the order the cases
     * were added.
     * 
     * @return the iterator
     */
    public Iterator<Entry<Integer, Ref4>> iterator() {
        Map<Integer, Ref4> map = Collections.unmodifiableMap(data_);
        return map.entrySet().iterator();
    }


    /**
     * The number of non-default branches
     * 
     * @return the number of branches
     */
    public int size() {
        return data_.size();
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    @Override
    public String source() {
        StringBuilder buf = new StringBuilder();
        String nl = "\n:";
        buf.append(type_).append(nl).append(dflt_.getName());
        for(Entry<Integer, Ref4> e:this) {
            buf.append(nl).append(e.getKey()).append(':').append(
                    e.getValue().getName());
        }
        return buf.toString();
    }
}
