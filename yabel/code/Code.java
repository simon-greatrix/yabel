package yabel.code;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import yabel.ClassBuilder;
import yabel.ClassData;
import yabel.Field;
import yabel.Method;
import yabel.OpCodes;
import yabel.attributes.Attribute;
import yabel.attributes.AttributeList;
import yabel.constants.Constant;
import yabel.constants.ConstantClass;
import yabel.constants.ConstantFieldRef;
import yabel.constants.ConstantInterfaceMethodRef;
import yabel.constants.ConstantMethodRef;
import yabel.constants.ConstantNumber;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantString;
import yabel.io.IO;
import yabel.parser.Decompiler;
import yabel.parser.ParserAnalyzer;

/**
 * A Code attribute for a method. No support for LineNumberTable nor
 * LocalVariableTable attributes.
 * 
 * @author Simon Greatrix
 */
public class Code extends Attribute {

    /** The actual characters we escape */
    private final static String ESCAPE_CHARS = "\b\t\n\f\r\"'\\";

    /** The characters used to represent escape sequences */
    private final static String ESCAPE_VALS = "btnfr\"'\\";


    /**
     * Escape a string according to the rules of Java string escaping.
     * 
     * @param val
     *            the string to escape
     * @return the escaped string
     */
    public static String escapeJava(String val) {
        StringBuilder buf = new StringBuilder(val.length());
        escapeJava(buf, val);
        return buf.toString();
    }


    /**
     * Escape a string according to the rules of Java string escaping and append
     * it onto the provided buffer.
     * 
     * @param buf
     *            the buffer to append to.
     * @param val
     *            the value to escape.
     */
    public static void escapeJava(StringBuilder buf, String val) {
        for(int i = 0;i < val.length();i++) {
            char ch = val.charAt(i);
            int p = ESCAPE_CHARS.indexOf(ch);
            if( p != -1 ) {
                buf.append('\\');
                buf.append(ESCAPE_VALS.charAt(p));
                continue;
            }

            // handle regular characters
            if( (ch > 0x20) && (ch < 0x7f) && (ch != ' ') && (ch != '{')
                    && (ch != '}') && (ch != ':') ) {
                buf.append(ch);
                continue;
            }

            // handle octal escapes
            if( ch < 0xff ) {
                // ensure the character following the octal escape is not an
                // octal digit
                int i1 = i + 1;
                char ch2 = (i1 < val.length()) ? val.charAt(i1) : 'x';
                if( (ch2 < '0') || ('7' < ch2) ) {
                    // handle special chars and normal octal escapes
                    buf.append('\\');
                    if( ch >= 0100 )
                        buf.append(Character.forDigit((ch >> 6) & 0x7, 8));
                    if( ch >= 0010 )
                        buf.append(Character.forDigit((ch >> 3) & 0x7, 8));
                    buf.append("01234567".charAt((ch) & 0x7));
                    continue;
                }
            }

            // handle 16-bit escapes
            buf.append("\\u");
            buf.append(Character.forDigit((ch >> 12) & 0xf, 16));
            buf.append(Character.forDigit((ch >> 8) & 0xf, 16));
            buf.append(Character.forDigit((ch >> 4) & 0xf, 16));
            buf.append(Character.forDigit(ch & 0xf, 16));
        }
    }


