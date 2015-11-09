package yabel2;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * Access modifiers for fields, methods and classes
 * 
 * @author Simon Greatrix
 */
public class Access {

    /** Canonical order for modifiers. @see java.lang.reflect.Modifier */
    private static final int[] ACC_ORDER;

    public static final Access CLASS;

    public static final Access FIELD;

    public static final Access METHOD;

    public static final Access PARAM;

    static {
        ACC_ORDER = new int[32];
        int[] canon = new int[] { ACC_PUBLIC, ACC_PROTECTED, ACC_PRIVATE,
                ACC_ABSTRACT, ACC_STATIC, ACC_FINAL, ACC_TRANSIENT,
                ACC_VOLATILE, ACC_SYNCHRONIZED, ACC_NATIVE, ACC_STRICT,
                ACC_INTERFACE };
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

        CLASS = new Access(new int[] { ACC_PUBLIC, ACC_PROTECTED, ACC_PRIVATE,
                ACC_FINAL, ACC_SUPER, ACC_INTERFACE, ACC_ABSTRACT, ACC_STRICT,
                ACC_SYNTHETIC, ACC_ANNOTATION, ACC_ENUM, ACC_DEPRECATED },
                new String[] { "public", "protected", "private", "final",
                        "super", "interface", "abstract", "strictfp",
                        "synthetic", "annotation", "enum", "deprecated" });
        FIELD = new Access(new int[] { ACC_PUBLIC, ACC_PROTECTED, ACC_PRIVATE,
                ACC_STATIC, ACC_FINAL, ACC_VOLATILE, ACC_TRANSIENT,
                ACC_SYNTHETIC, ACC_ENUM }, new String[] { "public",
                "protected", "private", "static", "final", "volatile",
                "transient", "synthetic", "enum" });
        METHOD = new Access(new int[] { ACC_PUBLIC, ACC_PROTECTED, ACC_PRIVATE,
                ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, ACC_BRIDGE,
                ACC_VARARGS, ACC_NATIVE, ACC_ABSTRACT, ACC_STRICT,
                ACC_SYNTHETIC, ACC_DEPRECATED }, new String[] { "public",
                "protected", "private", "static", "final", "synchronized",
                "bridge", "varargs", "native", "abstract", "strictfp",
                "synthetic", "deprecated" });
        PARAM = new Access(
                new int[] { ACC_FINAL, ACC_SYNTHETIC, ACC_MANDATED },
                new String[] { "final", "synthetic", "mandated" });

    }


    private Access(int[] flags, String[] names) {
        for(int i = 0;i < names_.length;i++) {
            names_[i] = "(unknown " + i + ")";
        }
        for(int i = 0;i < flags.length;i++) {
            int bit = bit(flags[i]);
            names_[bit] = names_[i];
            masks_.put(names_[i], Integer.valueOf(flags[i]));
        }
    }


    /**
     * Get the textual representation of an access code for a given bit-mask
     * 
     * @param m
     *            the bit mask
     * @return the access representation
     */
    public String accessCode(int m) {
        if( m == 0 ) return "";
        StringBuilder buf = new StringBuilder();
        for(int v:ACC_ORDER) {
            if( (m & v) != 0 ) {
                buf.append(' ').append(names_[bit(v)]);
            }
        }
        return buf.substring(1);
    }


    /**
     * Get the bit-mask for a given access modifier description
     * 
     * @param s
     *            the description
     * @return the bit mask
     */
    public int accessCode(String s) {
        s = s.trim();
        if( s.equals("") ) return 0;
        int i = 0;
        String[] sp = s.split("\\s+");
        for(String l:sp) {
            Integer m = masks_.get(l);
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
        for(int i = 0;i < 16;i++) {
            if( v == 1 ) return i;
            v >>>= 1;
        }
        throw new IllegalArgumentException(val
                + " could not be matched to a bit");
    }

    private final Map<String, Integer> masks_ = new HashMap<>();

    private final String[] names_ = new String[32];

}
