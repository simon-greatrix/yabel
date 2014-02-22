package yabel.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import yabel.OpCodes;

/**
 * Parse bytes into op-codes.
 * 
 * @author Simon Greatrix
 * 
 */
public class Parser {
    /**
     * The Parse states
     * 
     * @author Simon Greatrix
     * 
     */
    private enum State {
        /** Reading the extra bytes for an op-code */
        EXTRA_BYTES,

        /** Reading a LOOKUPSWITCH statement */
        LOOKUP,

        /** Start of a new op-code */
        START,

        /** Reading a TABLESWITCH statement */
        TABLE,

        /** Reading the op-code for a WIDE instruction */
        WIDE;
    }



    /**
     * Switch statement parsing
     * 
     * @author Simon Greatrix
     */
    private enum SwitchState {
        /** Reading cases */
        CASES,

        /** Reading default offset */
        DEFAULT,

        /** Reading maxima for table switch */
        MAX,

        /** Reading minima for table switch */
        MIN,

        /** Reading number of offsets in lookup switch */
        NPAIRS,

        /** Reading initial padding bytes */
        PAD
    }

    /** Op-codes that branch */
    static final Set<Byte> BRANCH_OPS;

    /** File path to write debug messages to */
    private static String DEBUG_FILE = null;

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

