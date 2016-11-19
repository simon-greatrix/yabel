package yabel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;

import yabel.io.ClassDataWriter;
import yabel.io.XMLDataWriter;

/**
 * Typed data to configure a class compilation.
 * 
 * @author Simon Greatrix
 * 
 */
public class ClassData extends LinkedHashMap<String, Object> implements
        Copyable<ClassData>, Iterable<Entry<String, Object>> {
    protected static void checkValue(Object o) {
        if( o == null ) return;
        checkClass(o.getClass());
    }


    protected static void checkClass(Class<?> cl) {
        if( cl == String.class ) return;
        if( cl == Integer.class ) return;
        if( cl == Float.class ) return;
        if( cl == Long.class ) return;
        if( cl == Double.class ) return;
        if( cl == ClassData.class ) return;
        if( cl == SwitchData.class ) return;
        if( cl == TypedList.class ) return;
        throw new AssertionError("Class " + cl.getName()
                + " is not allowed in ClassData");
    }

    /**
     * Representation of a key and value pair
     * 
     * @author Simon Greatrix
     * 
     */
    public static class NamedValue {
        /** Key for this named value */
        final String key_;

        /** Value for this named value */
        final Object value_;


        /**
         * Representation of a key and a value
         * 
         * @param key
         *            the key name
         * @param value
         *            the value
         */
        public NamedValue(String key, Object value) {
            key_ = key;
            value_ = value;
        }


        public String getKey() {
            return key_;
        }


        public Object getValue() {
            return value_;
        }
    }

    /**
     * Holder for a list that knows the type of the list contents
     * 
     * @author Simon Greatrix
     * 
     * @param <T>
     *            the type of the list contents
     */
    public static class TypedList<T> implements Copyable<TypedList<T>> {
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
        public TypedList(List<T> list, Class<T> clss) {
            checkClass(clss);
            list_ = list;
            clss_ = clss;
        }


        public TypedList<T> copy() {
            ArrayList<T> list;
            if( Copyable.class.isAssignableFrom(clss_) ) {
                list = new ArrayList<T>(list_.size());
                for(T t:list_) {
                    @SuppressWarnings("unchecked")
                    T t2 = (T) ((Copyable<T>) t).copy();
                    list_.add(t2);
                }
            } else {
                list = new ArrayList<T>(list_);
            }
            return new TypedList<T>(list, clss_);
        }


        /** {@inheritDoc} */
        @Override
        public boolean equals(Object other) {
            if( other == null ) return false;
            if( other == this ) return true;
            if( !(other instanceof TypedList<?>) ) return false;
            TypedList<?> otherList = (TypedList<?>) other;

            return list_.equals(otherList.list_);
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
        public <Q> List<Q> get(Class<Q> clss) {
            if( clss.isAssignableFrom(clss_) ) {
                @SuppressWarnings("unchecked")
                List<Q> list = (List<Q>) list_;
                return list;
            }
            return null;
        }


        public Class<T> getType() {
            return clss_;
        }


        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return list_.hashCode();
        }
    }

    /** serial version UID */
    private static final long serialVersionUID = 7439179816314472328L;


    /**
     * Create new empty ClassData
     */
    public ClassData() {
        // do nothing
    }


    /**
     * Create a copy of the supplied ClassData
     * 
     * @param orig
     *            the original ClassData
     */
    public ClassData(ClassData orig) {
        super(orig);
    }


    @Override
    public ClassData copy() {
        ClassData copy = new ClassData();
        for(Entry<String, Object> entry:this) {
            Object v = entry.getValue();
            if( v instanceof Copyable<?> ) {
                v = ((Copyable<?>) v).copy();
            }
            copy.putInternal(entry.getKey(), v);
        }
        return copy;
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
        if( !(o instanceof TypedList<?>) ) return null;
        return ((TypedList<?>) o).get(clss);
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
    public <T> List<T> getListSafe(Class<T> clss, String key) {
        List<T> l = getList(clss, key);
        if( l != null ) return l;
        throw new IllegalStateException("Missing required list for '" + key
                + "' of class " + clss.getName());
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
    public <T> T getSafe(Class<T> clss, String key) {
        T t = get(clss, key);
        if( t != null ) return t;
        throw new IllegalStateException("Missing required value for '" + key
                + "' of class " + clss.getName());
    }


    /**
     * Put a named value into this.
     * 
     * @param nv
     *            the named value to put
     * @return the value previously stored against the key
     */
    public Object put(NamedValue nv) {
        return putInternal(nv.getKey(), nv.getValue());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(String key, Object value) {
        if( (value != null) && (value instanceof List<?>) ) {
            throw new AssertionError(
                    "Lists must be put into the map using putList(Class,String,List)");
        }
        return putInternal(key, value);
    }


    private Object putInternal(String key, Object value) {
        if( (key == null) || key.equals("") )
            throw new AssertionError("Key must be specified and not empty");
        checkValue(value);
        return super.put(key, value);
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
     * @return the value previously stored against the key
     */
    public <T> Object putList(Class<T> clss, String key, List<T> list) {
        TypedList<T> lh = new TypedList<T>(list, clss);
        return putInternal(key, lh);
    }


    /**
     * Sort the members of this map into alphabetical order.
     */
    public void sort() {
        TreeMap<String, Object> temp = new TreeMap<String, Object>(this);
        clear();
        putAll(temp);
    }


    /**
     * Returns this ClassData contents in XML format.
     * 
     * @return this as a String
     */
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        XMLDataWriter xml = new XMLDataWriter(writer);
        try {
            writeTo(xml);
            writer.close();
        } catch (IOException ioe) {
            throw new Error("Unexpected IOE", ioe);
            // return "<unavailable>";
        }
        return writer.toString();
    }


    /**
     * Write this to the given writer
     * 
     * @param cdw
     *            the writer to write to
     * @throws IOException
     */
    public void writeTo(ClassDataWriter cdw) throws IOException {
        cdw.start();
        cdw.write(this);
        cdw.finish();
    }


    @Override
    public Iterator<java.util.Map.Entry<String, Object>> iterator() {
        return entrySet().iterator();
    }
}
