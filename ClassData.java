package yabel;

import yabel.io.ClassDataWriter;
import yabel.io.XMLDataWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Typed data to configure a class compilation.
 * 
 * @author Simon Greatrix
 * 
 */
public class ClassData extends LinkedHashMap<String, Object> {
    /** serial version UID */
    private static final long serialVersionUID = 7439179816314472328L;


    /**
     * Holder for a list that knows the type of the list contents
     * 
     * @author Simon Greatrix
     * 
     * @param <T>
     *            the type of the list contents
     */
    private static class ListHolder<T> {
        /** Class for the list contents */
        final Class<T> clss_;

        /** The list itself */
        final List<T> list_;


        /**
         * Create new holder.
         * 
         * @param list
         *            the list to hold
         * @param clss
         *            the class of the list contents
         */
        ListHolder(List<T> list, Class<T> clss) {
            list_ = list;
            clss_ = clss;
        }


        /**
         * Get the list if it is of the correct type, or null.
         * 
         * @param <Q>
         *            the required type
         * @param clss
         *            the class for the required type
         * @return the list if it is of the correct type
         */
        <Q> List<Q> get(Class<Q> clss) {
            if( clss.isAssignableFrom(clss_) ) {
                @SuppressWarnings("unchecked")
                List<Q> list = (List<Q>) list_;
                return list;
            }
            return null;
        }
        
        
        void writeTo(String key, ClassDataWriter cdw) throws IOException {
            cdw.writeList(key, clss_, list_);
        }
    }
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(String key, Object value) {
        if( (value!=null) && (value instanceof List<?>) ) {
            throw new Error("Lists must be put into the map using put List");
        }
        return super.put(key, value);
    }


    /**
     * Get the map entry if it is of the correct type.
     * 
     * @param <T>
     *            the required type
     * @param clss
     *            the class of the required type
     * @param key
     *            the key to lookup
     * @return the data if present, otherwise null
     */
    public <T> T get(Class<T> clss, String key) {
        return get(clss, key, null);
    }


    /**
     * Get the map entry if it is of the correct type.
     * 
     * @param <T>
     *            the required type
     * @param clss
     *            the class of the required type
     * @param key
     *            the key to lookup
     * @param dflt
     *            the default to return if not found
     * @return the data if present, otherwise <code>dflt</code>
     */
    public <T> T get(Class<T> clss, String key, T dflt) {
        Object o = get(key);
        if( o == null ) return dflt;
        if( !clss.isAssignableFrom(o.getClass()) ) return dflt;
        return clss.cast(o);
    }


    /**
     * Get a List from the map if it contains the correct type
     * 
     * @param <T>
     *            the required class
     * @param clss
     *            the class of the required type
     * @param key
     *            the key to lookup
     * @return the list if it exists and matches, or null
     */
    public <T> List<T> getList(Class<T> clss, String key) {
        Object o = get(key);
        if( o == null ) return null;
        if( !(o instanceof ListHolder<?>) ) return null;
        return ((ListHolder<?>) o).get(clss);
    }


    /**
     * Put a List into this map and remember its member type.
     * 
     * @param <T>
     *            the required class
     * @param clss
     *            the class of the required type
     * @param key
     *            the key to store the list under
     * @param list
     *            the list to store
     */
    public <T> void putList(Class<T> clss, String key, List<T> list) {
        ListHolder<T> lh = new ListHolder<T>(list, clss);
        super.put(key, lh);
    }


    /**
     * Sort the members of this map into alphabetical order.
     */
    public void sort() {
        TreeMap<String, Object> temp = new TreeMap<String, Object>(this);
        clear();
        putAll(temp);
    }
    
    
    public void writeTo(ClassDataWriter cdw) throws IOException {
        cdw.start();
        writeTo(null,cdw);
        cdw.finish();
    }
    
    
    private void writeTo(String name, ClassDataWriter cdw) throws IOException {
        cdw.startClassData(name);
        for(Entry<String,Object> e : entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if( v instanceof ClassData ) {
                ((ClassData) v).writeTo(k,cdw);
            } else if( v instanceof ListHolder<?> ) {
                ((ListHolder<?>) v).writeTo(k, cdw);
            } else {
                cdw.write(k,v);
            }
        }
        cdw.endClassData();
    }
    

    /** Returns this ClassData contents in XML format */
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        XMLDataWriter xml = new XMLDataWriter(writer);
        try {
            writeTo(xml);
            writer.close();
        } catch ( IOException ioe ) {
            throw new Error("Unexpected IOE",ioe);
//            return "<unavailable>";
        }
        return writer.toString();
    }
}
