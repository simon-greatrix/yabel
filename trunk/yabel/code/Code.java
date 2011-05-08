package yabel.code;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yabel.ClassBuilder;
import yabel.ClassData;
import yabel.Method;
import yabel.OpCodes;
import yabel.attributes.Attribute;
import yabel.attributes.AttributeList;
import yabel.attributes.AttributeListListener;
import yabel.attributes.LineNumberTable;
import yabel.code.operand.CodeArrays;
import yabel.code.operand.CodeClass;
import yabel.code.operand.CodeConstant;
import yabel.code.operand.CodeField;
import yabel.code.operand.CodeIINC;
import yabel.code.operand.CodeInterface;
import yabel.code.operand.CodeLDC;
import yabel.code.operand.CodeLabel;
import yabel.code.operand.CodeLoadStore;
import yabel.code.operand.CodeMethod;
import yabel.code.operand.CodeSwitch;
import yabel.code.operand.CodeU1U2;
import yabel.code.operand.CodeVar;
import yabel.constants.ConstantClass;
import yabel.constants.ConstantPool;
import yabel.io.IO;
import yabel.parser.Decompiler;
import yabel.parser.ParserAnalyzer;

/**
 * A Code attribute for a method. No support for LineNumberTable nor
 * LocalVariableTable attributes.
 * 
 * @author Simon Greatrix
 */
public class Code extends Attribute implements AttributeListListener {

    /** The actual characters we escape */
    private final static String ESCAPE_CHARS = "\b\t\n\f\r\"'\\";

