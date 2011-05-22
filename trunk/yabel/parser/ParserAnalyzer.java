package yabel.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import yabel.Access;
import yabel.Method;
import yabel.OpCodes;
import yabel.code.Code;
import yabel.code.Handler;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantRef;
import yabel.io.IO;

/**
 * Analyze a class and work out how many local variables and stack slots it
 * uses.
 * 
 * @author Simon Greatrix
 * 
 */
public class ParserAnalyzer implements ParserListener {

    /**
     * Representation of a block of op codes that will normally run as a unit
     * and hence can be considered a single item for calculating stack
     * requirements.
     * 
     * @author Simon Greatrix
     */
    static class Block {
        /** Where this block branches to */
        Integer[] branchTo_;

        /** The cumulative delta of the block */
        int delta_ = 0;

        /** Did this block end with a RET? In which return to JSR point */
        boolean exitViaRET_ = false;

        /** The stack high-water-mark in this bock */
        int hwm_ = 0;

        /** If the block was terminated by a JSR, where to return to */
        Integer retFromJSR_ = null;


        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("Block[ delta=").append(delta_).append(" hwm=").append(
                    hwm_).append(", branch-to=(");
            if( branchTo_ != null ) {
                for(int i = 0;i < branchTo_.length;i++) {
                    if( i > 0 ) buf.append(",");
                    buf.append(branchTo_[i]);
                }
            }
            buf.append(')');
            if( exitViaRET_ ) buf.append(",RET");
            buf.append("]");
            return buf.toString();
        }
    }



    /**
     * Representation of an op-code and how it affects the stack. Used in
     * calculating required stack depth for a method.
     * 
     * @author Simon Greatrix
     * 
     */
    static class OpCode {
        /** Standard array used when an op-code is not a branch */
        private static final int[] NO_BRANCHES = new int[0];

        /** Where this op-code branches to (relative) */
        int[] branchTo_ = NO_BRANCHES;

        /** Change in stack from this op-code */
        int delta_;

        /** The byte-length of this op-code */
        int length_;

        /** The op-code */
        byte opCode_;


        /**
         * This OpCode as a String
         * 
         * @return this OpCode
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("OpCode[").append(OpCodes.getOpName(opCode_)).append(
                    ", delta=").append(delta_).append(", length=").append(
                    length_);
            if( branchTo_.length > 0 ) {
                buf.append(", branchTo=(");
                for(int i = 0;i < branchTo_.length;i++) {
                    if( i > 0 ) buf.append(',');
                    buf.append(branchTo_[i]);
                }
                buf.append(')');
            }
            buf.append(']');
            return buf.toString();
        }
    }

    /** File path to write debug messages to */
    private static String DEBUG_FILE = null;

    /** Op-codes that set local variable 0 */
    static final Set<Byte> VAR_OPS_0;

    /** Op-codes that set local variable 1 */
    static final Set<Byte> VAR_OPS_1;

    /** Op-codes that set local variable 2 */
    static final Set<Byte> VAR_OPS_2;

    /** Op-codes that set local variable 3 */
    static final Set<Byte> VAR_OPS_3;

    /** Op-codes that set local variable 4 */
    static final Set<Byte> VAR_OPS_4;

    /** Op-codes that set local variable X */
    static final Set<Byte> VAR_OPS_X;

    /** Op-codes that set local variable X+1 */
    static final Set<Byte> VAR_OPS_X1;

    static {
        // initialise ops that update local variable X
        Set<Byte> opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.ALOAD));
        opVars.add(Byte.valueOf(OpCodes.ASTORE));
        opVars.add(Byte.valueOf(OpCodes.FLOAD));
        opVars.add(Byte.valueOf(OpCodes.FSTORE));
        opVars.add(Byte.valueOf(OpCodes.ILOAD));
        opVars.add(Byte.valueOf(OpCodes.ISTORE));
        opVars.add(Byte.valueOf(OpCodes.RET));
        opVars.add(Byte.valueOf(OpCodes.IINC));
        VAR_OPS_X = Collections.unmodifiableSet(opVars);

        // initialise ops that update local variable X+1
        opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.DLOAD));
        opVars.add(Byte.valueOf(OpCodes.DSTORE));
        opVars.add(Byte.valueOf(OpCodes.LLOAD));
        opVars.add(Byte.valueOf(OpCodes.LSTORE));
        VAR_OPS_X1 = Collections.unmodifiableSet(opVars);

        // initialise ops that update local variable 0
        opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.ALOAD_0));
        opVars.add(Byte.valueOf(OpCodes.ASTORE_0));
        opVars.add(Byte.valueOf(OpCodes.FLOAD_0));
        opVars.add(Byte.valueOf(OpCodes.FSTORE_0));
        opVars.add(Byte.valueOf(OpCodes.ILOAD_0));
        opVars.add(Byte.valueOf(OpCodes.ISTORE_0));
        VAR_OPS_0 = Collections.unmodifiableSet(opVars);

        // initialise ops that update local variable 1
        opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.ALOAD_1));
        opVars.add(Byte.valueOf(OpCodes.ASTORE_1));
        opVars.add(Byte.valueOf(OpCodes.DLOAD_0));
        opVars.add(Byte.valueOf(OpCodes.DSTORE_0));
        opVars.add(Byte.valueOf(OpCodes.FLOAD_1));
        opVars.add(Byte.valueOf(OpCodes.FSTORE_1));
        opVars.add(Byte.valueOf(OpCodes.ILOAD_1));
        opVars.add(Byte.valueOf(OpCodes.ISTORE_1));
        opVars.add(Byte.valueOf(OpCodes.LLOAD_0));
        opVars.add(Byte.valueOf(OpCodes.LSTORE_0));
        VAR_OPS_1 = Collections.unmodifiableSet(opVars);

        // initialise ops that update local variable 2
        opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.ALOAD_2));
        opVars.add(Byte.valueOf(OpCodes.ASTORE_2));
        opVars.add(Byte.valueOf(OpCodes.DLOAD_1));
        opVars.add(Byte.valueOf(OpCodes.DSTORE_1));
        opVars.add(Byte.valueOf(OpCodes.FLOAD_2));
        opVars.add(Byte.valueOf(OpCodes.FSTORE_2));
        opVars.add(Byte.valueOf(OpCodes.ILOAD_2));
        opVars.add(Byte.valueOf(OpCodes.ISTORE_2));
        opVars.add(Byte.valueOf(OpCodes.LLOAD_1));
        opVars.add(Byte.valueOf(OpCodes.LSTORE_1));
        VAR_OPS_2 = Collections.unmodifiableSet(opVars);

        // initialise ops that update local variable 3
        opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.ALOAD_3));
        opVars.add(Byte.valueOf(OpCodes.ASTORE_3));
        opVars.add(Byte.valueOf(OpCodes.DLOAD_2));
        opVars.add(Byte.valueOf(OpCodes.DSTORE_2));
        opVars.add(Byte.valueOf(OpCodes.FLOAD_3));
        opVars.add(Byte.valueOf(OpCodes.FSTORE_3));
        opVars.add(Byte.valueOf(OpCodes.ILOAD_3));
        opVars.add(Byte.valueOf(OpCodes.ISTORE_3));
        opVars.add(Byte.valueOf(OpCodes.LLOAD_2));
        opVars.add(Byte.valueOf(OpCodes.LSTORE_2));
        VAR_OPS_3 = Collections.unmodifiableSet(opVars);

        // initialise ops that update local variable 4
        opVars = new HashSet<Byte>();
        opVars.add(Byte.valueOf(OpCodes.DLOAD_3));
        opVars.add(Byte.valueOf(OpCodes.DSTORE_3));
        opVars.add(Byte.valueOf(OpCodes.LLOAD_3));
        opVars.add(Byte.valueOf(OpCodes.LSTORE_3));
        VAR_OPS_4 = Collections.unmodifiableSet(opVars);

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

    /**
     * Number of stack elements added by each op-code. Negative values indicate:
     * <ul>
     * <li>-1 : 1 normally, but 2 if long or double field
     * <li>-2 : 1 normally, but 0 if return type was void
     * </ul>
     * 
     */
    static final int[] STACK_MADE = { 0/* NOP */, 1/* ACONST_NULL */,
            1/* ICONST_M1 */, 1/* ICONST_0 */, 1/* ICONST_1 */,
            1/* ICONST_2 */, 1/* ICONST_3 */, 1/* ICONST_4 */, 1/* ICONST_5 */,
            2/* LCONST_0 */, 2/* LCONST_1 */, 1/* FCONST_0 */, 1/* FCONST_1 */,
            1/* FCONST_2 */, 2/* DCONST_0 */, 2/* DCONST_1 */, 1/* BIPUSH */,
            1/* SIPUSH */, 1/* LDC */, 1/* LDC_W */, 2/* LDC2_W */,
            1/* ILOAD */, 2/* LLOAD */, 1/* FLOAD */, 2/* DLOAD */,
            1/* ALOAD */, 1/* ILOAD_0 */, 1/* ILOAD_1 */, 1/* ILOAD_2 */,
            1/* ILOAD_3 */, 2/* LLOAD_0 */, 2/* LLOAD_1 */, 2/* LLOAD_2 */,
            2/* LLOAD_3 */, 1/* FLOAD_0 */, 1/* FLOAD_1 */, 1/* FLOAD_2 */,
            1/* FLOAD_3 */, 2/* DLOAD_0 */, 2/* DLOAD_1 */, 2/* DLOAD_2 */,
            2/* DLOAD_3 */, 1/* ALOAD_0 */, 1/* ALOAD_1 */, 1/* ALOAD_2 */,
            1/* ALOAD_3 */, 1/* IALOAD */, 2/* LALOAD */, 1/* FALOAD */,
            2/* DALOAD */, 1/* AALOAD */, 1/* BALOAD */, 1/* CALOAD */,
            1/* SALOAD */, 0/* ISTORE */, 0/* LSTORE */, 0/* FSTORE */,
            0/* DSTORE */, 0/* ASTORE */, 0/* ISTORE_0 */, 0/* ISTORE_1 */,
            0/* ISTORE_2 */, 0/* ISTORE_3 */, 0/* LSTORE_0 */, 0/* LSTORE_1 */,
            0/* LSTORE_2 */, 0/* LSTORE_3 */, 0/* FSTORE_0 */, 0/* FSTORE_1 */,
            0/* FSTORE_2 */, 0/* FSTORE_3 */, 0/* DSTORE_0 */, 0/* DSTORE_1 */,
            0/* DSTORE_2 */, 0/* DSTORE_3 */, 0/* ASTORE_0 */, 0/* ASTORE_1 */,
            0/* ASTORE_2 */, 0/* ASTORE_3 */, 0/* IASTORE */, 0/* LASTORE */,
            0/* FASTORE */, 0/* DASTORE */, 0/* AASTORE */, 0/* BASTORE */,
            0/* CASTORE */, 0/* SASTORE */, 0/* POP */, 0/* POP2 */,
            2/* DUP */, 3/* DUP_X1 */, 4/* DUP_X2 */, 4/* DUP2 */,
            5/* DUP2_X1 */, 6/* DUP2_X2 */, 2/* SWAP */, 1/* IADD */,
            2/* LADD */, 1/* FADD */, 2/* DADD */, 1/* ISUB */, 2/* LSUB */,
            1/* FSUB */, 2/* DSUB */, 1/* IMUL */, 2/* LMUL */, 1/* FMUL */,
            2/* DMUL */, 1/* IDIV */, 2/* LDIV */, 1/* FDIV */, 2/* DDIV */,
            1/* IREM */, 2/* LREM */, 1/* FREM */, 2/* DREM */, 1/* INEG */,
            2/* LNEG */, 1/* FNEG */, 2/* DNEG */, 1/* ISHL */, 2/* LSHL */,
            1/* ISHR */, 2/* LSHR */, 1/* IUSHR */, 2/* LUSHR */, 1/* IAND */,
            2/* LAND */, 1/* IOR */, 2/* LOR */, 1/* IXOR */, 2/* LXOR */,
            0/* IINC */, 2/* I2L */, 1/* I2F */, 2/* I2D */, 1/* L2I */,
            1/* L2F */, 2/* L2D */, 1/* F2I */, 2/* F2L */, 2/* F2D */,
            1/* D2I */, 2/* D2L */, 1/* D2F */, 1/* I2B */, 1/* I2C */,
            1/* I2S */, 1/* LCMP */, 1/* FCMPL */, 1/* FCMPG */, 1/* DCMPL */,
            1/* DCMPG */, 0/* IFEQ */, 0/* IFNE */, 0/* IFLT */, 0/* IFGE */,
            0/* IFGT */, 0/* IFLE */, 0/* IF_ICMPEQ */, 0/* IF_ICMPNE */,
            0/* IF_ICMPLT */, 0/* IF_ICMPGE */, 0/* IF_ICMPGT */,
            0/* IF_ICMPLE */, 0/* IF_ACMPEQ */, 0/* IF_ACMPNE */, 0/* GOTO */,
            1/* JSR */, 0/* RET */, 0/* TABLESWITCH */, 0/* LOOKUPSWITCH */,
            0/* IRETURN */, 0/* LRETURN */, 0/* FRETURN */, 0/* DRETURN */,
            0/* ARETURN */, 0/* RETURN */, -1/* GETSTATIC */, 0/* PUTSTATIC */,
            -1/* GETFIELD */, 0/* PUTFIELD */, -2/* INVOKEVIRTUAL */,
            -2/* INVOKESPECIAL */, -2/* INVOKESTATIC */,
            -2/* INVOKEINTERFACE */, 0, 1/* NEW */, 1/* NEWARRAY */,
            1/* ANEWARRAY */, 1/* ARRAYLENGTH */, 1/* ATHROW */,
            1/* CHECKCAST */, 1/* INSTANCEOF */, 0/* MONITORENTER */,
            0/* MONITOREXIT */, 0/* WIDE */, 1/* MULTIANEWARRAY */,
            0/* IFNULL */, 0/* IFNONNULL */, 0/* GOTO_W */, 1 /* JSR_W */};

    /**
     * Number of elements on stack used by each op-code. Negatives values
     * indicate:
     * <ul>
     * <li>-1 : 1 normally, but 2 if long or double field and 1 more if not
     * static
     * <li>-2 : Depends on number of arguments to method
     * <li>-3 : Depends on number of dimensions of array
     * </ul>
     */
    static final byte[] STACK_USED = { 0/* NOP */, 0/* ACONST_NULL */,
            0/* ICONST_M1 */, 0/* ICONST_0 */, 0/* ICONST_1 */,
            0/* ICONST_2 */, 0/* ICONST_3 */, 0/* ICONST_4 */, 0/* ICONST_5 */,
            0/* LCONST_0 */, 0/* LCONST_1 */, 0/* FCONST_0 */, 0/* FCONST_1 */,
            0/* FCONST_2 */, 0/* DCONST_0 */, 0/* DCONST_1 */, 0/* BIPUSH */,
            0/* SIPUSH */, 0/* LDC */, 0/* LDC_W */, 0/* LDC2_W */,
            0/* ILOAD */, 0/* LLOAD */, 0/* FLOAD */, 0/* DLOAD */,
            0/* ALOAD */, 0/* ILOAD_0 */, 0/* ILOAD_1 */, 0/* ILOAD_2 */,
            0/* ILOAD_3 */, 0/* LLOAD_0 */, 0/* LLOAD_1 */, 0/* LLOAD_2 */,
            0/* LLOAD_3 */, 0/* FLOAD_0 */, 0/* FLOAD_1 */, 0/* FLOAD_2 */,
            0/* FLOAD_3 */, 0/* DLOAD_0 */, 0/* DLOAD_1 */, 0/* DLOAD_2 */,
            0/* DLOAD_3 */, 0/* ALOAD_0 */, 0/* ALOAD_1 */, 0/* ALOAD_2 */,
            0/* ALOAD_3 */, 2/* IALOAD */, 2/* LALOAD */, 2/* FALOAD */,
            2/* DALOAD */, 2/* AALOAD */, 2/* BALOAD */, 2/* CALOAD */,
            2/* SALOAD */, 1/* ISTORE */, 2/* LSTORE */, 1/* FSTORE */,
            2/* DSTORE */, 1/* ASTORE */, 1/* ISTORE_0 */, 1/* ISTORE_1 */,
            1/* ISTORE_2 */, 1/* ISTORE_3 */, 2/* LSTORE_0 */, 2/* LSTORE_1 */,
            2/* LSTORE_2 */, 2/* LSTORE_3 */, 1/* FSTORE_0 */, 1/* FSTORE_1 */,
            1/* FSTORE_2 */, 1/* FSTORE_3 */, 2/* DSTORE_0 */, 2/* DSTORE_1 */,
            2/* DSTORE_2 */, 2/* DSTORE_3 */, 1/* ASTORE_0 */, 1/* ASTORE_1 */,
            1/* ASTORE_2 */, 1/* ASTORE_3 */, 3/* IASTORE */, 4/* LASTORE */,
            3/* FASTORE */, 4/* DASTORE */, 3/* AASTORE */, 3/* BASTORE */,
            3/* CASTORE */, 3/* SASTORE */, 1/* POP */, 2/* POP2 */,
            1/* DUP */, 2/* DUP_X1 */, 3/* DUP_X2 */, 2/* DUP2 */,
            3/* DUP2_X1 */, 4/* DUP2_X2 */, 2/* SWAP */, 2/* IADD */,
            4/* LADD */, 2/* FADD */, 4/* DADD */, 2/* ISUB */, 4/* LSUB */,
            2/* FSUB */, 4/* DSUB */, 2/* IMUL */, 4/* LMUL */, 2/* FMUL */,
            4/* DMUL */, 2/* IDIV */, 4/* LDIV */, 2/* FDIV */, 4/* DDIV */,
            2/* IREM */, 4/* LREM */, 2/* FREM */, 4/* DREM */, 1/* INEG */,
            2/* LNEG */, 1/* FNEG */, 2/* DNEG */, 2/* ISHL */, 3/* LSHL */,
            2/* ISHR */, 3/* LSHR */, 2/* IUSHR */, 3/* LUSHR */, 2/* IAND */,
            4/* LAND */, 2/* IOR */, 4/* LOR */, 2/* IXOR */, 4/* LXOR */,
            0/* IINC */, 1/* I2L */, 1/* I2F */, 1/* I2D */, 2/* L2I */,
            2/* L2F */, 2/* L2D */, 1/* F2I */, 1/* F2L */, 1/* F2D */,
            2/* D2I */, 2/* D2L */, 2/* D2F */, 1/* I2B */, 1/* I2C */,
            1/* I2S */, 4/* LCMP */, 2/* FCMPL */, 2/* FCMPG */, 4/* DCMPL */,
            4/* DCMPG */, 1/* IFEQ */, 1/* IFNE */, 1/* IFLT */, 1/* IFGE */,
            1/* IFGT */, 1/* IFLE */, 2/* IF_ICMPEQ */, 2/* IF_ICMPNE */,
            2/* IF_ICMPLT */, 2 /* IF_ICMPGE */, 2/* IF_ICMPGT */,
            2/* IF_ICMPLE */, 2/* IF_ACMPEQ */, 2/* IF_ACMPNE */, 0/* GOTO */,
            0/* JSR */, 0/* RET */, 1/* TABLESWITCH */, 1/* LOOKUPSWITCH */,
            1/* IRETURN */, 2/* LRETURN */, 1/* FRETURN */, 2/* DRETURN */,
            1/* ARETURN */, 0/* RETURN */, 0/* GETSTATIC */, -1/* PUTSTATIC */,
            1/* GETFIELD */, -1/* PUTFIELD */, -2/* INVOKEVIRTUAL */,
            -2/* INVOKESPECIAL */, -2/* INVOKESTATIC */,
            -2/* INVOKEINTERFACE */, 0, 0/* NEW */, 1/* NEWARRAY */,
            1/* ANEWARRAY */, 1/* ARRAYLENGTH */, 1/* ATHROW */,
            1/* CHECKCAST */, 1/* INSTANCEOF */, 1/* MONITORENTER */,
            1/* MONITOREXIT */, 0/* WIDE */, -3/* MULTIANEWARRAY */,
            1/* IFNULL */, 1/* IFNONNULL */, 0/* GOTO_W */, 0 /* JSR_W */};

    /** Map of location to op-code block. */
    Map<Integer, ParserAnalyzer.Block> blocks_ = new HashMap<Integer, ParserAnalyzer.Block>();

    /** The OpCodes relating to the byte code. */
    ParserAnalyzer.OpCode[] code_;

    /** The constant pool */
    final ConstantPool cp_;

    /** Do we find the maximum stack? */
    boolean findMaxStack_ = true;

    /** Do we find maximum local variables? */
    boolean findMaxVars_ = true;

    /** Maximum local variables found so far */
    int maxLocalVars_ = -1;

    /** Maximum stack identified so far */
    int maxStack_ = -1;

    /** Writer for debug messages */
    private PrintWriter debug_ = null;

    /** Are we generating debug output? */
    private boolean isDebug_ = false;


    /**
     * Analyze some byte code.
     * 
     * @param maxStack
     *            previously identified max stack, or -1 to find
     * @param maxLocalVars
     *            previously identified max var count, or -1 to find
     * @param method
     *            the method definition
     */
    public ParserAnalyzer(int maxStack, int maxLocalVars, Method method) {
        cp_ = method.getConstantPool();
        maxStack_ = maxStack;
        maxLocalVars_ = maxLocalVars;
        if( maxLocalVars_ == -1 ) {
            findMaxVars_ = true;

            int access = method.getAccess();
            // static methods do not have an initial object reference
            maxLocalVars_ = (access & Access.ACC_STATIC) != 0 ? 0 : 1;

            // one local var per parameter
            maxLocalVars_ += Method.getArgsForType(method.getType().get());
        }

        Code attrCode = method.getCode();

        byte[] code = attrCode.getCode();

        if( maxStack_ == -1 ) {
            findMaxStack_ = true;
            code_ = new ParserAnalyzer.OpCode[code.length];
            maxStack_ = 0;
        }

        if( findMaxStack_ || findMaxVars_ ) {
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
            }

            Parser parser = new Parser(this);
            for(byte element:code) {
                parser.parse(element);
            }
            exploreBlocks(0, 0);
            Handler[] handlers = attrCode.getHandlers();
            for(Handler h:handlers) {
                exploreBlocks(1, h.getHandlerPC());
            }
        }
    }


    /**
     * Build the block that starts at the given location. The block runs until
     * some kind of branch occurs.
     * 
     * @param location
     *            starting point of block
     * @return the block
     */
    ParserAnalyzer.Block buildBlock(Integer location) {
        // check if we have already built this block
        ParserAnalyzer.Block block = blocks_.get(location);
        if( block != null ) return block;

        int p = location.intValue();

        block = new Block();
        while( true ) {
            ParserAnalyzer.OpCode opc = code_[p];

            // update stack usage and high-water
            block.delta_ += opc.delta_;
            if( block.delta_ > block.hwm_ ) block.hwm_ = block.delta_;

            // is this a branch? if so it ends the block
            if( opc.branchTo_.length != 0 ) {
                // record all branch destinations in the block. Note that
                // op-codes use offsets and blocks use addresses.
                block.branchTo_ = new Integer[opc.branchTo_.length];
                for(int i = 0;i < opc.branchTo_.length;i++) {
                    block.branchTo_[i] = Integer.valueOf(p + opc.branchTo_[i]);
                }
                // if debugging, report new block
                if( isDebug_ )
                    debug_.printf("%6d : %s \t: %s\n", Integer.valueOf(p),
                            String.valueOf(opc), String.valueOf(block));
                return block;
            }

            // Test the op-code to see if it is a method exit
            Byte opCode = Byte.valueOf(opc.opCode_);
            if( Parser.OP_EXIT.contains(opCode) ) {
                // handle JSR and RET specially
                if( (opc.opCode_ == OpCodes.JSR)
                        || (opc.opCode_ == OpCodes.JSR_W) ) {
                    block.retFromJSR_ = Integer.valueOf(p + opc.length_);
                } else if( opc.opCode_ == OpCodes.RET ) {
                    block.exitViaRET_ = true;
                }
                if( isDebug_ )
                    debug_.printf("%6d : %s \t: %s\n", Integer.valueOf(p),
                            String.valueOf(opc), String.valueOf(block));

                // return block
                return block;
            }

            // continue building block
            if( isDebug_ )
                debug_.printf("%6d : %s \t: %s\n", Integer.valueOf(p),
                        String.valueOf(opc), String.valueOf(block));
            p = p + opc.length_;
        }
    }


    /**
     * Explore blocs from a given starting location and starting point. The
     * meaningful starting points are the code entry at location zero with an
     * empty stack and each exception handler with a single object on the stack
     * as the raising of an exception clears the stack.
     * 
     * @param stack
     *            current stack depth
     * @param location
     *            where in the code the block starts
     */
    void exploreBlocks(int stack, int location) {
        Stack<Integer> jsrs = new Stack<Integer>();
        Map<Integer, Integer> visited = new HashMap<Integer, Integer>();
        Set<Integer> crumbs = new HashSet<Integer>();
        exploreBlocks(stack, Integer.valueOf(location), visited, crumbs, jsrs);
    }


    /**
     * Recursively explore blocks of code to ascertain how the stack size
     * varies.
     * 
     * @param stack
     *            the stack depth at the start of the block
     * @param location
     *            the location of the block
     * @param visited
     *            which blocks have already been visited and the stack size on
     *            entry
     * @param crumbs
     *            the current path of blocks to this block
     * @param jsrs
     *            JSR return points
     */
    void exploreBlocks(int stack, Integer location,
            Map<Integer, Integer> visited, Set<Integer> crumbs,
            Stack<Integer> jsrs) {
        // have we explored this before?
        Integer previous = visited.get(location);
        if( previous != null ) {
            // We've explored here before. If stack is same or smaller, no
            // need to continue.
            if( stack <= previous.intValue() ) return;

            // We've explored here before and stack is bigger. If we are
            // looping that is bad as the stack is growing in the loop. We
            // cannot detect a loop exit condition so the stack will grow
            // without limit.
            if( crumbs.contains(location) )
                throw new YabelDecompileException(
                        "Stack growth in loop detected. Start of loop at "
                                + location);
        }
        visited.put(location, Integer.valueOf(stack));
        crumbs.add(location);

        ParserAnalyzer.Block block = buildBlock(location);
        int ms = stack + block.hwm_;
        if( ms > maxStack_ ) maxStack_ = ms;
        stack += block.delta_;

        // stack should never go negative
        if( stack < 0 )
            throw new YabelDecompileException(
                    "Stack exhausted at or after location " + location);

        // Class files cannot handle a stack size over 0xffff
        if( stack > 0xffff )
            throw new YabelDecompileException(
                    "Stack overflowed 65535 at or after location " + location);

        // if this block ends in a RET, return to where we last JSRed from
        if( block.exitViaRET_ ) {
            if( jsrs.isEmpty() )
                throw new YabelDecompileException(
                        "Encountered RET with no previous JSR. RET is on or after byte "
                                + location);
            Integer next = jsrs.pop();
            exploreBlocks(stack, next, visited, crumbs, jsrs);
            crumbs.remove(location);
            return;
        }

        // if we are JSRing, note the return address
        if( block.retFromJSR_ != null ) jsrs.push(block.retFromJSR_);

        // if a branch, explore all the branches
        if( block.branchTo_ != null ) {
            for(Integer next:block.branchTo_) {
                exploreBlocks(stack, next, visited, crumbs, jsrs);
            }
        }

        // We are done here.
        crumbs.remove(location);
    }


    public int getMaxLocalVars() {
        return maxLocalVars_;
    }


    public int getMaxStack() {
        return maxStack_;
    }


    /**
     * Process an op-code. The op code is analyzed to ascertain which local
     * variables it operates on and how many stack entries it uses.
     * 
     * @param position
     *            where in the code we are
     * @param buffer
     *            the complete op-code
     * @param length
     *            the length of the op-code
     */
    @Override
    public void opCodeFinish(int position, byte[] buffer, int length) {
        if( findMaxVars_ ) {
            updateMaxVars(buffer);
        }

        if( findMaxStack_ ) {
            updateMaxStack(position, buffer, length);
        }
    }


    private void updateMaxStack(int position, byte[] buffer, int length) {
        int delta = 0;
        int opCode = IO.readU1(buffer, 0);
        int extra = STACK_MADE[opCode];
        switch (extra) {
        case -1: {
            // it is a field access
            int v = IO.readU2(buffer, 1);
            ConstantRef cr = cp_.validate(v, ConstantRef.class);
            String type = cr.getType().get();
            if( type.equals("D") ) {
                // get double
                delta += 2;
            } else if( type.equals("J") ) {
                // get long
                delta += 2;
            } else {
                delta += 1;
            }
            break;
        }
        case -2: {
            // it is a method call
            int v = IO.readU2(buffer, 1);
            ConstantRef cr = cp_.validate(v, ConstantRef.class);
            String type = cr.getType().get();
            if( type.endsWith(")D") ) {
                // returns double
                delta += 2;
            } else if( type.endsWith(")J") ) {
                // returns long
                delta += 2;
            } else if( !type.endsWith(")V") ) {
                // does not return void
                delta += 1;
            }
            break;
        }
        default:
            delta += extra;
        }

        int less = STACK_USED[opCode];
        switch (less) {
        case -1: {
            // it is a field access
            int v = IO.readU2(buffer, 1);
            ConstantRef cr = cp_.validate(v, ConstantRef.class);
            String type = cr.getType().get();
            if( type.equals("D") ) {
                // get double
                delta -= 2;
            } else if( type.equals("J") ) {
                // get long
                delta -= 2;
            } else {
                delta -= 1;
            }
            if( buffer[0] == OpCodes.PUTFIELD ) {
                // also uses object ref
                delta -= 1;
            }
            break;
        }
        case -2: {
            // it is a method call
            int v = IO.readU2(buffer, 1);
            ConstantRef cr = cp_.validate(v, ConstantRef.class);
            String type = cr.getType().get();
            delta -= Method.getArgsForType(type);
            if( buffer[0] != OpCodes.INVOKESTATIC ) {
                // if it is not a static, it uses an object ref
                delta -= 1;
            }
            break;
        }
        case -3: {
            // it is a multianewarray call
            // byte 3 is the number of dimensions of the array
            delta -= IO.readU1(buffer, 3);
            break;
        }
        default:
            delta -= less;
        }

        ParserAnalyzer.OpCode opc = new OpCode();
        opc.delta_ = delta;
        opc.opCode_ = buffer[0];
        opc.length_ = length;

        // Note the offsets for any branches
        Byte b = Byte.valueOf(buffer[0]);
        if( Parser.BRANCH_OPS.contains(b) ) {
            switch (buffer[0]) {
            case OpCodes.GOTO_W:
                // falls through
            case OpCodes.JSR_W:
                // 4-byte offset
                opc.branchTo_ = new int[] { IO.readS4(buffer, 1) };
                break;
            case OpCodes.GOTO:
                // falls through
            case OpCodes.JSR:
                // 2-byte offset
                opc.branchTo_ = new int[] { IO.readS2(buffer, 1) };
                break;
            case OpCodes.LOOKUPSWITCH: {
                int s = 8 - (position % 4);
                int np = IO.readS4(buffer, s);
                int[] to = new int[np + 1];
                to[0] = IO.readS4(buffer, s - 4);
                for(int i = 1;i <= np;i++) {
                    to[i] = IO.readS4(buffer, s + 8 * i);
                }
                opc.branchTo_ = to;
                break;
            }
            case OpCodes.TABLESWITCH: {
                int s = 12 - (position % 4);
                int low = IO.readS4(buffer, s - 4);
                int high = IO.readS4(buffer, s);
                int[] to = new int[high - low + 2];
                to[0] = IO.readS4(buffer, s - 8);
                for(int i = 1;i < to.length;i++) {
                    to[i] = IO.readS4(buffer, s + 4 * i);
                }
                opc.branchTo_ = to;
                break;
            }
            default:
                // 2-byte offset conditional
                opc.branchTo_ = new int[] { IO.readS2(buffer, 1), 3 };
                break;
            }
        }
        if( isDebug_ )
            debug_.printf("MaxStack: %6d : %s\n", Integer.valueOf(position),
                    String.valueOf(opc));
        code_[position] = opc;
    }


    private void updateMaxVars(byte[] buffer) {
        Byte b = Byte.valueOf(buffer[0]);

        // check fixed variable operators
        if( VAR_OPS_0.contains(b) ) maxLocalVars_ = Math.max(maxLocalVars_, 1);
        if( VAR_OPS_1.contains(b) ) maxLocalVars_ = Math.max(maxLocalVars_, 2);
        if( VAR_OPS_2.contains(b) ) maxLocalVars_ = Math.max(maxLocalVars_, 3);
        if( VAR_OPS_3.contains(b) ) maxLocalVars_ = Math.max(maxLocalVars_, 4);
        if( VAR_OPS_4.contains(b) ) maxLocalVars_ = Math.max(maxLocalVars_, 5);

        // check variable operators
        if( VAR_OPS_X.contains(b) ) {
            int i = IO.readU1(buffer, 1);
            maxLocalVars_ = Math.max(maxLocalVars_, i + 1);
        }
        if( VAR_OPS_X1.contains(b) ) {
            int i = IO.readU1(buffer, 1);
            maxLocalVars_ = Math.max(maxLocalVars_, i + 2);
        }

        // handle WIDE
        if( buffer[0] == OpCodes.WIDE ) {
            b = Byte.valueOf(buffer[1]);
            if( VAR_OPS_X.contains(b) ) {
                int i = IO.readU2(buffer, 2);
                maxLocalVars_ = Math.max(maxLocalVars_, i + 1);
            }
            if( VAR_OPS_X1.contains(b) ) {
                int i = IO.readU2(buffer, 2);
                maxLocalVars_ = Math.max(maxLocalVars_, i + 2);
            }
        }
    }
}