    /**
     * Decode a Java escape sequence.
     * 
     * @param val
     *            the full string we are un-escaping
     * @param pos
     *            the parse position in the string
     * @param buf
     *            the buffer we are writing out onto
     */
    private static int parseEscape(String val, int i, StringBuilder buf) {
        if( i >= val.length() ) {
            buf.append('\\');
            return i;
        }
        char ch = val.charAt(i);
        int p = ESCAPE_VALS.indexOf(ch);
        if( p != -1 ) {
            buf.append(ESCAPE_CHARS.charAt(p));
            return i + 1;
        }

        // is it an octal escape?
        if( ('0' <= ch) && (ch <= '7') ) {
            int r[] = new int[3];
            for(int j = 0;j < 3;j++) {
                if( (i + j) < val.length() ) {
                    char c = val.charAt(i + j);
                    r[j] = Character.digit(c, 8);
                } else {
                    r[j] = -1;
                }
            }

            // first digit
            int v = r[0];
            if( r[1] == -1 ) {
                // if no more, we have char from one digit
                buf.append((char) v);
                return i + 1;
            }

            // second digit
            v = v * 8 + r[1];
            if( r[2] == -1 ) {
                // if no more, we have char from two digits
                buf.append((char) v);
                return i + 2;
            }

            // third digit
            v = v * 8 + r[2];
            if( v > 0377 ) {
                throw new YabelParseException("Invalid octal escape in " + val);
            }

            // must have char now
            buf.append((char) v);
            return i + 3;
        }

        // handle 16-bit escapes
        if( ch == 'u' ) {
            // must have 4 digits
            if( (i + 4) >= val.length() ) {
                throw new YabelParseException(
                        "Invalid \\u escape (too few characters) in " + val);
            }

            // get and parse hexadecimal number
            String v = val.substring(i + 1, i + 5).toLowerCase();
            int v2 = 0;
            for(int j = 0;j < 4;j++) {
                ch = v.charAt(j);
                int v3 = Character.digit(ch, 16);
                if( v3 == -1 ) {
                    throw new YabelParseException("Invalid \\u escape (" + ch
                            + " is not a hex digit) in " + val);
                }
                v2 = v2 * 16 + v3;
            }

            // got character
            buf.append((char) v2);
            return i + 5;
        }

        // unrecognised escape
        throw new YabelParseException("Unhandled escape sequence (\\" + ch
                + ") in " + val);
    }


    /**
     * Un-escape a string which has been escaped using the rules of Java string
     * escaping.
     * 
     * @param val
     *            the escaped string
     * @return the un-escaped string
     */
    public static String unescapeJava(String val) {
        StringBuilder buf = new StringBuilder(val.length());
        unescapeJava(buf, val);
        return buf.toString();
    }


    /**
     * Un-escape a string which has been escaped using the rules of Java string
     * escaping onto the provided string buffer.
     * 
     * @param buf
     *            the buffer to append onto
     * @param val
     *            the value to un-escape
     */
    public static void unescapeJava(StringBuilder buf, String val) {
        int i = 0;
        while( i < val.length() ) {
            char ch = val.charAt(i);
            if( ch == '\\' ) {
                i = parseEscape(val, i + 1, buf);
            } else {
                buf.append(ch);
                i++;
            }
        }
    }

    /** The attribute list for this Code block */
    private AttributeList attrList_ = new AttributeList();

    /** The class this code is part of */
    private ClassBuilder class_ = null;

    /** The byte code */
    private byte[] code_ = null;

    /** The constant pool associated with this */
    private final ConstantPool cp_;

    /** The exception handlers */
    private List<Handler> handler_ = new ArrayList<Handler>();

    /** History of this code's creation */
    private List<ClassData> history_ = new ArrayList<ClassData>();

    /**
     * Labels in compiled code. First element of list is label target, rest are
     * label references.
     */
    private Map<String, Label> labels_ = new HashMap<String, Label>();

    /** The local variables required */
    private int maxLocals_ = -1;

    /** The stack slots required */
    private int maxStack_ = -1;

    /** The method this code implements */
    private Method method_ = null;

    /** First pass compilation */
    private CompileStream pass1_ = new CompileStream();


    /**
     * New Code attribute.
     * 
     * @param cp
     *            the constant pool
     */
    public Code(ConstantPool cp) {
        super(cp, ATTR_CODE);
        cp_ = cp;
    }


    /**
     * New Code attribute from class data
     * 
     * @param cp
     *            the constant pool
     * @param cd
     *            the class data for this Code attribute
     */
    public Code(ConstantPool cp, ClassData cd) {
        super(cp, cd);
        cp_ = cp;

        for(ClassData d:cd.getListSafe(ClassData.class, "build")) {
            String bytes = d.get(String.class, "bytes");
            if( bytes != null ) {
                appendCode(IO.decode(bytes));
            } else {
                String raw = d.getSafe(String.class, "source");
                ClassData d2 = d.get(ClassData.class, "replacements");
                compile(raw, d2);
            }
        }

        for(ClassData d:cd.getListSafe(ClassData.class, "handlers")) {
            addHandler(d);
        }

        maxLocals_ = cd.getSafe(Integer.class, "maxLocals").intValue();
        maxStack_ = cd.getSafe(Integer.class, "maxStack").intValue();

        attrList_ = new AttributeList(cp, cd.getListSafe(ClassData.class,
                "attributes"));
    }


