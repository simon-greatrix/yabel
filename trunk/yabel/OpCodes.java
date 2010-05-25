package yabel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository of JVM Op-Codes
 * 
 * @author Simon Greatrix
 * 
 */
public class OpCodes {
    /** JVM op-code */
    public static final byte AALOAD = (byte) 50;

    /** JVM op-code */
    public static final byte AASTORE = (byte) 83;

    /** JVM op-code */
    public static final byte ACONST_NULL = (byte) 1;

    /** JVM op-code */
    public static final byte ALOAD = (byte) 25;

    /** JVM op-code */
    public static final byte ALOAD_0 = (byte) 42;

    /** JVM op-code */
    public static final byte ALOAD_1 = (byte) 43;

    /** JVM op-code */
    public static final byte ALOAD_2 = (byte) 44;

    /** JVM op-code */
    public static final byte ALOAD_3 = (byte) 45;

    /** JVM op-code */
    public static final byte ANEWARRAY = (byte) 189;

    /** JVM op-code */
    public static final byte ARETURN = (byte) 176;

    /**
     * What the array type codes correspond to.
     */
    private static String[] ARRAY_TYPES = { "", "", "", "", "boolean", "char",
            "float", "double", "byte", "short", "int", "long" };

    /** JVM op-code */
    public static final byte ARRAYLENGTH = (byte) 190;

    /** JVM op-code */
    public static final byte ASTORE = (byte) 58;

    /** JVM op-code */
    public static final byte ASTORE_0 = (byte) 75;

    /** JVM op-code */
    public static final byte ASTORE_1 = (byte) 76;

    /** JVM op-code */
    public static final byte ASTORE_2 = (byte) 77;

    /** JVM op-code */
    public static final byte ASTORE_3 = (byte) 78;

    /** JVM op-code */
    public static final byte ATHROW = (byte) 191;

    /** JVM op-code */
    public static final byte BALOAD = (byte) 51;

    /** JVM op-code */
    public static final byte BASTORE = (byte) 84;

    /** JVM op-code */
    public static final byte BIPUSH = (byte) 16;

    /** JVM op-code */
    public static final byte CALOAD = (byte) 52;

    /** JVM op-code */
    public static final byte CASTORE = (byte) 85;

    /** JVM op-code */
    public static final byte CHECKCAST = (byte) 192;

    /** JVM op-code */
    public static final byte D2F = (byte) 144;

    /** JVM op-code */
    public static final byte D2I = (byte) 142;

    /** JVM op-code */
    public static final byte D2L = (byte) 143;

    /** JVM op-code */
    public static final byte DADD = (byte) 99;

    /** JVM op-code */
    public static final byte DALOAD = (byte) 49;

    /** JVM op-code */
    public static final byte DASTORE = (byte) 82;

    /** JVM op-code */
    public static final byte DCMPG = (byte) 152;

    /** JVM op-code */
    public static final byte DCMPL = (byte) 151;

    /** JVM op-code */
    public static final byte DCONST_0 = (byte) 14;

    /** JVM op-code */
    public static final byte DCONST_1 = (byte) 15;

    /** JVM op-code */
    public static final byte DDIV = (byte) 111;

    /** JVM op-code */
    public static final byte DLOAD = (byte) 24;

    /** JVM op-code */
    public static final byte DLOAD_0 = (byte) 38;

    /** JVM op-code */
    public static final byte DLOAD_1 = (byte) 39;

    /** JVM op-code */
    public static final byte DLOAD_2 = (byte) 40;

    /** JVM op-code */
    public static final byte DLOAD_3 = (byte) 41;

    /** JVM op-code */
    public static final byte DMUL = (byte) 107;

    /** JVM op-code */
    public static final byte DNEG = (byte) 119;

    /** JVM op-code */
    public static final byte DREM = (byte) 115;

    /** JVM op-code */
    public static final byte DRETURN = (byte) 175;

    /** JVM op-code */
    public static final byte DSTORE = (byte) 57;

    /** JVM op-code */
    public static final byte DSTORE_0 = (byte) 71;

    /** JVM op-code */
    public static final byte DSTORE_1 = (byte) 72;

    /** JVM op-code */
    public static final byte DSTORE_2 = (byte) 73;

    /** JVM op-code */
    public static final byte DSTORE_3 = (byte) 74;

    /** JVM op-code */
    public static final byte DSUB = (byte) 103;

    /** JVM op-code */
    public static final byte DUP = (byte) 89;

