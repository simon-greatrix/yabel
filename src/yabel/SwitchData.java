package yabel;

import java.util.*;
import java.util.Map.Entry;

/**
 * The values and branch destinations for a SWITCH statement.
 * 
 * @author Simon Greatrix
 * 
 */
public class SwitchData implements Iterable<Entry<Integer, String>>, Copyable<SwitchData> {
    /**
     * Representation of a case value and label
     * 
     * @author Simon Greatrix
     */
    public static class Case {
        /** The label this case will jump to */
        private final String label_;

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
        public Case(Integer val, String label) {
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


        public String getLabel() {
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
    private final LinkedHashMap<Integer, String> data_ = new LinkedHashMap<Integer, String>();

    /** The name of the default destination */
    private final String dflt_;

    /** The maximum case value */
    private int max_ = Integer.MIN_VALUE;

    /** The minimum case value */
    private int min_ = Integer.MAX_VALUE;


    /**
     * Create new switch data with the specified default destination
     * 
     * @param dflt
     *            the default destination
     */
    public SwitchData(String dflt) {
        dflt_ = dflt;
    }


    /**
     * Create a copy of this instance
     * 
     * @return a copy
     */
    @Override
    public SwitchData copy() {
        SwitchData copy = new SwitchData(dflt_);
        copy.data_.putAll(data_);
        copy.max_ = max_;
        copy.min_ = min_;
        return copy;
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
    public void add(Integer i, String lbl) {
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
        if( !(other instanceof SwitchData) ) return false;
        SwitchData otherSwitch = (SwitchData) other;
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
    public String get(Integer i) {
        return data_.get(i);
    }


    /**
     * Get the default destination
     * 
     * @return the default destination
     */
    public String getDefault() {
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
    @Override
    public Iterator<Entry<Integer, String>> iterator() {
        Map<Integer, String> map = Collections.unmodifiableMap(data_);
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
}