    /**
     * New Code attribute.
     * 
     * @param cp
     *            the constant pool
     * @param input
     *            the stream
     */
    public Code(ConstantPool cp, InputStream input) throws IOException {
        super(cp, Attribute.ATTR_CODE);
        cp_ = cp;

        IO.readS4(input); // ignored
        int maxStack = IO.readU2(input);
        int maxLocals = IO.readU2(input);

        // read code
        int len = IO.readS4(input);
        byte[] code = new byte[len];
        for(int i = 0;i < len;i++) {
            code[i] = (byte) IO.readU1(input);
        }
        setByteCode(maxStack, maxLocals, code);

        // read exception handlers
        len = IO.readU2(input);
        for(int i = 0;i < len;i++) {
            handler_.add(new Handler(cp_, input));
        }

        // read attributes
        attrList_ = new AttributeList(cp_, input);
    }


    /**
     * Add an exception handler
     * 
     * @param handler
     *            class data representation of handler
     */
    public void addHandler(ClassData handler) {
        Handler h = new Handler(cp_, handler);
        handler_.add(h);
    }


    /**
     * Add an exception handler
     * 
     * @param startPC
     *            the start byte code index
     * @param endPC
     *            the end byte code index
     * @param handlerPC
     *            the handler byte code index
     * @param catchType
     *            the exception class (null for all)
     */
    public void addHandler(int startPC, int endPC, int handlerPC,
            String catchType) {
        ConstantClass cc = null;
        if( catchType != null ) cc = new ConstantClass(cp_, catchType);

        Handler h = new Handler(startPC, endPC, handlerPC, cc);
        handler_.add(h);
    }


    /**
     * Add an exception handler
     * 
     * @param startLabel
     *            the start byte code label
     * @param endLabel
     *            the end byte code label
     * @param handlerLabel
     *            the handler byte code label
     * @param catchType
     *            the exception class (null for all)
     */
    public void addHandler(String startLabel, String endLabel,
            String handlerLabel, String catchType) {
        int startPC = getLabelLocation(startLabel);
        int endPC = getLabelLocation(endLabel);
        int handlerPC = getLabelLocation(handlerLabel);
        ConstantClass cc = null;
        if( catchType != null ) cc = new ConstantClass(cp_, catchType);

        Handler h = new Handler(startPC, endPC, handlerPC, cc);
        handler_.add(h);
    }


    /**
     * Append a specific byte sequence to the code being built.
     * 
     * @param code
     *            the byte sequence to append
     */
    public void appendCode(byte[] code) {
        pass1_.write(code);
        ClassData cd = new ClassData();
        cd.put("bytes", IO.encode(code));
        history_.add(cd);
    }


    /**
     * Append a signed 4 byte value to the code currently being compiled.
     * 
     * @param v
     *            the value to append
     */
    void appendS4(int v) {
        IO.writeS4(pass1_, v);
    }


    /**
     * Append a single byte to the code currently being compiled.
     * 
     * @param b
     *            the byte to append
     */
    void appendU1(byte b) {
        pass1_.write(b);
    }


    /**
     * Append an U2 value to the code currently being compiled.
     * 
     * @param i
     *            the value to append
     */
    void appendU2(int i) {
        IO.writeU2(pass1_, i);
    }


    /**
     * Append the required number of zeros for switch padding. This assumes the
     * switch op-code has just been written.
     */
    void appendSwitchPadding() {
        int s = 3 - ((pass1_.size() - 1) % 4);
        while( s > 0 ) {
            pass1_.write(0);
            s--;
        }
    }

    /** Map of operand names to their compilers */
    private static Map<String, CodeOperand> OP_CODES = new HashMap<String, CodeOperand>();