    /** JVM op-code */
    public static final byte DUP_X1 = (byte) 90;

    /** JVM op-code */
    public static final byte DUP_X2 = (byte) 91;

    /** JVM op-code */
    public static final byte DUP2 = (byte) 92;

    /** JVM op-code */
    public static final byte DUP2_X1 = (byte) 93;

    /** JVM op-code */
    public static final byte DUP2_X2 = (byte) 94;

    /** JVM op-code */
    public static final byte F2D = (byte) 141;

    /** JVM op-code */
    public static final byte F2I = (byte) 139;

    /** JVM op-code */
    public static final byte F2L = (byte) 140;

    /** JVM op-code */
    public static final byte FADD = (byte) 98;

    /** JVM op-code */
    public static final byte FALOAD = (byte) 48;

    /** JVM op-code */
    public static final byte FASTORE = (byte) 81;

    /** JVM op-code */
    public static final byte FCMPG = (byte) 150;

    /** JVM op-code */
    public static final byte FCMPL = (byte) 149;

    /** JVM op-code */
    public static final byte FCONST_0 = (byte) 11;

    /** JVM op-code */
    public static final byte FCONST_1 = (byte) 12;

    /** JVM op-code */
    public static final byte FCONST_2 = (byte) 13;

    /** JVM op-code */
    public static final byte FDIV = (byte) 110;

    /** JVM op-code */
    public static final byte FLOAD = (byte) 23;

    /** JVM op-code */
    public static final byte FLOAD_0 = (byte) 34;

    /** JVM op-code */
    public static final byte FLOAD_1 = (byte) 35;

    /** JVM op-code */
    public static final byte FLOAD_2 = (byte) 36;

    /** JVM op-code */
    public static final byte FLOAD_3 = (byte) 37;

    /** JVM op-code */
    public static final byte FMUL = (byte) 106;

    /** JVM op-code */
    public static final byte FNEG = (byte) 118;

    /** JVM op-code */
    public static final byte FREM = (byte) 114;

    /** JVM op-code */
    public static final byte FRETURN = (byte) 174;

    /** JVM op-code */
    public static final byte FSTORE = (byte) 56;

    /** JVM op-code */
    public static final byte FSTORE_0 = (byte) 67;

    /** JVM op-code */
    public static final byte FSTORE_1 = (byte) 68;

    /** JVM op-code */
    public static final byte FSTORE_2 = (byte) 69;

    /** JVM op-code */
    public static final byte FSTORE_3 = (byte) 70;

    /** JVM op-code */
    public static final byte FSUB = (byte) 102;

    /** JVM op-code */
    public static final byte GETFIELD = (byte) 180;

    /** JVM op-code */
    public static final byte GETSTATIC = (byte) 178;

    /** JVM op-code */
    public static final byte GOTO = (byte) 167;

    /** JVM op-code */
    public static final byte GOTO_W = (byte) 200;

    /** JVM op-code */
    public static final byte I2B = (byte) 145;

    /** JVM op-code */
    public static final byte I2C = (byte) 146;

    /** JVM op-code */
    public static final byte I2D = (byte) 135;

    /** JVM op-code */
    public static final byte I2F = (byte) 134;

    /** JVM op-code */
    public static final byte I2L = (byte) 133;

    /** JVM op-code */
    public static final byte I2S = (byte) 147;

    /** JVM op-code */
    public static final byte IADD = (byte) 96;

    /** JVM op-code */
    public static final byte IALOAD = (byte) 46;

    /** JVM op-code */
    public static final byte IAND = (byte) 126;

    /** JVM op-code */
    public static final byte IASTORE = (byte) 79;

    /** JVM op-code */
    public static final byte ICONST_0 = (byte) 3;

    /** JVM op-code */
    public static final byte ICONST_1 = (byte) 4;

    /** JVM op-code */
    public static final byte ICONST_2 = (byte) 5;

    /** JVM op-code */
    public static final byte ICONST_3 = (byte) 6;

    /** JVM op-code */
    public static final byte ICONST_4 = (byte) 7;

    /** JVM op-code */
    public static final byte ICONST_5 = (byte) 8;

    /** JVM op-code */
    public static final byte ICONST_M1 = (byte) 2;

    /** JVM op-code */
    public static final byte IDIV = (byte) 108;

    /** JVM op-code */
    public static final byte IF_ACMPEQ = (byte) 165;

    /** JVM op-code */
    public static final byte IF_ACMPNE = (byte) 166;

