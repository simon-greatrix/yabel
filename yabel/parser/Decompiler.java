package yabel.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import yabel.ClassData;
import yabel.OpCodes;
import yabel.SwitchData;
import yabel.attributes.Attribute;
import yabel.attributes.LineNumberTable;
import yabel.attributes.LineNumberTable.LNV;
import yabel.code.Code;
import yabel.code.Handler;
import yabel.constants.Constant;
import yabel.constants.ConstantClass;
import yabel.constants.ConstantNumber;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantRef;
import yabel.constants.ConstantString;
import yabel.io.IO;
import yabel.parser.decomp.Label;
import yabel.parser.decomp.LabelList;
import yabel.parser.decomp.LabelSwitch;
import yabel.parser.decomp.LabeledHandler;
import yabel.parser.decomp.Multi;
import yabel.parser.decomp.Ops;
import yabel.parser.decomp.Simple;
import yabel.parser.decomp.Source;
import yabel.parser.decomp.VarDef;
import yabel.parser.decomp.VarRef;
import yabel.parser.decomp.VariableSet;
import yabel.parser.decomp.Label.Ref4;

/**
 * Decompile byte code and produce a compilable output that can be used to
 * reconstruct the input perfectly.
 * 
 * @author Simon Greatrix
 * 
 */
public class Decompiler implements ParserListener {
    /**
     * A decompiled set of byte-codes as an op-code
     * 
     * @author Simon Greatrix
     * 
     */
    static class OpCode {
        /** The op-code itself */
        byte code_;

        /** Location of the start of this op-code */
        int location_;

        /** The decompiled op-code */
        Source opCode_;
    }



    /**
     * Options for de-compilation. This controls how much data is in the raw
     * source and how much is parameterised into the ClassData.
     * 
     * @author Simon Greatrix
     */
    public static class Options {
        /** Include comments indicating location? */
        public boolean locationComments = true;
        
        /** Are classes explicit or in the class data? */
        public boolean classInData = false;

        /** Are constants explicit or in the class data? */
        public boolean constInData = false;

        /** Are fields explicit or in the class data? */
        public boolean fieldInData = false;

        /** Are interfaces explicit or in the class data? */
        public boolean ifaceInData = false;

        /** Are methods explicit or in the class data? */
        public boolean methodInData = false;

        /** Are switch statements explicit or in the class data? */
        public boolean switchInData = false;
    }

    /**
     * Map of op codes and operands to source
     */
    private static Map<Integer, Source> SOURCE = Collections.synchronizedMap(new HashMap<Integer, Source>());

    /**
     * Configuration data associated with the decompiled code
     */
    private final ClassData replacements_ = new ClassData();

    /**
     * Class data containing source and replacements
     */
    private final ClassData build_ = new ClassData();

    /** The ConstantPool referenced by the code */
    private final ConstantPool cp_;

    /**
     * The decompiled byte-code
     */
    private final List<Decompiler.OpCode> decomp_ = new ArrayList<Decompiler.OpCode>();

    /** Exception handlers associated with the code */
    private final List<LabeledHandler> handlers_ = new ArrayList<LabeledHandler>();

    /** Labels generated for the decompiled code */
    private final LabelList labels_ = new LabelList();

    /** Options used during the de-compilation */
    private Options options_ = new Options();

    /** The de-compilation result */
    private final ClassData result_ = new ClassData();

    /** Switches in the code */
    private final Map<String, LabelSwitch> switches_ = new HashMap<String, LabelSwitch>();

    /** Set of variables for the code block */
    private final VariableSet varSet_ = new VariableSet();

    /** Line numbers in this code block */
    private final LineNumberTable lineNumbers_;

    /**
     * New decompiler for a given constant pool
     * 
     * @param cp
     *            the ConstantPool
     */
    public Decompiler(ConstantPool cp) {
        cp_ = cp;
        lineNumbers_ = new LineNumberTable(cp);
        List<ClassData> buildList = new ArrayList<ClassData>(1);
        buildList.add(build_);
        build_.put("replacements",replacements_);
        result_.put("name",Attribute.ATTR_CODE);
        result_.putList(ClassData.class,"build",buildList);
        result_.putList(ClassData.class, "handlers", new ArrayList<ClassData>());
        result_.putList(ClassData.class, "attributes", new ArrayList<ClassData>(2));
    }


    /**
     * Inform this decompiler of an exception handler associated with the code
     * block it is working on
     * 
     * @param h
     *            the exception handler
     */
    public void addHandler(Handler h) {
        handlers_.add(new LabeledHandler(labels_, h));
    }