    static {
        load(CodeArrays.values());
        load(CodeClass.values());
        load(CodeConstant.values());
        load(CodeField.values());
        load(CodeInterface.values());
        load(CodeLoadStore.values());
        load(CodeMethod.values());
        load(CodeSwitch.values());
        load(CodeU1U2.values());
        load(CodeLabel.values());
        load(new CodeOperand[] { CodeIINC.INSTANCE, CodeLDC.INSTANCE });
    }


    private static final void load(CodeOperand[] ops) {
        for(CodeOperand op:ops) {
            OP_CODES.put(op.name().toUpperCase(), op);
        }
    }


    /**
     * <p>
     * Compile some byte code instructions. The byte code is specified as white
     * space separated mnemonics. Code can also be generated using the supplied
     * ClassData and these special tokens:
     * </p>
     * 
     * <table border=1>
     * <tr>
     * <th>Token</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>{property}</td>
     * <td>Look up the String "property" in the ClassData and compile that</td>
     * </tr>
     * <tr>
     * <td><i>token:</i>{property}</td>
     * <td>Look up the String "property" in the ClassData and use that as the
     * argument for the token. This is done recursively until the argument is
     * not enclosed in {}s.</td>
     * </tr>
     * <tr>
     * <td>class:property<br>
     * class:java/lang/String</td>
     * <td>Generates a U2 value reference to a class constant in the constant
     * pool for the specified class. The class is either the indicated String in
     * the ClassData, or explicit.</td>
     * </tr>
     * <tr>
     * <td>iconst:property<br>
     * iconst:456</td>
     * <td>Generate an appropriate ICONST, BIPUSH, SIPUSH, LDC, or LDC_W
     * statement to specify the indicated integer constant. The constant is
     * either specified as an integer in the ClassData, or explicit</td>
     * </tr>
     * <tr>
     * <td>number:property</td>
     * <td>Generate an U2 value reference to a numeric constant in the constant
     * pool. The value must be in the indicated property of the ClassData.</td>
     * </tr>
     * <tr>
     * <td>field:property<br>
     * field:myLocalField</td>
     * <td>Generate an U2 value reference to a class field. The property can be
     * either a String, indicating an already defined in this class, or a three
     * element StringArray giving class, name and type in class file format.
     * Alternatively it can simply be the explicit name of an already defined
     * field in this class.</td>
     * </tr>
     * <tr>
     * <td>putField:property<br>
     * putField:myLocalField<br>
     * getField:property<br>
     * getField:myLocalField<br>
     * putStatic:property<br>
     * putStatic:myLocalField<br>
     * getStatic:property<br>
     * getStatic:myLocalField</td>
     * <td>Generate the appropriate instruction to access the specified field.
     * Specification is as for <code>field:</code>.</td>
     * </tr>
     * <tr>
     * <td>invokeInterface:property<br>
     * interfaceMethod:property</td>
     * <td>Generates an <code>INVOKEINTERFACE</code> instruction or the U2
     * referent to the method definition in the constant pool. The property must
     * be a three element StringArray specifying interface, name, and type.</td>
     * </tr>
     * <tr>
     * <td>&#64;:property</td>
     * <td>Defines a marker at this position. Each marker can only be defined
     * once.</td>
     * </tr>
     * <tr>
     * <td>&#35;:property</td>
     * <td>Replaced with a U2 value indicating the location of the marker
     * relative to the preceeding instruction. This can be used to implement
     * IFs, GOTOs and JSRs like this:
     * <code>IFEQ &#35;else {handleTrue} GOTO &#35;endOfIf &#64;else {handleFalse} &#64;endOfIf</code>
     * </td>
     * </tr>
     * <tr>
     * <td>&#35;4:property</td>
     * <td>Replaced with a U4 value indicating the location of the marker
     * relative to the preceeding instruction. This can be used to implement
     * GOTO_W, JSR_W, TABLESWITCH and LOOKUPSWITCH</td>
     * </tr>
     * <tr>
     * <td>LDC:property</td>
     * <td>Generates an LDC or LDC_W instruction to refer to the given constant
     * in the constant pool. The ClassData must contain a value for "property"
     * which is either a String or a Number.</td>
     * </tr>
     * <tr>
     * <td>ALOAD:property<br>
     * ALOAD:12<br>
     * DLOAD:property<br>
     * DLOAD:12<br>
     * FLOAD:property<br>
     * FLOAD:12<br>
     * ILOAD:property<br>
     * ILOAD:12<br>
     * LLOAD:property<br>
     * LLOAD:12</td>
     * <td>Generates an appropriate load or wide load instruction. The ClassData
     * should contain an Integer for "property".</td>
     * </tr>
     * <tr>
     * <td>ASTORE:property<br>
     * ASTORE:12<br>
     * DSTORE:property<br>
     * DSTORE:12<br>
     * FSTORE:property<br>
     * FSTORE:12<br>
     * ISTORE:property<br>
     * ISTORE:12<br>
     * LSTORE:property<br>
     * LSTORE:12</td>
     * <td>Generates an appropriate store or wide store instruction. The
     * ClassData should contain an Integer for "property".</td>
     * </tr>
     * <tr>
     * <td>IINC:property<br>
     * IINC:12:34<br>
     * IINC:varProp:34<br>
     * IINC:12:constProp<br>
     * IINC:varProp:constProp</td>
     * <td>Generates an appropriate iinc or wide iinc instruction. The ClassData
     * should contain a two element Integer Array for "property", or an integer
     * for "varProp" and for "constProp".</td>
     * </tr>
     * <tr>
     * <td>RET:property<br>
     * RET:12</td>
     * <td>Generates an appropriate ret or wide ret instruction. The ClassData
     * should contain an Integer for "property".</td>
     * </tr>
     * <tr>
     * <td>method:property<br>
     * method:123<br>
     * invokeSpecial:property<br>
     * invokeSpecial:123<br>
     * invokeVirtual:property<br>
     * invokeVirtual:123<br>
     * invokeStatic:property<br>
     * invokeStatic:123</td>
     * <td>Generates an U2 reference, INVOKESPECIAL, INVOKEVIRTUAL or
     * INVOKESTATIC instruction. The ClassData's "property" should contain
     * either a two element StringArray specifying name and type of a method in
     * this class, or a three element String Array specifying class, name and
     * type of the method, or an Integer giving the index of the method
     * definition in the constant pool. If an explicit value is given, it is the
     * constant pool index.</td>
     * </tr>
     * <tr>
     * <td>U1:property<br>
     * U1:123</td>
     * <td>Outputs a single byte, either the specified integer in the ClassData
     * or the explicit value</td>
     * </tr>
     * <tr>
     * <td>U2:property<br>
     * U2:1234</td>
     * <td>Outputs an U2 pair of bytes, either the specified integer in the
     * ClassData or the explicit value</td>
     * </tr>
     * <tr>
     * <td>S4:property<br>
     * S4:123</td>
     * <td>Outputs a signed four byte value, either the specified integer in the
     * ClassData or the explicit value</td>
     * </tr>
     * </table>
     * 
     * <p>
     * All the mnemonics and tokens are case insensitive, but the ClassData
     * properties are case sensitive.
     * </p>
     * 
     * @param raw
     *            byte code
     * @param cd
     *            ClassData value substitutions
     */
    public void compile(String raw, ClassData cd) {
        if( ClassBuilder.DEBUG ) {
            System.out.println("Compile input\n" + raw + "\n" + cd);
        }

        ClassData hist = new ClassData();
        hist.put("source", raw);
        if( cd != null ) hist.put("replacements", new ClassData(cd));
        history_.add(hist);

        if( cd == null ) cd = new ClassData();

        code_ = null;
        Iterator<List<String>> iter = new CodeTokenizer(raw, cd);
        while( iter.hasNext() ) {
            List<String> toks = iter.next();

            // If just one String, probably an op-code
            int size = toks.size();
            if( size == 2 ) {
                String tu = toks.get(1).toUpperCase();
                Byte c = OpCodes.OP_CODES.get(tu);
                if( c == null ) {
                    throw new YabelParseException("Unrecognized opcode:" + tu);
                }
                pass1_.write(c.intValue());
                continue;
            }

            // compile standard productions
            String lbl = toks.get(1).toUpperCase();
            CodeOperand op = OP_CODES.get(lbl);
            if( op != null ) {
                op.compile(this, toks, cd);
                continue;
            }

            throw new YabelParseException("Unrecognized opcode:" + toks.get(0));
        }
    }