        String propName = Parser.class.getName() + ".debugFile";
        String debug = System.getProperty(propName);
        if( (debug == null) || (debug.equals("")) ) {
            debug = System.getenv(propName.toUpperCase());
        }
        if( (debug != null) && !debug.equals("") ) {
            File f = new File(debug).getAbsoluteFile();
            File p = f.getParentFile();
            if( (p != null) && (!p.exists()) && (!p.mkdirs()) ) {
                System.err.println("Cannot create debug folder "
                        + p.getAbsolutePath());
            } else {
                DEBUG_FILE = debug;
            }
        }
    }

    /** Copy of current op-code */
    private byte[] buffer_ = new byte[16];

    /** Number of bytes in buffer */
    private int bufPos_ = 0;

    /** Bytes left in current state */
    private int bytesLeft_ = 0;

    /** Number of bytes in this code block */
    private int count_ = 0;

    /** Writer for debug messages */
    private PrintWriter debug_ = null;

    /** Are we generating debug output? */
    private boolean isDebug_ = false;

    /** Position of the last op code */
    private int lastOpPosition_ = 0;

    /** Listener for op-codes */
    private ParserListener listener_;

    /** Current parsing state */
    private State state_ = State.START;

    /** Value used in reading switch statements */
    private int switch1_ = 0;

    /** Value used in reading switch statements */
    private int switch2_ = 0;

    /** Value used in reading switch statements */
    private int switch3_ = 0;

    /** Current parsing state of switch parsing */
    private SwitchState switchState_ = null;


    /**
     * Create a new Parser for parsing byte-code into op-codes.
     * 
     * @param listener
     *            the recipient of op-code information
     */
    public Parser(ParserListener listener) {
        listener_ = listener;

        if( DEBUG_FILE != null ) {
            for(int i = 0;i < 0x10000;i++) {
                File f = new File(String.format("%s.%04x.log", DEBUG_FILE,
                        Integer.valueOf(i)));
                try {
                    if( f.createNewFile() ) {
                        debug_ = new PrintWriter(new FileWriter(f), true);
                        isDebug_ = true;
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Unable to create debug output to file "
                            + f);
                    e.printStackTrace(System.err);
                }
            }
            new Throwable().printStackTrace(debug_);
        }
    }


    /** Flush and close the debug writer on finalize */
    @Override
    protected void finalize() throws Throwable {
        if( isDebug_ ) {
            debug_.flush();
            debug_.close();
        }
        super.finalize();
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

        if( state_ == State.START ) {
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
        case START:
            // just about to read an op-code
            lastOpPosition_ = count_;
            bytesLeft_ = NUM_BYTES[b];
            if( isDebug_ ) {
                String s = OpCodes.getOpName(b);
                debug_.printf("%6d : %s", Integer.valueOf(count_), s);
                if( bytesLeft_ == 0 ) {
                    debug_.println();
                } else {
                    debug_.print(" ");
                }
            }
            if( bytesLeft_ > 0 ) state_ = State.EXTRA_BYTES;
            if( bytesLeft_ < 0 ) {
                switch ((byte) b) {
                case OpCodes.WIDE:
                    state_ = State.WIDE;
                    break;
                case OpCodes.LOOKUPSWITCH:
                    bytesLeft_ = 4;
                    switch1_ = 0;
                    switch2_ = 0;
                    switch3_ = 0;
                    state_ = State.LOOKUP;
                    switchState_ = ((count_ % 4) == 3) ? SwitchState.DEFAULT
                            : SwitchState.PAD;
                    break;
                case OpCodes.TABLESWITCH:
                    bytesLeft_ = 4;
                    switch1_ = 0;
                    switch2_ = 0;
                    switch3_ = 0;
                    state_ = State.TABLE;
                    switchState_ = ((count_ % 4) == 3) ? SwitchState.DEFAULT
                            : SwitchState.PAD;
                    break;
                default:
                    throw new YabelDecompileException("Op-code " + b + " ("
                            + Integer.toHexString(b) + ") was not recognised");
                }
            }
            break;
        case EXTRA_BYTES:
            // reading additional bytes for op-code
            bytesLeft_--;
            if( bytesLeft_ == 0 ) state_ = State.START;
            if( isDebug_ ) {
                debug_.printf("%02x", Integer.valueOf(b));
                if( bytesLeft_ == 0 ) {
                    debug_.println();
                } else {
                    debug_.print(" ");
                }
            }
            break;
        case WIDE:
            // read a WIDE op-code
            bytesLeft_ = 2 * Parser.NUM_BYTES[b];
            if( bytesLeft_ <= 0 )
                throw new YabelDecompileException("Op-code " + b + " ("
                        + Integer.toHexString(b) + " cannot follow WIDE");
            if( isDebug_ ) {
                String s = OpCodes.getOpName(b);
                debug_.print(s);
                debug_.print(" ");
                debug_.flush();
            }
            state_ = State.EXTRA_BYTES;
            break;
        case LOOKUP:
            parseLookup(b);
            break;
        case TABLE:
            parseTable(b);
            break;

        }

        // have we finished the op-code? If so, inform listener
        if( (state_ == State.START) && (listener_ != null) ) {
            listener_.opCodeFinish(lastOpPosition_, buffer_, bufPos_);
        }

        // increment byte count
        count_++;
    }


    private void parseLookup(int b) {
        switch (switchState_) {
        case PAD:
            // read a LOOKUPSWITCH instruction's padding bytes
            if( (count_ % 4) == 3 ) {
                switchState_ = SwitchState.DEFAULT;
                if( isDebug_ ) debug_.print("\n        ");
            }
            break;
        case DEFAULT:
            // read a LOOKUPSWITCH default
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( isDebug_ ) debug_.printf("%02x ", Integer.valueOf(b));
            if( bytesLeft_ == 0 ) {
                switchState_ = SwitchState.NPAIRS;
                bytesLeft_ = 4;
                if( isDebug_ )
                    debug_.printf(" (default=%d)\n         ",
                            Integer.valueOf(switch1_));
                switch1_ = 0;
            }
            break;
        case NPAIRS:
            // read a LOOKUPSWITCH npairs
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( isDebug_ ) debug_.printf("%02x ", Integer.valueOf(b));
            if( bytesLeft_ == 0 ) {
                switchState_ = SwitchState.CASES;
                bytesLeft_ = 8;
                if( isDebug_ )
                    debug_.printf(" (npairs=%d)\n         ",
                            Integer.valueOf(switch1_));
                switch2_ = 0;
            }
            break;
        case CASES:
            // read LOOKUPSWITCH pairs
            bytesLeft_--;
            if( isDebug_ ) {
                // work out offset and report
                switch2_ = (switch2_ << 8) + b;
                debug_.printf("%02x ", Integer.valueOf(b));
                if( bytesLeft_ == 4 ) {
                    debug_.printf(" (match=%d) ", Integer.valueOf(switch2_));
                    switch2_ = 0;
                } else if( bytesLeft_ == 0 ) {
                    debug_.printf(" (offset=%d)\n", Integer.valueOf(switch2_));
                    switch2_ = 0;
                }
            }
            if( bytesLeft_ == 0 ) {
                // decrement loop count
                switch1_--;
                if( switch1_ > 0 ) {
                    // another 8 bytes of value and offset to read
                    if( isDebug_ ) debug_.print("         ");
                    bytesLeft_ = 8;
                } else {
                    // finished statement
                    state_ = State.START;
                }
            }
            break;
        case MAX:
        case MIN:
            throw new AssertionError("Encountered state " + switchState_
                    + " when parsing LOOKUPSWITCH");
        }
    }


    private void parseTable(int b) {
        switch (switchState_) {
        case PAD:
            // read a TABLESWITCH instruction's padding bytes
            if( (count_ % 4) == 3 ) {
                switchState_ = SwitchState.DEFAULT;
                if( isDebug_ ) debug_.print("\n         ");
            }
            break;
        case DEFAULT:
            // read a TABLESWITCH default
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( isDebug_ ) debug_.printf("%02x ", Integer.valueOf(b));
            if( bytesLeft_ == 0 ) {
                switchState_ = SwitchState.MIN;
                bytesLeft_ = 4;
                if( isDebug_ )
                    debug_.printf(" (default=%d)\n         ",
                            Integer.valueOf(switch1_));
                switch1_ = 0;
            }
            break;
        case MIN:
            // read a TABLESWITCH minima
            bytesLeft_--;
            switch1_ = (switch1_ << 8) + b;
            if( isDebug_ ) debug_.printf("%02x ", Integer.valueOf(b));
            if( bytesLeft_ == 0 ) {
                switchState_ = SwitchState.MAX;
                bytesLeft_ = 4;
                if( isDebug_ )
                    debug_.printf(" (min=%d)\n         ",
                            Integer.valueOf(switch1_));
            }
            break;
        case MAX:
            // read a TABLESWITCH maxima
            bytesLeft_--;
            switch2_ = (switch2_ << 8) + b;
            if( isDebug_ ) debug_.printf("%02x ", Integer.valueOf(b));
            if( bytesLeft_ == 0 ) {
                switchState_ = SwitchState.CASES;
                bytesLeft_ = 4;
                if( isDebug_ )
                    debug_.printf(" (max=%d)\n         ",
                            Integer.valueOf(switch1_));
            }
            break;
        case CASES:
            // read a TABLESWITCH destinations
            if( isDebug_ ) debug_.printf("%02x ", Integer.valueOf(b));
            bytesLeft_--;
            switch3_ = (switch3_ << 8) + b;
            if( bytesLeft_ == 0 ) {
                if( isDebug_ )
                    debug_.printf(" (case:%d offset:%d)\n",
                            Integer.valueOf(switch1_),
                            Integer.valueOf(switch3_));
                switch3_ = 0;
                switch1_++;
                if( switch1_ > switch2_ ) {
                    // that was the final entry
                    state_ = State.START;
                } else {
                    // more to do
                    bytesLeft_ = 4;
                    if( isDebug_ ) debug_.print("         ");
                }
            }
            break;
        case NPAIRS:
            throw new AssertionError("Encountered state " + switchState_
                    + " when parsing TABLESWITCH");
        }
    }
}