    /**
     * Add a table of line numbers to the code
     * 
     * @param table
     *            the line numbers
     */
    public void addLineNumbers(LineNumberTable table) {
        for(LNV lnv:lineNumbers_) {
            Label l = labels_.getLabel(lnv.getStartPC());
            if( l!=null ) l.removeName(lnv.toString());
        }
        
        for(LNV lnv:table) {
            lineNumbers_.add(lnv.getStartPC(),lnv.getLine());
        }
        
        for(LNV lnv:lineNumbers_) {
            Label l = labels_.createLabel(lnv.getStartPC());
            l.setDefaultName(lnv.toString());
        }
    }


    /**
     * Store the details of a class in the configuration data and return its
     * name.
     * 
     * @param v
     *            the class reference id
     * @return the reference in the configuration data
     */
    private String classToData(int v) {
        if( options_.classInData ) {
            String cNm = String.format("class%04x", Integer.valueOf(v));
            if( replacements_.containsKey(cNm) ) return cNm;
            ConstantClass conCls = cp_.validate(v, ConstantClass.class);
            String n = conCls.getClassName().get();
            replacements_.put(cNm, Code.escapeJava(n));
            return "{" + cNm + "}";
        }

        ConstantClass conCls = cp_.validate(v, ConstantClass.class);
        String n = conCls.getClassName().get();
        return n;
    }


    private void decompile1(byte[] buffer, Decompiler.OpCode opc, int position) {
        byte b = buffer[0];
        Source src = varSet_.decomp(b, position);
        if( src != null ) {
            opc.opCode_ = src;
            return;
        }

        Integer v = Integer.valueOf(0xff & b);
        src = SOURCE.get(v);
        if( src == null ) {
            src = new Simple(b);
            SOURCE.put(v, src);
        }
        opc.opCode_ = src;
        return;
    }


    private void decompile2(byte[] buffer, Decompiler.OpCode opc, int position) {
        Integer v = Integer.valueOf((buffer[0] & 0xff) << 8
                | (buffer[1] & 0xff));
        Source src = SOURCE.get(v);
        if( src != null ) {
            opc.opCode_ = src;
            return;
        }

        switch (buffer[0]) {
        case OpCodes.BIPUSH:
            src = new Simple("ICONST:" + buffer[1]);
            SOURCE.put(v, src);
            opc.opCode_ = src;
            return;
        case OpCodes.LDC:
            // Constants are quite complicated
            int c = 0xff & buffer[1];
            opc.opCode_ = decompileLDC(c);
            return;
        case OpCodes.NEWARRAY:
            src = new Simple("NEWARRAY:" + OpCodes.getArrayType(buffer[1]));
            SOURCE.put(v, src);
            opc.opCode_ = src;
            return;
        default:
            // Must be load, store or ret
            c = 0xff & buffer[1];
            opc.opCode_ = new Multi(OpCodes.getOpName(buffer[0]),
                    varSet_.getRef(c, position));
            return;
        }
    }


    private void decompile3(byte[] buffer, Decompiler.OpCode opc, int position) {
        int op = 0xff & buffer[0];
        int v = IO.readU2(buffer, 1);
        switch (buffer[0]) {
        // handle special cases
        case OpCodes.IINC:
            int var = IO.readU1(buffer, 1);
            opc.opCode_ = new Multi("IINC", varSet_.getRef(var, position),
                    Integer.valueOf(buffer[2]));
            return;
        case OpCodes.LDC_W:
            // falls through
        case OpCodes.LDC2_W:
            opc.opCode_ = decompileLDC(v);
            return;
        case OpCodes.SIPUSH:
            opc.opCode_ = new Simple("ICONST:" + ((short) v));
            return;

            // handle method invocations
        case OpCodes.INVOKESPECIAL:
            // falls through
        case OpCodes.INVOKESTATIC:
            // falls through
        case OpCodes.INVOKEVIRTUAL: {
            String mNm = refToData("method", v);
            opc.opCode_ = new Multi(OpCodes.getOpName(op), mNm);
            return;
        }

            // handle class stuff
        case OpCodes.ANEWARRAY:
            // falls through
        case OpCodes.CHECKCAST:
            // falls through
        case OpCodes.INSTANCEOF:
            // falls through
        case OpCodes.NEW: {
            String cNm = classToData(v);
            opc.opCode_ = new Multi(OpCodes.getOpName(op), cNm);
            return;
        }

            // handle field stuff
        case OpCodes.GETFIELD:
            // falls through
        case OpCodes.PUTFIELD:
            // falls through
        case OpCodes.GETSTATIC:
            // falls through
        case OpCodes.PUTSTATIC: {
            String fNm = refToData("field", v);
            opc.opCode_ = new Multi(OpCodes.getOpName(op), fNm);
            return;
        }

            // handle branch instructions
        default: {
            int addr = position + IO.readS2(buffer, 1);
            Label.Ref ref = labels_.getRef(addr);
            opc.opCode_ = new Ops(OpCodes.getOpName(op), ref);
            return;
        }
        }
    }