    /**
     * Add a jump to a named location.
     * 
     * @param name
     *            the location's name
     * @param width
     *            the width of the jump (2 or 4 bytes)
     */
    protected void compileJump(String name, int width) {
        // get label's list
        Label label = labels_.get(name);
        if( label == null ) {
            // create new label
            label = new Label(name);
            labels_.put(name, label);
        }

        LabelUse use = new LabelUse();
        use.location_ = pass1_.size();
        use.opLoc_ = pass1_.getLastOpPosition();
        use.width_ = width;
        label.usage_.add(use);
        for(int i = 0;i < width;i++) {
            pass1_.write(0);
        }
    }


    /**
     * Decompile the code block.
     * 
     * @return representation of the decompiled code.
     */
    public ClassData decompile() {
        finalizeCode();

        Decompiler decomp = new Decompiler(cp_);
        decomp.parse(code_);
        for(Handler h:handler_) {
            decomp.addHandler(h);
        }

        ClassData cd = decomp.finish();
        cd.sort();
        return cd;
    }


    private void finalizeCode() {
        if( code_ != null ) return;

        code_ = pass1_.toByteArray();

        // set labels
        for(Label lbl:labels_.values()) {
            for(LabelUse use:lbl.usage_) {
                int jump = lbl.location_ - use.opLoc_;
                int here = use.location_;
                if( use.width_ == 2 ) {
                    // verify 2 byte offset is possible
                    if( jump < Short.MIN_VALUE || jump > Short.MAX_VALUE ) {
                        throw new YabelLabelException(
                                "Cannot jump distance of " + jump
                                        + " with 2-byte offset.");
                    }
                    code_[here] = (byte) ((jump >> 8) & 0xff);
                    code_[here + 1] = (byte) (jump & 0xff);
                } else {
                    // 4 byte offset
                    code_[here] = (byte) ((jump >> 24) & 0xff);
                    code_[here + 1] = (byte) ((jump >> 16) & 0xff);
                    code_[here + 2] = (byte) ((jump >> 8) & 0xff);
                    code_[here + 3] = (byte) (jump & 0xff);
                }
            }
        }
    }


