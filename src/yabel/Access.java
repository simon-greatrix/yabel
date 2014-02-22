package yabel;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Access modifiers for fields, methods and classes
 * 
 * @author Simon Greatrix
 */
public class Access {

    /** JVM access modifier */
    public static final int ACC_ABSTRACT = 0x0400;

    /** JVM access modifier */
    public static final int ACC_FINAL = 0x0010;

    /** JVM access modifier */
    public static final int ACC_INTERFACE = 0x0200;

    /** Map of access names to their masks */
    static final Map<String, Integer> ACC_MASKS;

    /** Map of access masks to their names */
    static final String[] ACC_NAMES = new String[16];

    /** JVM access modifier */
    public static final int ACC_NATIVE = 0x0100;

    /** Canonical order for modifiers. @see java.lang.reflect.Modifier */
    public static final int[] ACC_ORDER;

    /** JVM access modifier */
    public static final int ACC_PRIVATE = 0x0002;

    /** JVM access modifier */
    public static final int ACC_PROTECTED = 0x0004;

    /** JVM access modifier */
    public static final int ACC_PUBLIC = 0x0001;

    /** JVM access modifier */
    public static final int ACC_STATIC = 0x0008;

    /** JVM access modifier */
    public static final int ACC_STRICT = 0x0800;

    /** JVM access modifier */
    public static final int ACC_SUPER = 0x0020;

    /** JVM access modifier */
    public static final int ACC_SYNCH = 0x0020;

    /** JVM access modifier */
    public static final int ACC_TRANSIENT = 0x0080;

    /** JVM access modifier */
    public static final int ACC_VOLATILE = 0x0040;

    static {
        for(int j = 0;j < ACC_NAMES.length;j++) {
            ACC_NAMES[j] = "(unknown:" + j + ")";
        }
        ACC_NAMES[bit(ACC_ABSTRACT)] = "abstract";
        ACC_NAMES[bit(ACC_FINAL)] = "final";
        ACC_NAMES[bit(ACC_INTERFACE)] = "interface";
        ACC_NAMES[bit(ACC_NATIVE)] = "native";
        ACC_NAMES[bit(ACC_PRIVATE)] = "private";
        ACC_NAMES[bit(ACC_PROTECTED)] = "protected";
        ACC_NAMES[bit(ACC_PUBLIC)] = "public";
        ACC_NAMES[bit(ACC_STATIC)] = "static";
        ACC_NAMES[bit(ACC_STRICT)] = "strictfp";
        ACC_NAMES[bit(ACC_SUPER)] = "super";
        ACC_NAMES[bit(ACC_SYNCH)] = "synchronized";
        ACC_NAMES[bit(ACC_TRANSIENT)] = "transient";
        ACC_NAMES[bit(ACC_VOLATILE)] = "volatile";

        Map<String, Integer> s2i = new HashMap<String, Integer>();
        int k = 1;
        for(String element:ACC_NAMES) {
            s2i.put(element, Integer.valueOf(k));
            k *= 2;
        }
        ACC_MASKS = Collections.unmodifiableMap(s2i);

        ACC_ORDER = new int[ACC_NAMES.length];
        int[] canon = new int[] { ACC_PUBLIC, ACC_PROTECTED, ACC_PRIVATE,
                ACC_ABSTRACT, ACC_STATIC, ACC_FINAL, ACC_TRANSIENT,
                ACC_VOLATILE, ACC_SYNCH, ACC_NATIVE, ACC_STRICT, ACC_INTERFACE };
        System.arraycopy(canon, 0, ACC_ORDER, 0, canon.length);
        int v = 1;
        int p = canon.length;
        for(int i = 0;i < ACC_ORDER.length;i++) {
            boolean matched = false;
            for(int element:ACC_ORDER) {
                if( element == v ) {
                    matched = true;
                    break;
                }
            }
            if( !matched ) {
                ACC_ORDER[p] = v;
                p++;
            }
            v *= 2;
        }
    }


    /**
     * Get the textual representation of an access code for a given bit-mask
     * 
     * @param m
     *            the bit mask
     * @return the access representation
     */
    public static String accessCode(int m) {
        if( m == 0 ) return "";
        StringBuilder buf = new StringBuilder();
        for(int v:ACC_ORDER) {
            if( (m & v) != 0 ) {
                buf.append(' ').append(ACC_NAMES[bit(v)]);
            }
        }
        return buf.substring(1);
    }


    /**
     * Get the bit-pattern of an access code for a given
     * <code>java.lang.reflect.Member</code>.
     * 
     * @param m
     *            the <code>java.lang.reflect.Member</code>
     * @return the access representation
     */
    public static int accessCode(Member m) {
        // this is probably a no-op, but the Java documentation does not promise
        // that.
        int mods = m.getModifiers();
        return Access.accessCode(Modifier.toString(mods));
    }


    /**
     * Get the bit-mask for a given access modifier description
     * 
     * @param s
     *            the description
     * @return the bit mask
     */
    public static int accessCode(String s) {
        s = s.trim();
        if( s.equals("") ) return 0;
        int i = 0;
        String[] sp = s.split("\\s+");
        for(String l:sp) {
            Integer m = ACC_MASKS.get(l);
            if( m == null )
                throw new IllegalArgumentException(
                        "Unknown access modifier: \"" + l + "\" in \"" + s
                                + "\"");
            i += m.intValue();
        }
        return i;
    }


    /**
     * Which bit does a value correspond to?
     * 
     * @param val
     *            the value
     * @return the bit
     */
    static int bit(int val) {
        int v = val;
        int b = 1;
        for(int i = 0;i < 16;i++) {
            if( b == v ) return i;
            b <<= 1;
        }
        throw new IllegalArgumentException(val
                + " could not be matched to a bit");
    }

}
