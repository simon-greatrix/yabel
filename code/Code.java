package yabel.code;

import yabel.io.IO;

import yabel.attributes.Attribute;
import yabel.attributes.AttributeList;

import yabel.*;
import yabel.constants.*;
import yabel.parser.Decompiler;
import yabel.parser.ParserAnalyzer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

/**
 * A Code attribute for a method. No support for LineNumberTable nor
 * LocalVariableTable attributes.
 * 
 * @author Simon Greatrix
 */
public class Code extends Attribute {

    /** ICONST instructions in order */
    static final byte[] ICONST_VALS = new byte[] { OpCodes.ICONST_M1,
            OpCodes.ICONST_0, OpCodes.ICONST_1, OpCodes.ICONST_2,
            OpCodes.ICONST_3, OpCodes.ICONST_4, OpCodes.ICONST_5 };

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
            } else if( (ch < 0x20)
                    || ((ch > 0x7E) && (ch < 0xFF))
                    || ((ch == ' ') || (ch == '{') || (ch == '}') || (ch == ':')) ) {
                // handle special chars and normal octal escapes
                buf.append('\\');
                if( ch >= 0100 )
                    buf.append(Character.forDigit((ch >> 6) & 0x7, 8));
                if( ch >= 0010 )
                    buf.append(Character.forDigit((ch >> 3) & 0x7, 8));
                buf.append("01234567".charAt((ch) & 0x7));
            } else if( ch >= 0xFF ) {
                // handle 16-bit escapes
                buf.append("\\u");
                buf.append(Character.forDigit((ch >> 12) & 0xf, 16));
                buf.append(Character.forDigit((ch >> 8) & 0xf, 16));
                buf.append(Character.forDigit((ch >> 4) & 0xf, 16));
                buf.append(Character.forDigit(ch & 0xf, 16));
            } else {
                // normal char
                buf.append(ch);
            }
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
                throw new IllegalArgumentException("Invalid octal escape in "
                        + val);
            }

            // must have char now
            buf.append((char) v);
            return i + 3;
        }

        // handle 16-bit escapes
        if( ch == 'u' ) {
            // must have 4 digits
            if( (i + 4) >= val.length() ) {
                throw new IllegalArgumentException(
                        "Invalid \\u escape (too few characters) in " + val);
            }

            // get and parse hexadecimal number
            String v = val.substring(i + 1, i + 5).toLowerCase();
            int v2 = 0;
            for(int j = 0;j < 4;j++) {
                ch = v.charAt(j);
                int v3 = Character.digit(ch, 16);
                if( v3 == -1 ) {
                    throw new IllegalArgumentException("Invalid \\u escape ("
                            + ch + " is not a hex digit) in " + val);
                }
                v2 = v2 * 16 + v3;
            }

            // got character
            buf.append((char) v2);
            return i + 5;
        }

        // unrecognised escape
        throw new IllegalArgumentException("Unhandled escape sequence in "
                + val);
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

    /**
     * Labels in compiled code. First element of list is label target, rest are
     * label references.
     */
    private Map<String, Label> labels_ = new HashMap<String, Label>();

    /** The local variables required */
    private int maxLocals_ = 0;

    /** The stack slots required */
    private int maxStack_ = 0;

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
            handler_.add(new Handler(input));
        }

        // read attributes
        attrList_ = new AttributeList(cp_, input);
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
        int catchId = 0;
        if( catchType != null ) {
            ConstantClass cc = new ConstantClass(cp_, catchType);
            catchId = cc.getIndex();
        }
        Handler h = new Handler(startPC, endPC, handlerPC, catchId);
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
        int catchId = 0;
        if( catchType != null ) {
            ConstantClass cc = new ConstantClass(cp_, catchType);
            catchId = cc.getIndex();
        }
        Handler h = new Handler(startPC, endPC, handlerPC, catchId);
        handler_.add(h);
    }


    /**
     * Write an ALOAD instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void aload(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.ALOAD_0);
            break;
        case 1:
            appendU1(OpCodes.ALOAD_1);
            break;
        case 2:
            appendU1(OpCodes.ALOAD_2);
            break;
        case 3:
            appendU1(OpCodes.ALOAD_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.ALOAD);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.ALOAD);
                appendU2(i);
            }
        }
    }


    /**
     * Append a single byte to the code currently being compiled.
     * 
     * @param b
     *            the byte to append
     */
    public void appendU1(byte b) {
        if( code_ != null )
            throw new IllegalStateException(
                    "setByteCode(int,int,byte[]) already called");
        pass1_.write(b);
    }


    /**
     * Append an U2 value to the code currently being compiled.
     * 
     * @param i
     *            the value to append
     */
    public void appendU2(int i) {
        if( code_ != null )
            throw new IllegalStateException(
                    "setByteCode(int,int,byte[]) already called");
        IO.writeU2(pass1_, i);
    }


    /**
     * Write an ASTORE instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void astore(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.ASTORE_0);
            break;
        case 1:
            appendU1(OpCodes.ASTORE_1);
            break;
        case 2:
            appendU1(OpCodes.ASTORE_2);
            break;
        case 3:
            appendU1(OpCodes.ASTORE_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.ASTORE);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.ASTORE);
                appendU2(i);
            }
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
     * <td>Generates an appropriate store or wide store instruction. The ClassData
     * should contain an Integer for "property".</td>
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
     * INVOKESTATIC instruction. The ClassData's "property" should contain either
     * a two element StringArray specifying name and type of a method in this
     * class, or a three element String Array specifying class, name and type of
     * the method, or an Integer giving the index of the method definition in
     * the constant pool. If an explicit value is given, it is the constant pool
     * index.</td>
     * </tr>
     * <tr>
     * <td>U1:property<br>
     * U1:123</td>
     * <td>Outputs a single byte, either the specified integer in the ClassData or
     * the explicit value</td>
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
        if( code_ != null )
            throw new IllegalStateException(
                    "setByteCode(int,int,byte[]) already called");
        Iterator<List<String>> iter = new CodeTokenizer(raw, cd);
        while( iter.hasNext() ) {
            List<String> toks = iter.next();

            // If just one String, probably an op-code
            int size = toks.size();
            if( size == 2 ) {
                String tu = toks.get(1).toUpperCase();
                Byte c = OpCodes.OP_CODES.get(tu);
                if( c == null ) {
                    throw new IllegalArgumentException("Unrecognized opcode:"
                            + toks.get(0));
                }
                pass1_.write(c.intValue());
                continue;
            }

            // compile standard productions
            String lbl = toks.get(1).toUpperCase();
            if( compileClass(lbl, toks, cd) ) continue;
            if( compileConstant(lbl, toks, cd) ) continue;
            if( compileField(lbl, toks, cd) ) continue;
            if( compileInterface(lbl, toks, cd) ) continue;
            if( compileLabel(lbl, toks) ) continue;
            if( compileLDC(lbl, toks, cd) ) continue;
            if( compileLoad(lbl, toks, cd) ) continue;
            if( compileMethod(lbl, toks, cd) ) continue;
            if( compileNewArray(lbl, toks, cd) ) continue;
            if( compileStore(lbl, toks, cd) ) continue;
            if( compileSwitch(lbl, toks, cd) ) continue;
            if( compileU1U2(lbl, toks, cd) ) continue;

            throw new IllegalArgumentException("Unrecognized token:"
                    + toks.get(0));
        }
    }


    private boolean compileClass(String lbl, List<String> toks, ClassData cd) {
        boolean ok = false;
        if( lbl.equals("ANEWARRAY") ) {
            pass1_.write(OpCodes.ANEWARRAY);
            ok = true;
        }
        if( lbl.equals("CHECKCAST") ) {
            pass1_.write(OpCodes.CHECKCAST);
            ok = true;
        }
        if( lbl.equals("INSTANCEOF") ) {
            pass1_.write(OpCodes.INSTANCEOF);
            ok = true;
        }
        if( lbl.equals("NEW") ) {
            pass1_.write(OpCodes.NEW);
            ok = true;
        }
        if( lbl.equals("CLASS") ) {
            // CLASS does not have an op-code
            ok = true;
        }

        if( !ok ) return false;

        if( toks.size() != 3 )
            throw new IllegalArgumentException("\"" + lbl
                    + "\" operation accepts a single parameter.");
        String ref = toks.get(2);
        ref = cd.get(String.class, ref, ref);
        int val = getClassRef(ref);
        IO.writeU2(pass1_, val);
        return true;
    }


    private boolean compileConstant(String lbl, List<String> toks, ClassData cd) {
        int cs = -1;
        if( lbl.equals("ICONST") ) {
            cs = 0;
        } else if( lbl.equals("NUMBER") ) {
            cs = 1;
        } else if( lbl.equals("CONST") ) {
            cs = 2;
        } else if( lbl.equals("STRING") ) {
            cs = 3;
        } else {
            return false;
        }

        // Verify we have the right number of parameters. For most the input
        // should have been RAW OP-CODE <value> - so size is 3
        if( (cs != 2) && (toks.size() != 3) )
            throw new IllegalArgumentException("\"" + lbl
                    + "\" operation accepts a single parameter. Op-code was:\n"
                    + toks.get(0));

        // we have a constant, so handle it appropriately
        switch (cs) {
        case 0: {
            // compile integer constant
            int i = getInt(cd, toks.get(2), toks.get(0));
            iconst(i);
            return true;
        }
        case 1: {
            // compile numeric constant
            Number n = cd.get(Number.class, toks.get(2));
            int val = getConstantRef(n);
            IO.writeU2(pass1_, val);
            return true;
        }
        case 2: {
            // compile type-value constant
            if( toks.size() != 4 )
                throw new IllegalArgumentException(
                        "\""
                                + lbl
                                + "\" operation accepts either a type and value pair. Op-code was:\n"
                                + toks.get(0));
            String v = toks.get(3);
            v = cd.get(String.class, v, v);
            int val = getConstantRef(toks.get(0), toks.get(2), v);
            IO.writeU2(pass1_, val);
            return true;
        }
        case 3: {
            // compile String
            String k = toks.get(2);
            String s = cd.get(String.class, k);
            if( s == null ) s = k;
            int val = getConstantRef(s);
            IO.writeU2(pass1_, val);
            return true;
        }
        }

        return false;
    }


    private boolean compileField(String lbl, List<String> toks, ClassData cd) {
        boolean ok = false;

        // generic field reference
        if( lbl.equals("FIELD") ) {
            ok = true;
        }

        // putfield instruction
        if( lbl.equals("PUTFIELD") ) {
            pass1_.write(OpCodes.PUTFIELD);
            ok = true;
        }

        // getfield instruction
        if( lbl.equals("GETFIELD") ) {
            pass1_.write(OpCodes.GETFIELD);
            ok = true;
        }

        // putstatic instruction
        if( lbl.equals("PUTSTATIC") ) {
            pass1_.write(OpCodes.PUTSTATIC);
            ok = true;
        }

        // getstatic instruction
        if( lbl.equals("GETSTATIC") ) {
            pass1_.write(OpCodes.GETSTATIC);
            ok = true;
        }

        // if got a field ref, output it
        if( !ok ) return false;

        // is it valid? Local field will just be field name. Remote field
        // will be class, name, type.
        int size = toks.size();
        switch (size) {
        case 3: {
            String k = toks.get(2);
            String ref = cd.get(String.class, k);
            if( ref != null ) {
                int val = getFieldRef(ref);
                IO.writeU2(pass1_, val);
                return true;
            }

            // wasn't a local field, try for remote field
            List<String> saRefs = cd.getList(String.class, k);
            if( saRefs != null ) {
                // local field as remote
                if( saRefs.size() == 1 ) {
                    int val = getFieldRef(saRefs.get(0));
                    IO.writeU2(pass1_, val);
                    return true;
                }

                // remote field
                if( saRefs.size() == 3 ) {
                    int val = getFieldRef(saRefs.get(0), saRefs.get(1),
                            saRefs.get(2));
                    IO.writeU2(pass1_, val);
                    return true;
                }

                // unrecognized
                throw new IllegalArgumentException("Invalid field reference "
                        + toks.get(0) + ":\n" + cd);
            }

            // try for bare name
            int val = getFieldRef(k);
            IO.writeU2(pass1_, val);
            return true;
        }
        case 5: {
            int val = getFieldRef(toks.get(2), toks.get(3), toks.get(4));
            IO.writeU2(pass1_, val);
            return true;
        }
        default:
            throw new IllegalArgumentException("\"" + lbl
                    + "\" operation accepts 1 or 3 parameters.");
        }
    }


    private boolean compileInterface(String lbl, List<String> toks, ClassData cd) {
        boolean ok = false;
        if( lbl.equals("INVOKEINTERFACE") ) {
            pass1_.write(OpCodes.INVOKEINTERFACE);
            ok = true;
        }
        if( lbl.equals("INTERFACEMETHOD") ) {
            ok = true;
        }

        // did we get a match?
        if( !ok ) return false;

        // ensure we have three elements for the reference
        int size = toks.size();
        String[] pa = null;
        if( size == 3 ) {
            List<String> refs = cd.getList(String.class, toks.get(2));
            if( refs != null ) pa = refs.toArray(new String[3]);
        } else if( size == 5 ) {
            pa = new String[] { toks.get(2), toks.get(3), toks.get(4) };
        }
        if( pa == null || (pa.length != 3) ) {
            throw new IllegalArgumentException(
                    "Invalid interface method reference: " + toks.get(0));
        }

        // got the method details, write out reference
        int val = getInterfaceMethodRef(pa[0], pa[1], pa[2]);
        IO.writeU2(pass1_, val);

        // INVOKEINTERFACE requires an extra parameter and a zero
        if( lbl.equals("INVOKEINTERFACE") ) {
            int nargs = 1 + ClassBuilder.getArgsForType(pa[2]);
            pass1_.write(nargs);
            pass1_.write(0);
        }
        return true;
    }


    private void compileJump(String name, int width) {
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


    private boolean compileLabel(String lbl, List<String> toks) {
        if( !(lbl.equals("#") || lbl.equals("#4") || lbl.equals("@")) )
            return false;

        if( toks.size() != 3 ) {
            throw new IllegalArgumentException(
                    "Labels accept only a single argument");
        }

        String p = toks.get(2);

        if( lbl.equals("#") ) {
            compileJump(p, 2);
            return true;
        }

        if( lbl.equals("#4") ) {
            compileJump(p, 4);
            return true;
        }

        // set label location
        Label label = getLabel(p);
        if( label.location_ != -1 ) {
            throw new IllegalArgumentException("Label " + p
                    + " defined more than once");
        }
        label.location_ = pass1_.size();
        return true;
    }


    private boolean compileLDC(String lbl, List<String> toks, ClassData cd) {
        if( !lbl.equals("LDC") ) return false;

        int i = -1;
        switch (toks.size()) {
        case 3: // raw, LDC, property
            Object o = cd.get(toks.get(2));
            if( o != null ) {
                if( o instanceof Number ) {
                    // it is a numeric constant
                    Number n = (Number) o;
                    i = getConstantRef(n);
                } else if( o instanceof String ) {
                    // it is a String constant
                    String s = (String) o;
                    i = getConstantRef(s);
                } else if( o instanceof Class<?> ) {
                    // undocumented, but constants can be classes
                    String nm = ((Class<?>) o).getName();
                    nm = nm.replace('.', '/');
                    i = getClassRef(nm);
                }
            }
            break;
        case 4: // raw, LDC, type, value
            String v = toks.get(3);
            v = cd.get(String.class, v, v);
            i = getConstantRef(toks.get(0), toks.get(2), v);
            break;
        default: // wrong
            throw new IllegalArgumentException(
                    "LDC accepts either a lookup property or a type and value pair. Input was:"
                            + toks.get(0));
        }

        // try for a bare int
        if( i == -1 ) i = getInt(cd, toks.get(2), toks.get(0));
        ldc(i);

        return true;
    }


    private boolean compileLoad(String l, List<String> toks, ClassData cd) {
        // object
        if( l.equals("ALOAD") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + l
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            aload(i);
            return true;
        }

        // double
        if( l.equals("DLOAD") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + l
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            dload(i);
            return true;
        }

        // float
        if( l.equals("FLOAD") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + l
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            fload(i);
            return true;
        }

        // int
        if( l.equals("ILOAD") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + l
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            iload(i);
            return true;
        }

        // long
        if( l.equals("LLOAD") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + l
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            lload(i);
            return true;
        }

        return false;
    }


    private boolean compileMethod(String l, List<String> toks, ClassData cd) {
        boolean ok = false;
        if( l.equals("METHOD") ) {
            ok = true;
        }
        if( l.equals("INVOKESPECIAL") ) {
            pass1_.write(OpCodes.INVOKESPECIAL);
            ok = true;
        }
        if( l.equals("INVOKEVIRTUAL") ) {
            pass1_.write(OpCodes.INVOKEVIRTUAL);
            ok = true;
        }
        if( l.equals("INVOKESTATIC") ) {
            pass1_.write(OpCodes.INVOKESTATIC);
            ok = true;
        }

        // if we had a match, identify and output method reference
        if( !ok ) return false;

        // 1 argument - look up in configuration
        // 2 arguments - local method name and type
        // 3 arguments - class method and type

        switch (toks.size()) {
        case 3: {
            String p = toks.get(2);
            List<String> refs = cd.getList(String.class, p);
            if( refs == null ) {
                String v = cd.get(String.class, p);
                if( v != null ) {
                    String[] ps = v.split(":");
                    if( (ps.length == 2) || (ps.length == 3) ) {
                        refs = new ArrayList<String>(ps.length);
                        for(String s:ps)
                            refs.add(s);
                    } else {
                        throw new IllegalArgumentException(
                                "Invalid method reference " + toks.get(0)
                                        + ". Referencing \"" + v + "\".");
                    }
                }
            }
            if( refs != null ) {
                // size 2 is in this class
                if( refs.size() == 2 ) {
                    int val = getMethodRef(refs.get(0), refs.get(1));
                    IO.writeU2(pass1_, val);
                    return true;
                }
                // size 3 is in an explicit class
                if( refs.size() == 3 ) {
                    int val = getMethodRef(refs.get(0), refs.get(1),
                            refs.get(2));
                    IO.writeU2(pass1_, val);
                    return true;
                }

                // any other size is bad
                throw new IllegalArgumentException("Invalid method reference "
                        + toks.get(0));
            }

            // try for a reference id in the ClassData
            int i = getInt(cd, p, toks.get(0));
            IO.writeU2(pass1_, i);

            return true;
        }
        case 4: {
            int val = getMethodRef(toks.get(2), toks.get(3));
            IO.writeU2(pass1_, val);
            return true;
        }
        case 5: {
            int val = getMethodRef(toks.get(2), toks.get(3), toks.get(4));
            IO.writeU2(pass1_, val);
            return true;
        }
        default:
            throw new IllegalArgumentException("Invalid method reference "
                    + toks.get(0));
        }
    }


    private boolean compileNewArray(String l, List<String> toks, ClassData cd) {
        if( !l.equals("NEWARRAY") ) return false;
        if( toks.size() > 3 )
            throw new IllegalArgumentException("Operation \"" + l
                    + "\" accepts a single argument, not " + (toks.size() - 2)
                    + ". Input was:" + toks.get(0));
        pass1_.write(OpCodes.NEWARRAY);
        String p = toks.get(2);
        String q = cd.get(String.class, p, p);
        int i = OpCodes.getArrayType(q);
        if( i != -1 ) {
            pass1_.write(i);
            return true;
        }

        i = getInt(cd, p, toks.get(0));
        pass1_.write(i);
        return true;
    }


    private boolean compileStore(String lbl, List<String> toks, ClassData cd) {
        // object
        if( lbl.equals("ASTORE") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            astore(i);
            return true;
        }

        // double
        if( lbl.equals("DSTORE") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            dstore(i);
            return true;
        }

        // float
        if( lbl.equals("FSTORE") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            fstore(i);
            return true;
        }

        // int
        if( lbl.equals("ISTORE") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            istore(i);
            return true;
        }

        // long
        if( lbl.equals("LSTORE") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            lstore(i);
            return true;
        }

        // iinc
        if( lbl.equals("IINC") ) {
            int iincIndex, iincConst;
            int size = toks.size();
            if( size > 4 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts one or two arguments, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            if( size == 3 ) {
                List<Integer> ia = cd.getList(Integer.class, toks.get(2));
                if( (ia == null) || (ia.size() != 2) ) {
                    // unrecognized
                    throw new IllegalArgumentException(
                            "Invalid IINC reference " + toks.get(0) + ":\n"
                                    + cd);
                }
                iincIndex = ia.get(0).intValue();
                iincConst = ia.get(1).intValue();
            } else {
                // size is 4, so have type and value
                String raw = toks.get(0);
                iincIndex = getInt(cd, toks.get(2), raw);
                iincConst = getInt(cd, toks.get(3), raw);
            }

            // output IINC or WIDE IINC as needed
            if( (iincIndex < 0x100) && (iincConst < 0x100) ) {
                appendU1(OpCodes.IINC);
                appendU1((byte) iincIndex);
                appendU1((byte) iincConst);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.IINC);
                appendU2(iincIndex);
                appendU2(iincConst);
            }
            return true;
        }

        // ret
        if( lbl.equals("RET") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));

            if( i < 0x100 ) {
                appendU1(OpCodes.RET);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.RET);
                appendU2(i);
            }
            return true;
        }

        return false;
    }


    private boolean compileSwitch(String lbl, List<String> toks, ClassData cd) {
        if( !(lbl.equals("SWITCH") || lbl.equals("LOOKUPSWITCH") || lbl.equals("TABLESWITCH")) )
            return false;

        SwitchData vals;
        if( toks.size() == 3 ) {
            // named SwitchData
            String p = toks.get(toks.size() - 1);
            vals = cd.get(SwitchData.class, p);
            if( vals == null ) {
                throw new IllegalArgumentException(
                        "Property "
                                + p
                                + " does not give a list of value to label mappings in:\n"
                                + cd);
            }
        } else {
            // list of values and labels
            // raw switch default val1 lbl1 val2 lbl2 ...
            // so size must be an odd number
            if( (toks.size() % 2) == 0 ) {
                throw new IllegalArgumentException(
                        "Switch statement should be " + lbl
                                + " <default> [<value> <label>], not "
                                + toks.get(0));
            }

            Iterator<String> iter = toks.iterator();
            // skip raw
            iter.hasNext();
            iter.next();

            // skip switch
            iter.hasNext();
            iter.next();

            // get default
            iter.hasNext();
            vals = new SwitchData(iter.next());

            // read pairs
            while( iter.hasNext() ) {
                String value = iter.next();
                iter.hasNext();
                String label = iter.next();

                Integer i = Integer.valueOf(value);
                vals.add(i, label);
            }
        }

        if( lbl.equals("SWITCH") ) {
            // Choose the smallest kind of switch.
            // Lookup has a value-label pair for each value
            int lookupSize = 8 * vals.size();
            // Table has min, max and labels
            int tableSize = 4 * (1 + vals.getMax() - vals.getMin()) + 8;
            lbl = (lookupSize < tableSize) ? "LOOKUPSWITCH" : "TABLESWITCH";
        }

        // how many to skip?
        // 0 1 2 3 4
        // switch pad pad pad data
        // switch pad pad data
        // switch pad data
        // switch data
        int s = 3 - (pass1_.size() % 4);

        if( lbl.equals("TABLESWITCH") ) {
            pass1_.write(OpCodes.TABLESWITCH);
            while( s > 0 ) {
                pass1_.write(0);
                s--;
            }

            // default, min, max, all labels
            String dflt = vals.getDefault();
            compileJump(dflt, 4);

            int min = vals.getMin();
            int max = vals.getMax();
            IO.writeS4(pass1_, min);
            IO.writeS4(pass1_, max);
            for(int i = min;i <= max;i++) {
                Integer ii = Integer.valueOf(i);
                String slbl = vals.get(ii);
                if( slbl == null ) {
                    // no lookup means default
                    compileJump(dflt, 4);
                } else {
                    compileJump(slbl, 4);
                }
            }
            return true;
        }

        if( lbl.equals("LOOKUPSWITCH") ) {
            pass1_.write(OpCodes.LOOKUPSWITCH);
            while( s > 0 ) {
                pass1_.write(0);
                s--;
            }
            // default, value, label, value, label, ...
            String dflt = vals.getDefault();
            compileJump(dflt, 4);
            IO.writeS4(pass1_, vals.size());
            for(Entry<Integer, String> e:vals) {
                IO.writeS4(pass1_, e.getKey().intValue());
                compileJump(e.getValue(), 4);
            }
            return true;
        }

        return false;
    }


    private boolean compileU1U2(String lbl, List<String> toks, ClassData cd) {
        // compile explicit U1
        if( lbl.equals("U1") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            pass1_.write(i);
            return true;
        }

        // compile explicit U2
        if( lbl.equals("U2") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            IO.writeU2(pass1_, i);
            return true;
        }

        // compile explicit S4
        if( lbl.equals("S4") ) {
            if( toks.size() > 3 )
                throw new IllegalArgumentException("Operation \"" + lbl
                        + "\" accepts a single argument, not "
                        + (toks.size() - 2) + ". Input was:" + toks.get(0));
            int i = getInt(cd, toks.get(2), toks.get(0));
            IO.writeS4(pass1_, i);
            return true;
        }

        return false;
    }


    /**
     * Decompile the code block.
     * 
     * @return representation of the decompiled code.
     */
    public ClassData decompile() {
        if( code_ == null )
            throw new IllegalStateException(
                    "setByteCode(int,int,byte[]) has not been called");

        Decompiler decomp = new Decompiler(cp_);
        decomp.parse(code_);
        for(Handler h:handler_) {
            decomp.addHandler(h);
        }

        ClassData cd = decomp.finish();
        cd.sort();
        return cd;
    }


    /**
     * Write a DLOAD instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void dload(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.DLOAD_0);
            break;
        case 1:
            appendU1(OpCodes.DLOAD_1);
            break;
        case 2:
            appendU1(OpCodes.DLOAD_2);
            break;
        case 3:
            appendU1(OpCodes.DLOAD_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.DLOAD);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.DLOAD);
                appendU2(i);
            }
        }
    }


    /**
     * Write a DSTORE instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void dstore(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.DSTORE_0);
            break;
        case 1:
            appendU1(OpCodes.DSTORE_1);
            break;
        case 2:
            appendU1(OpCodes.DSTORE_2);
            break;
        case 3:
            appendU1(OpCodes.DSTORE_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.DSTORE);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.DSTORE);
                appendU2(i);
            }
        }
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
                    if( jump < Short.MIN_VALUE || jump > Short.MAX_VALUE ) {
                        throw new IllegalArgumentException(
                                "Cannot jump distance of " + jump);
                    }
                    code_[here] = (byte) ((jump >> 8) & 0xff);
                    code_[here + 1] = (byte) (jump & 0xff);
                } else {
                    code_[here] = (byte) ((jump >> 24) & 0xff);
                    code_[here + 1] = (byte) ((jump >> 16) & 0xff);
                    code_[here + 2] = (byte) ((jump >> 8) & 0xff);
                    code_[here + 3] = (byte) (jump & 0xff);
                }
            }
        }
    }


    /**
     * Write an FLOAD instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void fload(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.FLOAD_0);
            break;
        case 1:
            appendU1(OpCodes.FLOAD_1);
            break;
        case 2:
            appendU1(OpCodes.FLOAD_2);
            break;
        case 3:
            appendU1(OpCodes.FLOAD_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.FLOAD);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.FLOAD);
                appendU2(i);
            }
        }
    }


    /**
     * Write a FSTORE instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void fstore(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.FSTORE_0);
            break;
        case 1:
            appendU1(OpCodes.FSTORE_1);
            break;
        case 2:
            appendU1(OpCodes.FSTORE_2);
            break;
        case 3:
            appendU1(OpCodes.FSTORE_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.FSTORE);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.FSTORE);
                appendU2(i);
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
                throw new IllegalArgumentException("Integer constant value \""
                        + value + "\" is not an integer in:\n" + raw);
            }
            i = getConstantRef(n);
        } else if( type.equalsIgnoreCase("double") ) {
            // cast to Double
            try {
                n = Double.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Double constant value \""
                        + value + "\" is not a double in:\n" + raw);
            }
            i = getConstantRef(n);
        } else if( type.equalsIgnoreCase("float") ) {
            // cast to Float
            try {
                n = Float.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Float constant value \""
                        + value + "\" is not a float in:\n" + raw);
            }
            i = getConstantRef(n);
        } else if( type.equalsIgnoreCase("long") ) {
            // cast to Long
            try {
                n = Long.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Long constant value \""
                        + value + "\" is not a long in:\n" + raw);
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
            throw new IllegalArgumentException(
                    "Constant data type \""
                            + type
                            + "\" was not recognised. Use int, double, float, long or string:\n"
                            + raw);
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
            throw new IllegalArgumentException("No such field: " + name);

        ConstantFieldRef ref = new ConstantFieldRef(cp_, class_.getClassID(),
                f.getNameID(), f.getTypeID());
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
    private int getInt(ClassData cd, String fld, String raw) {
        Integer i = cd.get(Integer.class, fld);
        if( i != null ) return i.intValue();
        try {
            return Integer.parseInt(fld);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Unable to get integer from \""
                    + raw + "\". ClassData was:\n" + cd);
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
            throw new IllegalArgumentException("Label " + lbl
                    + " is not defined");
        int loc = li.location_;
        if( loc == -1 )
            throw new IllegalArgumentException("Label " + lbl
                    + " is not located");
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
        int cn = cp_.getUtf8(name);
        int ct = cp_.getUtf8(type);
        ConstantMethodRef ref = new ConstantMethodRef(cp_, class_.getClassID(),
                cn, ct);
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
     * Generate ICONST instruction. May be broadened to BIPUSH, SIPUSH or LDC.
     * 
     * @param val
     *            constant required
     */
    protected void iconst(int val) {
        int ic = val + 1;
        if( 0 <= ic && ic < ICONST_VALS.length ) {
            appendU1(ICONST_VALS[ic]);
            return;
        }

        if( (Byte.MIN_VALUE <= val) && (val <= Byte.MAX_VALUE) ) {
            appendU1(OpCodes.BIPUSH);
            appendU1((byte) val);
            return;
        }

        if( (Short.MIN_VALUE <= val) && (val <= Short.MAX_VALUE) ) {
            appendU1(OpCodes.SIPUSH);
            appendU2(val);
            return;
        }

        ldc(getConstantRef(Integer.valueOf(val)));
    }


    /**
     * Write an ILOAD instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void iload(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.ILOAD_0);
            break;
        case 1:
            appendU1(OpCodes.ILOAD_1);
            break;
        case 2:
            appendU1(OpCodes.ILOAD_2);
            break;
        case 3:
            appendU1(OpCodes.ILOAD_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.ILOAD);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.ILOAD);
                appendU2(i);
            }
        }
    }


    /**
     * Write an ISTORE instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void istore(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.ISTORE_0);
            break;
        case 1:
            appendU1(OpCodes.ISTORE_1);
            break;
        case 2:
            appendU1(OpCodes.ISTORE_2);
            break;
        case 3:
            appendU1(OpCodes.ISTORE_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.ISTORE);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.ISTORE);
                appendU2(i);
            }
        }
    }


    /**
     * Write an LDC or LDC_W instruction
     * 
     * @param i
     *            constant index
     */
    protected void ldc(int i) {
        Constant con = cp_.get(i);
        if( con instanceof ConstantNumber ) {
            // longs and doubles must use LDC2_W
            Number n = ((ConstantNumber) con).getValue();
            if( (n instanceof Double) || (n instanceof Long) ) {
                appendU1(OpCodes.LDC2_W);
                appendU2(i);
                return;
            }
        }

        if( i < 256 ) {
            appendU1(OpCodes.LDC);
            appendU1((byte) i);
        } else {
            appendU1(OpCodes.LDC_W);
            appendU2(i);
        }
    }


    /**
     * Write an LLOAD instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void lload(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.LLOAD_0);
            break;
        case 1:
            appendU1(OpCodes.LLOAD_1);
            break;
        case 2:
            appendU1(OpCodes.LLOAD_2);
            break;
        case 3:
            appendU1(OpCodes.LLOAD_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.LLOAD);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.LLOAD);
                appendU2(i);
            }
        }
    }


    /**
     * Write an LSTORE instruction.
     * 
     * @param i
     *            local variable index
     */
    protected void lstore(int i) {
        switch (i) {
        case 0:
            appendU1(OpCodes.LSTORE_0);
            break;
        case 1:
            appendU1(OpCodes.LSTORE_1);
            break;
        case 2:
            appendU1(OpCodes.LSTORE_2);
            break;
        case 3:
            appendU1(OpCodes.LSTORE_3);
            break;
        default:
            if( i < 0x100 ) {
                appendU1(OpCodes.LSTORE);
                appendU1((byte) i);
            } else {
                appendU1(OpCodes.WIDE);
                appendU1(OpCodes.LSTORE);
                appendU2(i);
            }
        }
    }


    /**
     * Reset this code block to completely empty. Note that any constants
     * associated with the previous code wil not be removed from the constant
     * pool.
     */
    public void reset() {
        code_ = null;
        handler_.clear();
        labels_.clear();
        maxLocals_ = -1;
        maxStack_ = -1;
        pass1_.reset();
    }


    /**
     * Set the byte code for this Code.
     * 
     * @param maxStack
     *            the number of stack slots
     * @param maxLocals
     *            the number of local variables
     * @param code
     *            the byte code
     */
    public void setByteCode(int maxStack, int maxLocals, byte[] code) {
        if( pass1_.size() > 0 )
            throw new IllegalStateException(
                    "compile(String,ClassData) already called");
        maxStack_ = maxStack;
        maxLocals_ = maxLocals;
        code_ = new byte[code.length];
        System.arraycopy(code, 0, code_, 0, code.length);
        handler_.clear();
        labels_.clear();
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

        IO.writeU2(baos, attrId_);
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