    /** JVM op-code */
    public static final byte IF_ICMPEQ = (byte) 159;

    /** JVM op-code */
    public static final byte IF_ICMPGE = (byte) 162;

    /** JVM op-code */
    public static final byte IF_ICMPGT = (byte) 163;

    /** JVM op-code */
    public static final byte IF_ICMPLE = (byte) 164;

    /** JVM op-code */
    public static final byte IF_ICMPLT = (byte) 161;

    /** JVM op-code */
    public static final byte IF_ICMPNE = (byte) 160;

    /** JVM op-code */
    public static final byte IFEQ = (byte) 153;

    /** JVM op-code */
    public static final byte IFGE = (byte) 156;

    /** JVM op-code */
    public static final byte IFGT = (byte) 157;

    /** JVM op-code */
    public static final byte IFLE = (byte) 158;

    /** JVM op-code */
    public static final byte IFLT = (byte) 155;

    /** JVM op-code */
    public static final byte IFNE = (byte) 154;

    /** JVM op-code */
    public static final byte IFNONNULL = (byte) 199;

    /** JVM op-code */
    public static final byte IFNULL = (byte) 198;

    /** JVM op-code */
    public static final byte IINC = (byte) 132;

    /** JVM op-code */
    public static final byte ILOAD = (byte) 21;

    /** JVM op-code */
    public static final byte ILOAD_0 = (byte) 26;

    /** JVM op-code */
    public static final byte ILOAD_1 = (byte) 27;

    /** JVM op-code */
    public static final byte ILOAD_2 = (byte) 28;

    /** JVM op-code */
    public static final byte ILOAD_3 = (byte) 29;

    /** JVM op-code */
    public static final byte IMUL = (byte) 104;

    /** JVM op-code */
    public static final byte INEG = (byte) 116;

    /** JVM op-code */
    public static final byte INSTANCEOF = (byte) 193;

    /** JVM op-code */
    public static final byte INVOKEINTERFACE = (byte) 185;

    /** JVM op-code */
    public static final byte INVOKESPECIAL = (byte) 183;

    /** JVM op-code */
    public static final byte INVOKESTATIC = (byte) 184;

    /** JVM op-code */
    public static final byte INVOKEVIRTUAL = (byte) 182;

    /** JVM op-code */
    public static final byte IOR = (byte) 128;

    /** JVM op-code */
    public static final byte IREM = (byte) 112;

    /** JVM op-code */
    public static final byte IRETURN = (byte) 172;

    /** JVM op-code */
    public static final byte ISHL = (byte) 120;

    /** JVM op-code */
    public static final byte ISHR = (byte) 122;

    /** JVM op-code */
    public static final byte ISTORE = (byte) 54;

    /** JVM op-code */
    public static final byte ISTORE_0 = (byte) 59;

    /** JVM op-code */
    public static final byte ISTORE_1 = (byte) 60;

    /** JVM op-code */
    public static final byte ISTORE_2 = (byte) 61;

    /** JVM op-code */
    public static final byte ISTORE_3 = (byte) 62;

    /** JVM op-code */
    public static final byte ISUB = (byte) 100;

    /** JVM op-code */
    public static final byte IUSHR = (byte) 124;

    /** JVM op-code */
    public static final byte IXOR = (byte) 130;

    /** JVM op-code */
    public static final byte JSR = (byte) 168;

    /** JVM op-code */
    public static final byte JSR_W = (byte) 201;

    /** JVM op-code */
    public static final byte L2D = (byte) 138;

    /** JVM op-code */
    public static final byte L2F = (byte) 137;

    /** JVM op-code */
    public static final byte L2I = (byte) 136;

    /** JVM op-code */
    public static final byte LADD = (byte) 97;

    /** JVM op-code */
    public static final byte LALOAD = (byte) 47;

    /** JVM op-code */
    public static final byte LAND = (byte) 127;

    /** JVM op-code */
    public static final byte LASTORE = (byte) 80;

    /** JVM op-code */
    public static final byte LCMP = (byte) 148;

    /** JVM op-code */
    public static final byte LCONST_0 = (byte) 9;

    /** JVM op-code */
    public static final byte LCONST_1 = (byte) 10;

    /** JVM op-code */
    public static final byte LDC = (byte) 18;

    /** JVM op-code */
    public static final byte LDC_W = (byte) 19;

    /** JVM op-code */
    public static final byte LDC2_W = (byte) 20;