    private void decompile4(byte[] buffer, Decompiler.OpCode opc, int position) {
        // must be MULTINEWARRAY or WIDE
        switch (buffer[0]) {
        case OpCodes.MULTIANEWARRAY: {
            int v = IO.readU2(buffer, 1);
            opc.opCode_ = new Multi("MULTIANEWARRAY", classToData(v),
                    Integer.toString(IO.readU1(buffer, 3)));
            return;
        }

        case OpCodes.WIDE: {
            int op = IO.readU1(buffer, 1);
            int v = IO.readU2(buffer, 2);
            VarRef ref = varSet_.getRef(v, position);
            opc.opCode_ = new Multi(OpCodes.getOpName(op), ref);
            return;
        }
        default:
            throw new AssertionError("opCode is " + buffer[0] + ", length is 4");
        }
    }


    private void decompile5(byte[] buffer, Decompiler.OpCode opc, int position) {
        // JSR_W GOTO_W INVOKEINTERFACE
        switch (buffer[0]) {
        case OpCodes.JSR_W:
            // falls through
        case OpCodes.GOTO_W: {
            int v = IO.readS4(buffer, 1);
            int addr = position + v;
            Ref4 ref = labels_.getRef4(addr);
            opc.opCode_ = new Ops(OpCodes.getOpName(buffer[0]), ref);
            return;
        }
        case OpCodes.INVOKEINTERFACE: {
            int v = IO.readU2(buffer, 1);
            String iNm = refToData("iface", v);
            opc.opCode_ = new Multi("INVOKEINTERFACE", iNm);
            return;
        }
        default:
            throw new AssertionError("opCode is " + buffer[0] + ", length is 5");
        }
    }


    private void decompile6(byte[] buffer, Decompiler.OpCode opc, int position) {
        if( (buffer[0] == OpCodes.WIDE) && (buffer[1] == OpCodes.IINC) ) {
            int v1 = IO.readU2(buffer, 2);
            int v2 = IO.readS2(buffer, 4);
            opc.opCode_ = new Multi("IINC", varSet_.getRef(v1, position),
                    Integer.valueOf(v2));
            return;
        }

        throw new AssertionError("opCode is " + buffer[0] + ", length is 6");
    }


    private Source decompileLDC(int c) {
        return options_.constInData ? decompileLDCConst(c)
                : decompileLDCExplicit(c);
    }


    private Source decompileLDCConst(int c) {
        String nm = String.format("con%04x", Integer.valueOf(c));
        Source ret = new Multi("LDC", "{" + nm + "}");
        Constant con = cp_.get(c);
        if( con instanceof ConstantNumber ) {
            // Constant is a number type, so can use a numeric lookup
            Number n = ((ConstantNumber) con).getValue();
            replacements_.put(nm, n);
        } else if( con instanceof ConstantString ) {
            // strings are simple
            String v = ((ConstantString) con).getValue().get();
            replacements_.put(nm, Code.escapeJava(v));
        } else if( con instanceof ConstantClass ) {
            // classes must have an explicit type
            String v = ((ConstantClass) con).getClassName().get();
            replacements_.put(nm, Code.escapeJava(v));
            ret = new Multi("LDC", "class", "{" + nm + "}");
        } else {
            // other types not handled
            throw new YabelDecompileException(
                    "Constant for LDC is neither a string nor a number: " + con);
        }
        return ret;
    }


    private Source decompileLDCExplicit(int c) {
        Constant con = cp_.get(c);
        if( con instanceof ConstantNumber ) {
            // Constant is a number type, so can use a numeric lookup
            ConstantNumber n = (ConstantNumber) con;
            return new Multi("LDC", n.getType(), String.valueOf(n.getValue()));
        } else if( con instanceof ConstantString ) {
            // strings are simple
            String v = ((ConstantString) con).getValue().get();
            return new Multi("LDC", "string", Code.escapeJava(v));
        } else if( con instanceof ConstantClass ) {
            // classes must have an explicit type
            String v = ((ConstantClass) con).getClassName().get();
            return new Multi("LDC", "class", Code.escapeJava(v));
        }

        // other types not handled
        throw new YabelDecompileException(
                "Constant for LDC is neither a string nor a number: " + con);
    }