    /**
     * Get a class reference
     * 
     * @param name
     *            name of class
     * @return reference
     */
    protected int getClassRef(String name) {
        ConstantClass c = new ConstantClass(cp_, name);
        return c.getIndex();
    }


    /**
     * Get the byte-code associated with this method. Note that once this has
     * been called the byte-code is fixed unless a <code>reset()</code> is
     * performed.
     * 
     * @return the byte-code
     */
    public byte[] getCode() {
        finalizeCode();
        byte[] newCode = new byte[code_.length];
        System.arraycopy(code_, 0, newCode, 0, code_.length);
        return newCode;
    }


    /**
     * Get the Constant for a given reference.
     * 
     * @param ref
     *            the reference
     * @return the Constant
     */
    protected Constant getConstant(int ref) {
        return cp_.get(ref);
    }


    /**
     * Get a constant reference
     * 
     * @param number
     *            value
     * @return reference
     */
    protected int getConstantRef(Number number) {
        Constant c = new ConstantNumber(cp_, number);
        return c.getIndex();
    }


    /**
     * Get a constant reference
     * 
     * @param string
     *            value
     * @return reference
     */
    protected int getConstantRef(String string) {
        Constant c = new ConstantString(cp_, string);
        return c.getIndex();
    }


    /**
     * Get a reference to some kind of ConstantRef given a type and value.
     * 
     * @param raw
     *            the raw source to report in errors
     * @param type
     *            the type of constant
     * @param value
     *            the value of the constant
     * @return the constant's index in the pool
     */
    protected int getConstantRef(String raw, String type, String value) {
        int i;
        Number n;
        if( type.equalsIgnoreCase("int") ) {
            // cast to Integer
            try {
                n = Integer.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new YabelBadNumberException(raw, value, type);
            }
            i = getConstantRef(n);
        } else if( type.equalsIgnoreCase("double") ) {
            // cast to Double
            try {
                n = Double.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new YabelBadNumberException(raw, value, type);
            }
            i = getConstantRef(n);
        } else if( type.equalsIgnoreCase("float") ) {
            // cast to Float
            try {
                n = Float.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new YabelBadNumberException(raw, value, type);
            }
            i = getConstantRef(n);
        } else if( type.equalsIgnoreCase("long") ) {
            // cast to Long
            try {
                n = Long.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new YabelBadNumberException(raw, value, type);
            }
            i = getConstantRef(n);
        } else if( type.equalsIgnoreCase("string") ) {
            // it's a String (no need to cast)
            i = getConstantRef(value);
        } else if( type.equalsIgnoreCase("class") ) {
            // cast to Class
            i = getClassRef(value);
        } else {
            // unknown type
            throw new YabelParseException(
                    "Constant data type \""
                            + type
                            + "\" was not recognised. Use int, double, float, long, class or string. Input was \""
                            + raw + "\"");
        }

        return i;
    }


