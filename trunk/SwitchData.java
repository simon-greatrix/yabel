package yabel;

import yabel.io.ClassDataWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The values and branch destinations for a SWITCH statement.
 * 
 * @author Simon Greatrix
 * 
 */
public class SwitchData implements Iterable<Entry<Integer, String>> {
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


    /**
     * Iterate over all the cases. The iteration runs in the order the cases
     * were added.
     * 
     * @return the iterator
     */
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
    
    public void writeTo(String key, ClassDataWriter cdw) throws IOException {
        
    }
}