    private void decompileLookupSwitch(int position, byte[] buffer, int length,
            Decompiler.OpCode opc) {
        // format is:
        // LOOKUPSWITCH
        // <padding>
        // default
        // n-pairs
        // match - offset
        // match - offset ...
        int p = 4 - (position % 4);
        int dflt = position + IO.readS4(buffer, p);
        Ref4 dfltRef = labels_.getRef4(dflt);
        LabelSwitch sw = new LabelSwitch("LOOKUPSWITCH", dfltRef);

        p += 4;
        int pairs = IO.readS4(buffer, p);
        while( pairs > 0 ) {
            p += 4;
            int match = IO.readS4(buffer, p);
            p += 4;
            int offset = position + IO.readS4(buffer, p);

            // get label and mapping
            Ref4 l = labels_.getRef4(offset);
            sw.add(Integer.valueOf(match), l);

            pairs--;
        }

        if( options_.switchInData ) {
            String sl = String.format("switch%04x", Integer.valueOf(position));
            switches_.put(sl, sw);
            opc.opCode_ = new Multi("LOOKUPSWITCH", sl);
        } else {
            opc.opCode_ = sw;
        }
    }


    private void decompileTableSwitch(int position, byte[] buffer, int length,
            Decompiler.OpCode opc) {
        // format is:
        // TABLESWITCH
        // <padding>
        // default
        // low
        // high
        // offsets...
        int p = 4 - (position % 4);
        int dflt = position + IO.readS4(buffer, p);
        Ref4 lblDflt = labels_.getRef4(dflt);
        LabelSwitch sw = new LabelSwitch("TABLESWITCH", lblDflt);

        int low = IO.readS4(buffer, p + 4);
        int high = IO.readS4(buffer, p + 8);
        p = p + 8;
        for(int i = low;i <= high;i++) {
            p += 4;
            int offset = position + IO.readS4(buffer, p);

            // we forcibly retain pointless table entries for low and
            // high in order to ensure the recompiled code is identical
            // to the original
            if( (offset == dflt) && (i != low) && (i != high) ) continue;
            sw.add(Integer.valueOf(i), labels_.getRef4(offset));
        }

        if( options_.switchInData ) {
            String sl = String.format("switch%04x", Integer.valueOf(position));
            switches_.put(sl, sw);
            opc.opCode_ = new Multi("TABLESWITCH", sl);
        } else {
            opc.opCode_ = sw;
        }
    }


    /**
     * Finish de-compilation and get the result
     * 
     * @return the decompiled code and associated data
     */
    public ClassData finish() {
        // we can finalise the label now so we can copy the switch data.
        for(Entry<String, LabelSwitch> e:switches_.entrySet()) {
            SwitchData sw = e.getValue().getData();
            replacements_.put(e.getKey(), sw);
        }

        StringBuilder buf = new StringBuilder();
        // Minimal pretty printing: labels are out-dented and there is a
        // blank line after a branch.
        String prefix1, prefix2;
        if( options_.locationComments ) {
            prefix1 = "            ";
        } else {
            prefix1 = "";
        }
        
        for(Decompiler.OpCode opc:decomp_) {
            
            Label lbl = labels_.getLabel(opc.location_);
            if( lbl != null ) {
                buf.append(prefix1).append(lbl.source()).append('\n');
            }
            List<VarDef> defs = varSet_.getDefs(opc.location_);
            if( !defs.isEmpty() ) {
                for(VarDef v:defs) {
                    buf.append(prefix1).append("    ").append(v.source());
                }
            }
            String opSrc = opc.opCode_.source();
            if( options_.locationComments ) {
                buf.append(String.format("/* %5d */ ",Integer.valueOf(opc.location_)));
                opSrc=opSrc.replaceAll("\n","\n        "+prefix1);
            }
            buf.append("    ").append(opSrc).append('\n');
            Byte b = Byte.valueOf(opc.code_);
            if( Parser.OP_EXIT.contains(b) || Parser.BRANCH_OPS.contains(b) )
                buf.append('\n');
        }

        // decompile complete
        String src = buf.toString();
        build_.put("source", src);

        // add the exception handlers
        List<ClassData> handlers = result_.getList(ClassData.class, "handlers");
        handlers.clear();
        for(LabeledHandler lh:handlers_) {
            handlers.add(lh.toClassData());
        }

        // attributes
        List<ClassData> attrs = result_.getList(ClassData.class,"attributes");
        Iterator<ClassData> iter = attrs.iterator();
        while( iter.hasNext() ) {
            ClassData cd = iter.next();
            if( Attribute.ATTR_LINE_NUMBER_TABLE.equals(cd.get(String.class,"name")) ) {
                iter.remove();
            }
        }
        if(! lineNumbers_.isEmpty() ) attrs.add(lineNumbers_.toClassData());
        
        replacements_.sort();
        return result_;
    }