    /**
     * Get a field reference in this class. Must exist already.
     * 
     * @param name
     *            field name
     * @return reference
     */
    protected int getFieldRef(String name) {
        Field f = class_.getField(name);
        if( f == null )
            throw new IllegalStateException("Field \"" + name+"\" is not yet defined in this class");

        ConstantFieldRef ref = new ConstantFieldRef(cp_, class_.getNameUtf8(),
                f.getName(), f.getType());
        return ref.getIndex();
    }


    /**
     * Get a field reference in another class.
     * 
     * @param clss
     *            class name
     * @param name
     *            field name
     * @param type
     *            field type
     * @return reference
     */
    protected int getFieldRef(String clss, String name, String type) {
        ConstantFieldRef ref = new ConstantFieldRef(cp_, clss, name, type);
        return ref.getIndex();
    }


    /**
     * Get the exception handlers associated with this Code block.
     * 
     * @return the exception handlers
     */
    public Handler[] getHandlers() {
        Handler[] h = new Handler[handler_.size()];
        return handler_.toArray(h);
    }


    /**
     * Is the field value a replacement? That is, does it start with a '{' and
     * end with a '}'?
     * 
     * @param v
     *            the field value
     * @return true if it is a replacement
     */
    protected static String isReplacement(String v) {
        int l = v.length() - 1;
        if( l < 1 ) return null;
        if( v.charAt(0) != '{' ) return null;
        if( v.charAt(l) != '}' ) return null;
        return v.substring(1, l);
    }


    /**
     * Get an integer.
     * 
     * @param cd
     *            the replacements.
     * @param fld
     *            the field to look up
     * @param raw
     *            the raw text for the op-code
     * @return the integer
     */
    protected static int getInt(ClassData cd, String fld, String raw) {
        String r = isReplacement(fld);
        if( r != null ) {
            Integer i = cd.getSafe(Integer.class, r);
            return i.intValue();
        }
        try {
            return Integer.parseInt(fld);
        } catch (NumberFormatException nfe) {
            throw new YabelBadNumberException(raw, fld, "integer");
        }
    }


    /**
     * Get an interface method reference.
     * 
     * @param clss
     *            interface
     * @param name
     *            method name
     * @param type
     *            method type
     * @return reference
     */
    protected int getInterfaceMethodRef(String clss, String name, String type) {
        ConstantInterfaceMethodRef c = new ConstantInterfaceMethodRef(cp_,
                clss, name, type);
        return c.getIndex();
    }


    /**
     * Define a new label at the current position.
     * 
     * @param name
     *            the label's name
     */
    protected void setLabel(String name) {
        Label label = getLabel(name);
        if( label.location_ != -1 ) {
            throw new YabelLabelException("Label \"" + name
                    + "\" defined more than once");
        }
        label.location_ = pass1_.size();
    }


    private Label getLabel(String name) {
        Label label = labels_.get(name);
        if( label == null ) {
            // create new label
            label = new Label(name);
            labels_.put(name, label);
        }
        return label;
    }


    private int getLabelLocation(String lbl) {
        Label li = labels_.get(lbl);
        if( li == null )
            throw new YabelLabelException("Label \"" + lbl
                    + "\" is not defined");
        int loc = li.location_;
        if( loc == -1 )
            throw new YabelLabelException("Label \"" + lbl
                    + "\" is not located");
        return loc;
    }


