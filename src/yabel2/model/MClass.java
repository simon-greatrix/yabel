package yabel2.model;

import yabel2.Access;
import yabel2.Version;

/**
 * A model for a class.
 * 
 * @author Simon Greatrix
 */
public class MClass {
    /** Java version */
    private Version version_ = Version.CURRENT;

    /** Class access modifier */
    private int access_;

    public MClass(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        this(Version.forId(version), access, name, signature, superName,
                interfaces);
    }

    public MClass(String name) {
        this(Version.CURRENT,)
        Access.CLASS.verify(access);
        access_ = access;
    }


    public MClass(Version version, int access, String name, String signature,
            String superName, String[] interfaces) {
        Access.CLASS.verify(access);
        access_ = access;
    }
}
