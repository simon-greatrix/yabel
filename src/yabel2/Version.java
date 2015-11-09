package yabel2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;

public enum Version {
    V1_1(Opcodes.V1_1,"1.1",1),
    V1_2(Opcodes.V1_2,"1.2",2),
    V1_3(Opcodes.V1_3,"1.3",3),
    V1_4(Opcodes.V1_4,"1.4",4),
    V1_5(Opcodes.V1_5,"1.5",5),
    V1_6(Opcodes.V1_6,"1.6",6),
    V1_7(Opcodes.V1_7,"1.7",7),
    V1_8(Opcodes.V1_8,"1.8",8);
    
    private static final Map<Integer,Version> REVISION_TO_VERSION;
    private static final Map<Integer,Version> CODE_TO_VERSION;
    private static final Map<String,Version> ID_TO_VERSION;
    
    static {
        HashMap<Integer,Version> r2v = new HashMap<>();
        HashMap<Integer,Version> c2v = new HashMap<>();
        HashMap<String,Version> n2v = new HashMap<>();
        for(Version v : Version.values()) {
            r2v.put(Integer.valueOf(v.revision()),v);
            n2v.put(v.id(), v);
            c2v.put(Integer.valueOf(v.code()),v);
        }
        REVISION_TO_VERSION = Collections.unmodifiableMap(r2v);
        ID_TO_VERSION = Collections.unmodifiableMap(n2v);
        CODE_TO_VERSION = Collections.unmodifiableMap(c2v);
    }
    
    public static Version forRevision(int v) {
        return REVISION_TO_VERSION.get(Integer.valueOf(v)); 
    }
    
    public static Version forCode(int v) {
        return CODE_TO_VERSION.get(Integer.valueOf(v));
    }
    
    public static Version forId(int v) {
        return ID_TO_VERSION.get(Integer.valueOf(v));
    }
    
    private final int code_;
    
    private final String name_;
    
    private final int revision_;
    
    private Version(int code, String name, int revision) {
        code_ = code;
        name_ = name;
        revision_ = revision;
    }
    
    public int code() {
        return code_;
    }
    
    public String id() {
        return name_;
    }
    
    public int revision() {
        return revision_;
    }
}
