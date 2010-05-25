package yabel.parser;

import yabel.ClassBuilder;
import yabel.OpCodes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Parse bytes into op-codes.
 * 
 * @author Simon Greatrix
 * 
 */
public class Parser {
    /** Copy of current op-code */
    private byte[] buffer_ = new byte[16];

    /** Number of bytes in buffer */
    private int bufPos_ = 0;

    /** Bytes left in current state */
    private int bytesLeft_ = 0;

    /** Number of bytes in this code block */
    private int count_ = 0;

    /** Position of the last op code */
    private int lastOpPosition_ = 0;

    /** Listener for op-codes */
    private ParserListener listener_;

    /** Current parsing state */
    private int state_ = 0;

    /** Value used in reading switch statements */
    private int switch1_ = 0;

    /** Value used in reading switch statements */
    private int switch2_ = 0;

    /** Value used in reading switch statements */
    private int switch3_ = 0;

    /** Op-codes that branch */
    static final Set<Byte> BRANCH_OPS;


    /**
     * Number of bytes after the op-code itself.
     */
    private static final byte[] NUM_BYTES = { 0/* NOP */, 0/* ACONST_NULL */,
            0/* ICONST_M1 */, 0/* ICONST_0 */, 0/* ICONST_1 */,
            0/* ICONST_2 */, 0/* ICONST_3 */, 0/* ICONST_4 */, 0/* ICONST_5 */,
            0/* LCONST_0 */, 0/* LCONST_1 */, 0/* FCONST_0 */, 0/* FCONST_1 */,
            0/* FCONST_2 */, 0/* DCONST_0 */, 0/* DCONST_1 */, 1/* BIPUSH */,
            2/* SIPUSH */, 1/* LDC */, 2/* LDC_W */, 2/* LDC2_W */,
            1/* ILOAD */, 1/* LLOAD */, 1/* FLOAD */, 1/* DLOAD */,
            1/* ALOAD */, 0/* ILOAD_0 */, 0/* ILOAD_1 */, 0/* ILOAD_2 */,
            0/* ILOAD_3 */, 0/* LLOAD_0 */, 0/* LLOAD_1 */, 0/* LLOAD_2 */,
            0/* LLOAD_3 */, 0/* FLOAD_0 */, 0/* FLOAD_1 */, 0/* FLOAD_2 */,
            0/* FLOAD_3 */, 0/* DLOAD_0 */, 0/* DLOAD_1 */, 0/* DLOAD_2 */,
            0/* DLOAD_3 */, 0/* ALOAD_0 */, 0/* ALOAD_1 */, 0/* ALOAD_2 */,
            0/* ALOAD_3 */, 0/* IALOAD */, 0/* LALOAD */, 0/* FALOAD */,
            0/* DALOAD */, 0/* AALOAD */, 0/* BALOAD */, 0/* CALOAD */,
            0/* SALOAD */, 1/* ISTORE */, 1/* LSTORE */, 1/* FSTORE */,
            1/* DSTORE */, 1/* ASTORE */, 0/* ISTORE_0 */, 0/* ISTORE_1 */,
            0/* ISTORE_2 */, 0/* ISTORE_3 */, 0/* LSTORE_0 */, 0/* LSTORE_1 */,
            0/* LSTORE_2 */, 0/* LSTORE_3 */, 0/* FSTORE_0 */, 0/* FSTORE_1 */,
            0/* FSTORE_2 */, 0/* FSTORE_3 */, 0/* DSTORE_0 */, 0/* DSTORE_1 */,
            0/* DSTORE_2 */, 0/* DSTORE_3 */, 0/* ASTORE_0 */, 0/* ASTORE_1 */,
            0/* ASTORE_2 */, 0/* ASTORE_3 */, 0/* IASTORE */, 0/* LASTORE */,
            0/* FASTORE */, 0/* DASTORE */, 0/* AASTORE */, 0/* BASTORE */,
            0/* CASTORE */, 0/* SASTORE */, 0/* POP */, 0/* POP2 */,
            0/* DUP */, 0/* DUP_X1 */, 0/* DUP_X2 */, 0/* DUP2 */,
            0/* DUP2_X1 */, 0/* DUP2_X2 */, 0/* SWAP */, 0/* IADD */,
            0/* LADD */, 0/* FADD */, 0/* DADD */, 0/* ISUB */, 0/* LSUB */,
            0/* FSUB */, 0/* DSUB */, 0/* IMUL */, 0/* LMUL */, 0/* FMUL */,
            0/* DMUL */, 0/* IDIV */, 0/* LDIV */, 0/* FDIV */, 0/* DDIV */,
            0/* IREM */, 0/* LREM */, 0/* FREM */, 0/* DREM */, 0/* INEG */,
            0/* LNEG */, 0/* FNEG */, 0/* DNEG */, 0/* ISHL */, 0/* LSHL */,
            0/* ISHR */, 0/* LSHR */, 0/* IUSHR */, 0/* LUSHR */, 0/* IAND */,
            0/* LAND */, 0/* IOR */, 0/* LOR */, 0/* IXOR */, 0/* LXOR */,
            2/* IINC */, 0/* I2L */, 0/* I2F */, 0/* I2D */, 0/* L2I */,
            0/* L2F */, 0/* L2D */, 0/* F2I */, 0/* F2L */, 0/* F2D */,
            0/* D2I */, 0/* D2L */, 0/* D2F */, 0/* I2B */, 0/* I2C */,
            0/* I2S */, 0/* LCMP */, 0/* FCMPL */, 0/* FCMPG */, 0/* DCMPL */,
            0/* DCMPG */, 2/* IFEQ */, 2/* IFNE */, 2/* IFLT */, 2/* IFGE */,
            2/* IFGT */, 2/* IFLE */, 2/* IF_ICMPEQ */, 2/* IF_ICMPNE */,
            2/* IF_ICMPLT */, 2/* IF_ICMPGE */, 2/* IF_ICMPGT */,
            2/* IF_ICMPLE */, 2/* IF_ACMPEQ */, 2/* IF_ACMPNE */, 2/* GOTO */,
            2/* JSR */, 1/* RET */, -1/* TABLESWITCH */, -1/* LOOKUPSWITCH */,
            0/* IRETURN */, 0/* LRETURN */, 0/* FRETURN */, 0/* DRETURN */,
            0/* ARETURN */, 0/* RETURN */, 2/* GETSTATIC */, 2/* PUTSTATIC */,
            2/* GETFIELD */, 2/* PUTFIELD */, 2/* INVOKEVIRTUAL */,
            2/* INVOKESPECIAL */, 2/* INVOKESTATIC */, 4/* INVOKEINTERFACE */,
            -4, 2/* NEW */, 1/* NEWARRAY */, 2/* ANEWARRAY */,
            0/* ARRAYLENGTH */, 0/* ATHROW */, 2/* CHECKCAST */,
            2/* INSTANCEOF */, 0/* MONITORENTER */, 0/* MONITOREXIT */,
            -1/* WIDE */, 3/* MULTIANEWARRAY */, 2/* IFNULL */,
            2/* IFNONNULL */, 4/* GOTO_W */, 4 /* JSR_W */};