    /** The characters used to represent escape sequences */
    private final static String ESCAPE_VALS = "btnfr\"'\\";

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
        load(new CodeOperand[] { CodeIINC.INSTANCE, CodeLDC.INSTANCE,
                CodeVar.INSTANCE });
    }


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
     * it onto the provided buffer. Characters that have special meaning in the
     * source code are also escaped as follows: ' ' as \40 or , ':' as \72 or
     * \u003a, '{' as \173 or \u007b, and '}' as \175 or \u007d. The comment
     * slash-slash and slash-star patterns are all made safe by escaping the
     * first character of the comment marker.
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

            // watch out for slash-slash and slash-star comments
            boolean notComment = true;
            int i1 = i + 1;
            char ch2 = (i1 < val.length()) ? val.charAt(i1) : 'x';
            if( ((ch == '/') && ((ch2 == '/') || (ch2 == '*')))
                    || ((ch == '*' && ch2 == '/')) ) notComment = false;

            // handle regular characters
            if( notComment && (ch > 0x20) && (ch < 0x7f) && (ch != ' ')
                    && (ch != '{') && (ch != '}') && (ch != ':') ) {
                buf.append(ch);
                continue;
            }

            // handle octal escapes
            if( ch < 0xff ) {
                // ensure the character following the octal escape is not an
                // octal digit

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


    private static final void load(CodeOperand[] ops) {
        for(CodeOperand op:ops) {
            OP_CODES.put(op.name().toUpperCase(), op);
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
    private final AttributeList attrList_;

    /** The constant pool associated with this */
    private final ConstantPool cp_;

    /** The exception handlers */
    private List<Handler> handler_ = new ArrayList<Handler>();

    /** History of this code's creation */
    private List<ClassData> history_ = new ArrayList<ClassData>();

    /** The local variables required */
    private int maxLocals_ = -1;

    /** The stack slots required */
    private int maxStack_ = -1;

    /** The method this code implements */
    private Method method_ = null;

    /** First pass compilation */
    private final CompilerOutput output_;


    /**
     * New Code attribute.
     * 
     * @param cp
     *            the constant pool
     */
    public Code(ConstantPool cp) {
        super(cp, ATTR_CODE);
        cp_ = cp;
        output_ = new CompilerOutput(cp_);
        attrList_ = new AttributeList();
        attrList_.setOwner(this);
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
        output_ = new CompilerOutput(cp_);

        List<ClassData> list = cd.getList(ClassData.class, "build");
        if( list != null ) {
            for(ClassData d:list) {
                String bytes = d.get(String.class, "bytes");
                if( bytes != null ) {
                    appendCode(IO.decode(bytes));
                } else {
                    String raw = d.getSafe(String.class, "source");
                    ClassData d2 = d.get(ClassData.class, "replacements");
                    compile(raw, d2);
                }
            }
        }

        //if handlers use labels as from decompiler, this errors
        list = cd.getList(ClassData.class, "handlers");
        if( list != null ) {
            for(ClassData d:list) {
                addHandler(d);
            }
        }

        maxLocals_ = cd.get(Integer.class, "maxLocals", Integer.valueOf(-1)).intValue();
        maxStack_ = cd.get(Integer.class, "maxStack", Integer.valueOf(-1)).intValue();

        attrList_ = new AttributeList(cp, cd.getList(ClassData.class,
                "attributes"));
        attrList_.setOwner(this);
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
        output_ = new CompilerOutput(cp_);

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
        attrList_.setOwner(this);
    }


    /**
     * Add an exception handler
     * 
     * @param handler
     *            class data representation of handler
     */
    public void addHandler(ClassData handler) {
        Handler h = new Handler(cp_, output_, handler);
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
        int startPC = output_.getLabelLocation(startLabel);
        int endPC = output_.getLabelLocation(endLabel);
        int handlerPC = output_.getLabelLocation(handlerLabel);
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
        ClassData cd = new ClassData();
        cd.put("bytes", IO.encode(code));
        history_.add(cd);
        if( method_ != null ) {
            output_.restart();
            output_.appendCode(code);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.attributes.AttributeListListener#attributeChanged(java.lang.String,
     *      yabel.attributes.Attribute)
     */
    @Override
    public void attributeChanged(String attrId, Attribute attr) {
    // TODO Auto-generated method stub

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
        if( method_ == null ) return;

        if( cd == null ) cd = new ClassData();

        output_.restart();
        Iterator<List<String>> iter = new CodeTokenizer(raw, cd);
        while( iter.hasNext() ) {
            List<String> toks = iter.next();

            // If just one String, probably an op-code
            int size = toks.size();
            if( size == 2 ) {
                String tu = toks.get(1).toUpperCase();
                if( "WIDE".equals(tu) ) {
                    output_.appendWide();
                    continue;
                }

                Byte c = OpCodes.OP_CODES.get(tu);
                if( c == null ) {
                    throw new YabelParseException("Unrecognized opcode:" + tu);
                }
                output_.appendU1(c.byteValue());
                continue;
            }

            // compile standard productions
            String lbl = toks.get(1).toUpperCase();
            CodeOperand op = OP_CODES.get(lbl);
            if( op != null ) {
                op.compile(output_, toks, cd);
                continue;
            }

            throw new YabelParseException("Unrecognized opcode:" + toks.get(0));
        }
    }


    /**
     * Decompile the code block.
     * 
     * @return representation of the decompiled code.
     */
    public ClassData decompile() {
        byte[] code = getCodeInternal();

        Decompiler decomp = new Decompiler(cp_);
        List<Attribute> lnt = attrList_.getAll(cp_,
                Attribute.ATTR_LINE_NUMBER_TABLE);
        for(Attribute a:lnt) {
            decomp.addLineNumbers((LineNumberTable) a);
        }

        decomp.parse(code);
        for(Handler h:handler_) {
            decomp.addHandler(h);
        }

        ClassData cd = decomp.finish();
        
        // we want to merge in the decompiler's attributes
        List<ClassData> attrsDecomp = cd.getList(ClassData.class, "attributes");
        List<ClassData> attrsSrc = attrList_.toClassData();
        for(int i=0;i<attrsSrc.size();i++) {
            ClassData a = attrsSrc.get(i);
            String aName = a.getSafe(String.class,"name");
            for(int j=0;j<attrsDecomp.size();j++) {
                ClassData d = attrsDecomp.get(j);
                String dName = d.getSafe(String.class,"name");
                if( aName.equals(dName) ) {
                    attrsSrc.set(i,d);
                    attrsDecomp.remove(j);
                    break;
                }
            }
        }
        attrsSrc.addAll(attrsDecomp);
        cd.putList(ClassData.class,"attributes",attrsSrc);
        cd.sort();
        return cd;
    }


    /**
     * Get the byte-code associated with this method. Note that once this has
     * been called the byte-code is fixed unless a <code>reset()</code> is
     * performed.
     * 
     * @return the byte-code
     */
    public byte[] getCode() {
        byte[] code = getCodeInternal();
        byte[] newCode = new byte[code.length];
        System.arraycopy(code, 0, newCode, 0, code.length);
        return newCode;
    }


    /**
     * Get the byte-code associated with this method. Note that once this has
     * been called the byte-code is fixed unless a <code>reset()</code> is
     * performed.
     * 
     * @return the byte-code
     */
    private byte[] getCodeInternal() {
        byte[] code = output_.finalizeCode();

        Pattern p = Pattern.compile("LINE_(\\d+)(_.*)?");
        Collection<Label> lbls = output_.getAllLabels();
        List<Attribute> attrs = attrList_.getAll(cp_,
                Attribute.ATTR_LINE_NUMBER_TABLE);
        LineNumberTable lnt = null;

        for(Label lbl:lbls) {
            if( lbl.getLocation() == -1 ) continue;

            // is this a line number label?
            Matcher m = p.matcher(lbl.id_);
            if( m.matches() ) {
                int posPC = lbl.getLocation();
                int lineNum = Integer.parseInt(m.group(1));

                if( lnt == null ) {
                    // create LNT if it doesn't exist yet
                    if( attrs.isEmpty() ) {
                        attrList_.add(new LineNumberTable(cp_));
                        attrs = attrList_.getAll(cp_,
                                Attribute.ATTR_LINE_NUMBER_TABLE);
                    }
                    lnt = (LineNumberTable) attrs.get(0);
                }

                // add line number reference
                lnt.add(posPC, lineNum);
            }
        }

        return code;
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


    public int getMaxLocals() {
        return maxLocals_;
    }


    public int getMaxStack() {
        return maxStack_;
    }


    /**
     * Reset this code block to completely empty. Note that any constants
     * associated with the previous code will not be removed from the constant
     * pool.
     */
    public void reset() {
        output_.reset();
        handler_.clear();
        history_.clear();
        maxLocals_ = -1;
        maxStack_ = -1;
        output_.reset();
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
        output_.reset();
        maxStack_ = maxStack;
        maxLocals_ = maxLocals;
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
        assert method_ == null : "Code owner already set";
        assert classBuilder != null : "Code class must not be null";
        assert method != null : "Code owner must not be null";
        output_.setClass(classBuilder);
        method_ = method;

        // prior to the method being set nothing was compiled
        List<ClassData> pend = new ArrayList<ClassData>(history_);
        history_.clear();
        for(ClassData cd:pend) {
            if( cd.containsKey("source") ) compile(cd.get(String.class,
                    "source"), cd.get(ClassData.class, "replacements"));
            else
                appendCode(IO.decode(cd.get(String.class, "bytes")));
        }
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
        if( maxLocals_ != -1 )
            cd.put("maxLocals", Integer.valueOf(maxLocals_));
        if( maxStack_ != -1 ) cd.put("maxStack", Integer.valueOf(maxStack_));
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
        byte code[] = getCodeInternal();

        // first get the attribute size
        ByteArrayOutputStream attrs = new ByteArrayOutputStream();
        attrList_.writeTo(attrs);
        byte[] attrBytes = attrs.toByteArray();

        IO.writeU2(baos, attrId_.getIndex());
        IO.writeS4(baos, 10 + code.length + handler_.size() * 8
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
        IO.writeS4(baos, code.length);
        baos.write(code, 0, code.length);

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