    public int getMaxLocals() {
        return maxLocals_;
    }


    public int getMaxStack() {
        return maxStack_;
    }


    /**
     * Get a method reference in this class.
     * 
     * @param name
     *            method name
     * @param type
     *            method type
     * @return reference
     */
    protected int getMethodRef(String name, String type) {
        ConstantMethodRef ref = new ConstantMethodRef(cp_, class_.getName(),
                name, type);
        return ref.getIndex();
    }


    /**
     * Get a method reference in another class
     * 
     * @param clss
     *            class name
     * @param name
     *            method name
     * @param type
     *            type
     * @return reference
     */
    protected int getMethodRef(String clss, String name, String type) {
        ConstantMethodRef ref = new ConstantMethodRef(cp_, clss, name, type);
        return ref.getIndex();
    }


    /**
     * Reset this code block to completely empty. Note that any constants
     * associated with the previous code will not be removed from the constant
     * pool.
     */
    public void reset() {
        code_ = null;
        handler_.clear();
        labels_.clear();
        history_.clear();
        maxLocals_ = -1;
        maxStack_ = -1;
        pass1_.reset();
    }


    /**
     * Set the byte code for this Code. Any existing code is discarded. Any
     * existing label markers are discarded. Exception handlers are retained.
     * 
     * @param maxStack
     *            The number of stack slots. Use -1 if it is to be calculated.
     * @param maxLocals
     *            The number of local variables. Use -1 if it is to be
     *            calculated.
     * @param code
     *            the byte code
     */
    public void setByteCode(int maxStack, int maxLocals, byte[] code) {
        pass1_.reset();
        maxStack_ = maxStack;
        maxLocals_ = maxLocals;
        labels_.clear();
        history_.clear();
        appendCode(code);
    }


    public void setMaxLocals(int maxLocals) {
        maxLocals_ = maxLocals;
    }


    public void setMaxStack(int maxStack) {
        maxStack_ = maxStack;
    }


    /**
     * Set the class and method this is code for
     * 
     * @param classBuilder
     *            the class
     * @param method
     *            the method
     */
    public void setOwner(ClassBuilder classBuilder, Method method) {
        class_ = classBuilder;
        method_ = method;
    }


    /**
     * Get the class data representation of this Code attribute.
     * 
     * @return the representation
     */
    public ClassData toClassData() {
        ClassData cd = makeClassData();
        cd.putList(ClassData.class, "build", history_);
        List<ClassData> handlers = new ArrayList<ClassData>(handler_.size());
        for(Handler h:handler_) {
            handlers.add(h.toClassData(cp_));
        }
        cd.putList(ClassData.class, "handlers", handlers);
        cd.put("maxLocals", Integer.valueOf(maxLocals_));
        cd.put("maxStack", Integer.valueOf(maxStack_));
        cd.putList(ClassData.class, "attributes", attrList_.toClassData());
        return cd;
    }


    /**
     * Write this Code attribute
     * 
     * @param baos
     *            output stream
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        finalizeCode();

        // first get the attribute size
        ByteArrayOutputStream attrs = new ByteArrayOutputStream();
        attrList_.writeTo(attrs);
        byte[] attrBytes = attrs.toByteArray();

        IO.writeU2(baos, attrId_.getIndex());
        IO.writeS4(baos, 10 + code_.length + handler_.size() * 8
                + attrBytes.length);

        if( (maxStack_ == -1) || (maxLocals_ == -1) ) {
            ParserAnalyzer analyzer = new ParserAnalyzer(maxStack_, maxLocals_,
                    method_);
            maxStack_ = analyzer.getMaxStack();
            maxLocals_ = analyzer.getMaxLocalVars();
        }
        IO.writeU2(baos, maxStack_);
        IO.writeU2(baos, maxLocals_);

        // write out code
        IO.writeS4(baos, code_.length);
        baos.write(code_, 0, code_.length);

        // write out exception handlers
        int s = handler_.size();
        IO.writeU2(baos, s);
        for(int i = 0;i < s;i++) {
            handler_.get(i).writeTo(baos);
        }

        // write attributes
        baos.write(attrBytes, 0, attrBytes.length);
    }
}