    public Options getOptions() {
        return options_;
    }


    /**
     * Accept notification that a set of byte-codes have been recognised as a
     * complete op-code and can be added to the de-compilation.
     * 
     * @param position
     *            where in the code we are
     * @param buffer
     *            the op-code's bytes
     * @param length
     *            the length of the op-code
     */
    public void opCodeFinish(int position, byte[] buffer, int length) {
        Decompiler.OpCode opc = new OpCode();
        decomp_.add(opc);
        opc.code_ = buffer[0];
        opc.location_ = position;

        // switches are variable length
        if( buffer[0] == OpCodes.TABLESWITCH ) {
            decompileTableSwitch(position, buffer, length, opc);
            return;
        }

        if( buffer[0] == OpCodes.LOOKUPSWITCH ) {
            decompileLookupSwitch(position, buffer, length, opc);
            return;
        }

        // it's not a switch so the length tells us what it is
        switch (length) {
        case 1:
            decompile1(buffer, opc, position);
            return;
        case 2:
            decompile2(buffer, opc, position);
            return;
        case 3:
            decompile3(buffer, opc, position);
            return;
        case 4:
            decompile4(buffer, opc, position);
            return;
        case 5:
            decompile5(buffer, opc, position);
            return;
        case 6:
            decompile6(buffer, opc, position);
            return;
        }

        throw new AssertionError("opCode is " + buffer[0] + ", length is "
                + length);
    }


    /**
     * Parse some byte-codes and decompile them
     * 
     * @param code
     *            the byte code to decompile
     */
    public void parse(byte[] code) {
        Parser parser = new Parser(this);
        decomp_.clear();
        replacements_.clear();
        for(int i = 0;i < code.length;i++) {
            parser.parse(code[i]);
        }
    }


    private String refToData(String prefix, int v) {
        // check options
        boolean inData = true;
        int required = 3;
        if( "field".equals(prefix) ) {
            inData = options_.fieldInData;
            required = 1;
        } else if( "method".equals(prefix) ) {
            inData = options_.methodInData;
            required = 2;
        } else if( "iface".equals(prefix) ) {
            inData = options_.ifaceInData;
        }

        // if inData may already have processed
        String pNm = null;
        if( inData ) {
            pNm = String.format(prefix + "%04x", Integer.valueOf(v));
            if( replacements_.containsKey(pNm) ) return "{" + pNm + "}";
        }

        // get the class, name and type of the referent
        ConstantRef con = cp_.validate(v, ConstantRef.class);
        String clsName = con.getClassName().get();
        String cls = Code.escapeJava(clsName);
        String name = Code.escapeJava(con.getName().get());
        String type = Code.escapeJava(con.getType().get());

        if( inData ) {
            // build list and put in data
            List<String> sa = new ArrayList<String>(3);
            sa.add(cls);
            sa.add(name);
            sa.add(type);
            replacements_.putList(String.class, pNm, sa);
        } else {
            // return explicit specification if not declared in this class
            StringBuilder buf = new StringBuilder();
            if( !clsName.equals(cp_.getOwner().getName()) ) {
                required = 3;
            } else {
                // in this class, but if it is inherited give full specification
                if( "field".equals(prefix) ) {
                    if( cp_.getOwner().getDeclaredField(name) == null )
                        required = 3;
                } else if( "method".equals(prefix) ) {
                    if( cp_.getOwner().getDeclaredMethod(name, type) == null )
                        required = 3;
                }
            }
            String sep=(required>2)?"\n:":":";
            if( required == 3 ) buf.append(cls).append(sep);
            buf.append(name);
            if( required >= 2 ) buf.append(sep).append(type);
            pNm = buf.toString();
        }

        return pNm;
    }


    public void setOptions(Options options) {
        options_ = options;
    }
}