    /** JVM op-code */
    public static final byte LDIV = (byte) 109;

    /** JVM op-code */
    public static final byte LLOAD = (byte) 22;

    /** JVM op-code */
    public static final byte LLOAD_0 = (byte) 30;

    /** JVM op-code */
    public static final byte LLOAD_1 = (byte) 31;

    /** JVM op-code */
    public static final byte LLOAD_2 = (byte) 32;

    /** JVM op-code */
    public static final byte LLOAD_3 = (byte) 33;

    /** JVM op-code */
    public static final byte LMUL = (byte) 105;

    /** JVM op-code */
    public static final byte LNEG = (byte) 117;

    /** JVM op-code */
    public static final byte LOOKUPSWITCH = (byte) 171;

    /** JVM op-code */
    public static final byte LOR = (byte) 129;

    /** JVM op-code */
    public static final byte LREM = (byte) 113;

    /** JVM op-code */
    public static final byte LRETURN = (byte) 173;

    /** JVM op-code */
    public static final byte LSHL = (byte) 121;

    /** JVM op-code */
    public static final byte LSHR = (byte) 123;

    /** JVM op-code */
    public static final byte LSTORE = (byte) 55;

    /** JVM op-code */
    public static final byte LSTORE_0 = (byte) 63;

    /** JVM op-code */
    public static final byte LSTORE_1 = (byte) 64;

    /** JVM op-code */
    public static final byte LSTORE_2 = (byte) 65;

    /** JVM op-code */
    public static final byte LSTORE_3 = (byte) 66;

    /** JVM op-code */
    public static final byte LSUB = (byte) 101;

    /** JVM op-code */
    public static final byte LUSHR = (byte) 125;

    /** JVM op-code */
    public static final byte LXOR = (byte) 131;

    /** JVM op-code */
    public static final byte MONITORENTER = (byte) 194;

    /** JVM op-code */
    public static final byte MONITOREXIT = (byte) 195;

    /** JVM op-code */
    public static final byte MULTIANEWARRAY = (byte) 197;

    /** JVM op-code */
    public static final byte NEW = (byte) 187;

    /** JVM op-code */
    public static final byte NEWARRAY = (byte) 188;

    /** JVM op-code */
    public static final byte NOP = (byte) 0;

    /** Map of op-code name to op-code byte */
    public static final Map<String, Byte> OP_CODES;


    /**
     * Get the Op-Code name for a given byte. Will return "&lt;unknown&gt;" if
     * the code is not recognised.
     * 
     * @param b
     *            the byte
     * @return the name
     */
    public static String getOpName(byte b) {
        return getOpName(b & 0xff);
    }


    /**
     * Get the Op-Code name for a given byte. Will return "&lt;unknown&gt;" if
     * the code is not recognised.
     * 
     * @param i
     *            the byte
     * @return the name
     */
    public static String getOpName(int i) {
        String r = null;
        if( 0 <= i && i < OP_NAMES.length ) r = OP_NAMES[i];
        return (r == null) ? "<unknown>" : r;

    }