    /** All the byte codes that can exit a decompiler block */
    static final Set<Byte> OP_EXIT;
    
    static {

        // initialise branch instructions
        Set<Byte> branches = new HashSet<Byte>();
        branches.add(Byte.valueOf(OpCodes.GOTO));
        branches.add(Byte.valueOf(OpCodes.GOTO_W));
        branches.add(Byte.valueOf(OpCodes.JSR));
        branches.add(Byte.valueOf(OpCodes.JSR_W));
        branches.add(Byte.valueOf(OpCodes.IF_ACMPEQ));
        branches.add(Byte.valueOf(OpCodes.IF_ACMPNE));
        branches.add(Byte.valueOf(OpCodes.IF_ICMPEQ));
        branches.add(Byte.valueOf(OpCodes.IF_ICMPGE));
        branches.add(Byte.valueOf(OpCodes.IF_ICMPGT));
        branches.add(Byte.valueOf(OpCodes.IF_ICMPLE));
        branches.add(Byte.valueOf(OpCodes.IF_ICMPLT));
        branches.add(Byte.valueOf(OpCodes.IF_ICMPNE));
        branches.add(Byte.valueOf(OpCodes.IFEQ));
        branches.add(Byte.valueOf(OpCodes.IFGE));
        branches.add(Byte.valueOf(OpCodes.IFGT));
        branches.add(Byte.valueOf(OpCodes.IFLE));
        branches.add(Byte.valueOf(OpCodes.IFLT));
        branches.add(Byte.valueOf(OpCodes.IFNE));
        branches.add(Byte.valueOf(OpCodes.IFNONNULL));
        branches.add(Byte.valueOf(OpCodes.IFNULL));
        branches.add(Byte.valueOf(OpCodes.LOOKUPSWITCH));
        branches.add(Byte.valueOf(OpCodes.TABLESWITCH));
        BRANCH_OPS = Collections.unmodifiableSet(branches);


        Set<Byte> opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.ATHROW));
        opVars.add(Byte.valueOf(OpCodes.RETURN));
        opVars.add(Byte.valueOf(OpCodes.ARETURN));
        opVars.add(Byte.valueOf(OpCodes.DRETURN));
        opVars.add(Byte.valueOf(OpCodes.FRETURN));
        opVars.add(Byte.valueOf(OpCodes.IRETURN));
        opVars.add(Byte.valueOf(OpCodes.LRETURN));
        opVars.add(Byte.valueOf(OpCodes.RET));
        OP_EXIT = Collections.unmodifiableSet(opVars);
    }

    /**
     * Create a new Parser for parsing byte-code into op-codes.
     * 
     * @param listener
     *            the recipient of op-code information
     */
    public Parser(ParserListener listener) {
        listener_ = listener;
    }


    int getCurrentPosition() {
        return count_;
    }


    public int getLastOpPosition() {
        return lastOpPosition_;
    }


    /**
     * Parse the next byte.
     * 
     * @param b
     *            the next byte
     */
    public void parse(int b) {
        b = b & 0xff;

        if( state_ == 0 ) {
            bufPos_ = 0;
        }
        if( bufPos_ == buffer_.length ) {
            byte[] tmp = new byte[buffer_.length * 2];
            System.arraycopy(buffer_, 0, tmp, 0, bufPos_);
            buffer_ = tmp;
        }
        buffer_[bufPos_] = (byte) b;
        bufPos_++;

        switch (state_) {
        case 0:
            // just about to read an op-code
            lastOpPosition_ = count_;
            bytesLeft_ = NUM_BYTES[b];
            if( ClassBuilder.DEBUG ) {
                String s = OpCodes.getOpName(b);
                System.out.print(String.format("%6d : ",
                        Integer.valueOf(count_)));
                System.out.print(s);
                System.out.print((bytesLeft_ == 0) ? "\n" : " ");
            }
            if( bytesLeft_ > 0 ) state_ = 1;
            if( bytesLeft_ < 0 ) {
                switch ((byte) b) {
                case OpCodes.WIDE:
                    state_ = 100;
                    break;
                case OpCodes.LOOKUPSWITCH:
                    bytesLeft_ = 4;
                    switch1_ = 0;
                    switch2_ = 0;
                    switch3_ = 0;
                    state_ = ((count_ % 4) == 3) ? 201 : 200;
                    break;
                case OpCodes.TABLESWITCH:
                    bytesLeft_ = 4;
                    switch1_ = 0;
                    switch2_ = 0;
                    switch3_ = 0;
                    state_ = ((count_ % 4) == 3) ? 301 : 300;
                    break;
                default:
                    throw new IllegalArgumentException("Op-code " + b
                            + " (" + Integer.toHexString(b)
                            + ") was not recognised");
                }
            }
            break;
        case 1:
            // reading additional bytes for op-code
            bytesLeft_--;
            if( bytesLeft_ == 0 ) state_ = 0;
            if( ClassBuilder.DEBUG ) {
                System.out.print(String.format("%02x", Integer.valueOf(b)));
                System.out.print((bytesLeft_ == 0) ? "\n" : " ");
            }
            break;
        case 100:
            // read a WIDE op-code
            bytesLeft_ = 2 * Parser.NUM_BYTES[b];
            if( bytesLeft_ <= 0 )
                throw new IllegalArgumentException("Op-code " + b + " ("
                        + Integer.toHexString(b) + " cannot follow WIDE");
            if( ClassBuilder.DEBUG ) {
                String s = OpCodes.getOpName(b);
                System.out.print(s);
                System.out.print(" ");
            }
            state_ = 1;
            break;
        case 200:
            // read a LOOKUPSWITCH instruction's padding bytes
            if( (count_ % 4) == 3 ) {
                state_ = 201;
                if( ClassBuilder.DEBUG ) System.out.print("\n        ");
            }
            break;
        case 201:
            // read a LOOKUPSWITCH default
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( ClassBuilder.DEBUG )
                System.out.print(String.format("%02x ", Integer.valueOf(b)));
            if( bytesLeft_ == 0 ) {
                state_ = 202;
                bytesLeft_ = 4;
                if( ClassBuilder.DEBUG )
                    System.out.print(" (default=" + switch1_
                            + ")\n         ");
                switch1_ = 0;
            }
            break;
        case 202:
            // read a LOOKUPSWITCH npairs
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( ClassBuilder.DEBUG )
                System.out.print(String.format("%02x ", Integer.valueOf(b)));
            if( bytesLeft_ == 0 ) {
                state_ = 203;
                bytesLeft_ = 8;
                if( ClassBuilder.DEBUG )
                    System.out.print(" (npairs=" + switch1_
                            + ")\n         ");
                switch2_ = 0;
            }
            break;
        case 203:
            // read LOOKUPSWITCH pairs
            bytesLeft_--;
            if( ClassBuilder.DEBUG ) {
                // work out offset and report
                switch2_ = (switch2_ << 8) + b;
                System.out.print(String.format("%02x ", Integer.valueOf(b)));
                if( bytesLeft_ == 4 ) {
                    System.out.print(" (match=" + switch2_ + ") ");
                    switch2_ = 0;
                } else if( bytesLeft_ == 0 ) {
                    System.out.println(" (offset=" + switch2_ + ")");
                    switch2_ = 0;
                }
            }
            if( bytesLeft_ == 0 ) {
                // decrement loop count
                switch1_--;
                if( switch1_ > 0 ) {
                    // another 8 bytes of value and offset to read
                    if( ClassBuilder.DEBUG ) System.out.print("         ");
                    bytesLeft_ = 8;
                } else {
                    // finished statement
                    state_ = 0;
                }
            }
            break;
        case 300:
            // read a TABLESWITCH instruction's padding bytes
            if( (count_ % 4) == 3 ) {
                state_ = 301;
                if( ClassBuilder.DEBUG ) System.out.print("\n         ");
            }
            break;
        case 301:
            // read a TABLESWITCH default
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( ClassBuilder.DEBUG )
                System.out.print(String.format("%02x ", Integer.valueOf(b)));
            if( bytesLeft_ == 0 ) {
                state_ = 302;
                bytesLeft_ = 4;
                if( ClassBuilder.DEBUG )
                    System.out.print(" (default=" + switch1_
                            + ")\n         ");
                switch1_ = 0;
            }
            break;
        case 302:
            // read a TABLESWITCH minima
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( ClassBuilder.DEBUG )
                System.out.print(String.format("%02x ", Integer.valueOf(b)));
            if( bytesLeft_ == 0 ) {
                state_ = 303;
                bytesLeft_ = 4;
                if( ClassBuilder.DEBUG )
                    System.out.print(" (min=" + switch1_ + ")\n         ");
            }
            break;
        case 303:
            // read a TABLESWITCH maxima
            bytesLeft_--;
            switch2_ = (switch2_ << 8) + b;
            if( ClassBuilder.DEBUG )
                System.out.print(String.format("%02x ", Integer.valueOf(b)));
            if( bytesLeft_ == 0 ) {
                state_ = 304;
                bytesLeft_ = 4;
                if( ClassBuilder.DEBUG )
                    System.out.print(" (max=" + switch2_ + ")\n         ");
            }
            break;
        case 304:
            // read a TABLESWITCH destinations
            if( ClassBuilder.DEBUG )
                System.out.print(String.format("%02x ", Integer.valueOf(b)));
            bytesLeft_--;
            switch3_ = (switch3_ << 8) + b;
            if( bytesLeft_ == 0 ) {
                if( ClassBuilder.DEBUG )
                    System.out.println(" (case:" + switch1_ + " offset="
                            + switch3_ + ")");
                switch3_ = 0;
                switch1_++;
                if( switch1_ > switch2_ ) {
                    state_ = 0;
                } else {
                    bytesLeft_ = 4;
                    if( ClassBuilder.DEBUG ) System.out.print("         ");
                }
            }
            break;
        }

        // have we finished the op-code? If so, inform listener
        if( (state_ == 0) && (listener_ != null) ) {
            listener_.opCodeFinish(lastOpPosition_, buffer_, bufPos_);
        }

        // increment byte count
        count_++;
    }


    /**
     * Read a single unsigned byte
     * 
     * @param buf
     *            the buffer holding the byte
     * @param loc
     *            where the data is
     * @return the value
     */
    public static int readU1(byte[] buf, int loc) {
        return 0xff & buf[loc];
    }


    /**
     * Read a signed int
     * 
     * @param buf
     *            the buffer holding the data
     * @param loc
     *            where the data is
     * @return the value
     */
    public static int readS4(byte[] buf, int loc) {
        return ((buf[loc] & 0xff) << 24) + ((buf[loc + 1] & 0xff) << 16)
                + ((buf[loc + 2] & 0xff) << 8) + (buf[loc + 3] & 0xff);
    }


    /**
     * Read an unsigned short
     * 
     * @param buf
     *            the buffer holding the data
     * @param loc
     *            where the data is
     * @return the value
     */
    public static int readU2(byte[] buf, int loc) {
        return ((0xff & buf[loc]) << 8) + (0xff & buf[loc + 1]);
    }


    /**
     * Read a signed short
     * 
     * @param buf
     *            the buffer holding the data
     * @param loc
     *            where the data is
     * @return the value
     */
    static int readS2(byte[] buf, int loc) {
        return (buf[loc] << 8) | (0xff & buf[loc + 1]);
    }
}