    /**
     * Names of all op codes
     */
    private static final String[] OP_NAMES = new String[] { "NOP",
            "ACONST_NULL", "ICONST_M1", "ICONST_0", "ICONST_1", "ICONST_2",
            "ICONST_3", "ICONST_4", "ICONST_5", "LCONST_0", "LCONST_1",
            "FCONST_0", "FCONST_1", "FCONST_2", "DCONST_0", "DCONST_1",
            "BIPUSH", "SIPUSH", "LDC", "LDC_W", "LDC2_W", "ILOAD", "LLOAD",
            "FLOAD", "DLOAD", "ALOAD", "ILOAD_0", "ILOAD_1", "ILOAD_2",
            "ILOAD_3", "LLOAD_0", "LLOAD_1", "LLOAD_2", "LLOAD_3", "FLOAD_0",
            "FLOAD_1", "FLOAD_2", "FLOAD_3", "DLOAD_0", "DLOAD_1", "DLOAD_2",
            "DLOAD_3", "ALOAD_0", "ALOAD_1", "ALOAD_2", "ALOAD_3", "IALOAD",
            "LALOAD", "FALOAD", "DALOAD", "AALOAD", "BALOAD", "CALOAD",
            "SALOAD", "ISTORE", "LSTORE", "FSTORE", "DSTORE", "ASTORE",
            "ISTORE_0", "ISTORE_1", "ISTORE_2", "ISTORE_3", "LSTORE_0",
            "LSTORE_1", "LSTORE_2", "LSTORE_3", "FSTORE_0", "FSTORE_1",
            "FSTORE_2", "FSTORE_3", "DSTORE_0", "DSTORE_1", "DSTORE_2",
            "DSTORE_3", "ASTORE_0", "ASTORE_1", "ASTORE_2", "ASTORE_3",
            "IASTORE", "LASTORE", "FASTORE", "DASTORE", "AASTORE", "BASTORE",
            "CASTORE", "SASTORE", "POP", "POP2", "DUP", "DUP_X1", "DUP_X2",
            "DUP2", "DUP2_X1", "DUP2_X2", "SWAP", "IADD", "LADD", "FADD",
            "DADD", "ISUB", "LSUB", "FSUB", "DSUB", "IMUL", "LMUL", "FMUL",
            "DMUL", "IDIV", "LDIV", "FDIV", "DDIV", "IREM", "LREM", "FREM",
            "DREM", "INEG", "LNEG", "FNEG", "DNEG", "ISHL", "LSHL", "ISHR",
            "LSHR", "IUSHR", "LUSHR", "IAND", "LAND", "IOR", "LOR", "IXOR",
            "LXOR", "IINC", "I2L", "I2F", "I2D", "L2I", "L2F", "L2D", "F2I",
            "F2L", "F2D", "D2I", "D2L", "D2F", "I2B", "I2C", "I2S", "LCMP",
            "FCMPL", "FCMPG", "DCMPL", "DCMPG", "IFEQ", "IFNE", "IFLT", "IFGE",
            "IFGT", "IFLE", "IF_ICMPEQ", "IF_ICMPNE", "IF_ICMPLT", "IF_ICMPGE",
            "IF_ICMPGT", "IF_ICMPLE", "IF_ACMPEQ", "IF_ACMPNE", "GOTO", "JSR",
            "RET", "TABLESWITCH", "LOOKUPSWITCH", "IRETURN", "LRETURN",
            "FRETURN", "DRETURN", "ARETURN", "RETURN", "GETSTATIC",
            "PUTSTATIC", "GETFIELD", "PUTFIELD", "INVOKEVIRTUAL",
            "INVOKESPECIAL", "INVOKESTATIC", "INVOKEINTERFACE", "", "NEW",
            "NEWARRAY", "ANEWARRAY", "ARRAYLENGTH", "ATHROW", "CHECKCAST",
            "INSTANCEOF", "MONITORENTER", "MONITOREXIT", "WIDE",
            "MULTIANEWARRAY", "IFNULL", "IFNONNULL", "GOTO_W", "JSR_W" };

    /** JVM op-code */
    public static final byte POP = (byte) 87;

    /** JVM op-code */
    public static final byte POP2 = (byte) 88;

    /** JVM op-code */
    public static final byte PUTFIELD = (byte) 181;

    /** JVM op-code */
    public static final byte PUTSTATIC = (byte) 179;

    /** JVM op-code */
    public static final byte RET = (byte) 169;

    /** JVM op-code */
    public static final byte RETURN = (byte) 177;

    /** JVM op-code */
    public static final byte SALOAD = (byte) 53;

    /** JVM op-code */
    public static final byte SASTORE = (byte) 86;

    /** JVM op-code */
    public static final byte SIPUSH = (byte) 17;

    /** JVM op-code */
    public static final byte SWAP = (byte) 95;

    /** JVM op-code */
    public static final byte TABLESWITCH = (byte) 170;

    /** JVM op-code */
    public static final byte WIDE = (byte) 196;

    static {
        Map<String, Byte> m = new HashMap<String, Byte>();
        for(int i = 0;i < OP_NAMES.length;i++) {
            m.put(OP_NAMES[i], Byte.valueOf((byte) i));
        }
        OP_CODES = Collections.unmodifiableMap(m);
    }


    /**
     * Get the name of the type associated with the type code.
     * 
     * @param type
     *            the type code
     * @return the name of the type
     */
    public static String getArrayType(int type) {
        return ARRAY_TYPES[type];
    }


    /**
     * Get the code of the type associated with the type name
     * 
     * @param type
     *            the type name
     * @return the code of the type
     */
    public static int getArrayType(String type) {
        for(int i = 0;i < ARRAY_TYPES.length;i++) {
            if( type.equalsIgnoreCase(ARRAY_TYPES[i]) ) return i;
        }
        return -1;
    }
}
