package yabel.parser;

import yabel.ClassData;
import yabel.OpCodes;
import yabel.SwitchData;
import yabel.code.Code;
import yabel.code.Handler;
import yabel.constants.*;
import yabel.io.IO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        /** Location of the start of this op-code */
        int location_;

        /** The op-code itself */
        byte code_;

        /** The decompiled op-code */
        String opCode_;
    }

    /**
     * Buffer used to build the output.
     */
    private StringBuilder buf_ = new StringBuilder();

    /** The ConstantPool referenced by the code */
    private final ConstantPool cp_;

    /**
     * The decompiled byte-code
     */
    private final List<Decompiler.OpCode> decomp_ = new ArrayList<Decompiler.OpCode>();

    /**
     * Configuration data associated with the decompiled code
     */
    private final ClassData cd_ = new ClassData();

    /**
     * Exception handlers associated with the code
     */
    private final List<ClassData> handlers_ = new ArrayList<ClassData>();

    /** Labels generated for the decompiled code */
    private final Set<Integer> labels_ = new HashSet<Integer>();

    /** The de-compilation result */
    private final ClassData result_ = new ClassData();


    /**
     * New decompiler for a given constant pool
     * 
     * @param cp
     *            the ConstantPool
     */
    public Decompiler(ConstantPool cp) {
        cp_ = cp;
        result_.put("data", cd_);
        result_.putList(ClassData.class, "handlers", handlers_);
    }


    /**
     * Inform this decompiler of an exception handler associated with the code
     * block it is working on
     * 
     * @param h
     *            the exception handler
     */
    public void addHandler(Handler h) {
        ClassData cd = new ClassData();
        Integer i = Integer.valueOf(h.getStartPC());
        cd.put("start", String.format("lbl%04x", i));
        labels_.add(i);
        i = Integer.valueOf(h.getEndPC());
        cd.put("end", String.format("lbl%04x", i));
        labels_.add(i);
        i = Integer.valueOf(h.getHandlerPC());
        cd.put("handler", String.format("lbl%04x", i));
        labels_.add(i);

        // the type may be null for a catch-everything handler
        ConstantClass catchType = h.getCatchType();
        if( catchType == null ) {
            cd.put("type", null);
        } else {
            cd.put("type", catchType.getClassName().get());
        }

        // save to class data
        handlers_.add(cd);
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
        String cNm = String.format("class%04x", Integer.valueOf(v));
        if( cd_.containsKey(cNm) ) return cNm;
        ConstantClass conCls = cp_.validate(v, ConstantClass.class);
        String n = conCls.getClassName().get();
        cd_.put(cNm, Code.escapeJava(n));
        return cNm;
    }


    private void decompile2(int position, byte[] buffer, int length,
            Decompiler.OpCode opc) {
        switch (buffer[0]) {
        case OpCodes.BIPUSH:
            buf_.append("ICONST:").append(buffer[1]);
            opc.opCode_ = buf_.toString();
            return;
        case OpCodes.LDC:
            // Constants are quite complicated
            int c = 0xff & buffer[1];
            opc.opCode_ = decompileLDC(c);
            return;
        case OpCodes.NEWARRAY:
            buf_.append("NEWARRAY:").append(OpCodes.getArrayType(buffer[1]));
            opc.opCode_ = buf_.toString();
            return;
        default:
            buf_.append(OpCodes.getOpName(buffer[0])).append(':').append(
                    0xff & buffer[1]);
            opc.opCode_ = buf_.toString();
            return;
        }
    }


    private String decompileLDC(int c) {
        String nm = String.format("con%04x", Integer.valueOf(c));
        Constant con = cp_.get(c);
        if( con instanceof ConstantNumber ) {
            // Constant is a number type, so can use a numeric lookup
            Number n = ((ConstantNumber) con).getValue();
            cd_.put(nm, n);
        } else if( con instanceof ConstantString ) {
            // strings are simple
            String v = ((ConstantString) con).getValue().get();
            cd_.put(nm, Code.escapeJava(v));
        } else if( con instanceof ConstantClass ) {
            // classes must have an explicit type
            String v = ((ConstantClass) con).getClassName().get();
            cd_.put(nm, Code.escapeJava(v));
            return "LDC:class:{" + nm + "}";
        } else {
            // other types not handled
            throw new IllegalArgumentException(
                    "Constant for LDC is not a string nor a number: " + con);
        }
        return "LDC:{" + nm + "}";
    }


    private void decompile3(int position, byte[] buffer, int length,
            Decompiler.OpCode opc) {
        int op = 0xff & buffer[0];
        int v = IO.readU2(buffer, 1);
        switch (buffer[0]) {
        // handle special cases
        case OpCodes.IINC:
            buf_.append("IINC:").append(IO.readU1(buffer, 1)).append(':').append(
                    buffer[2]);
            opc.opCode_ = buf_.toString();
            return;
        case OpCodes.LDC_W:
            // falls through
        case OpCodes.LDC2_W:
            opc.opCode_ = decompileLDC(v);
            return;
        case OpCodes.SIPUSH:
            buf_.append("ICONST:").append((short) v);
            opc.opCode_ = buf_.toString();
            return;

            // handle method invocations
        case OpCodes.INVOKESPECIAL:
            // falls through
        case OpCodes.INVOKESTATIC:
            // falls through
        case OpCodes.INVOKEVIRTUAL: {
            String mNm = refToData("method", v);
            buf_.append(OpCodes.getOpName(op)).append(":{").append(mNm).append(
                    '}');
            opc.opCode_ = buf_.toString();
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

            buf_.append(OpCodes.getOpName(op)).append(":{").append(cNm).append(
                    '}');
            opc.opCode_ = buf_.toString();
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
            String mNm = refToData("field", v);
            buf_.append(OpCodes.getOpName(op)).append(":{").append(mNm).append(
                    '}');
            opc.opCode_ = buf_.toString();
            return;
        }

            // handle branch instructions
        default: {
            int addr = position + IO.readS2(buffer, 1);
            Integer iaddr = Integer.valueOf(addr);
            String lNm = String.format("lbl%04x", iaddr);
            labels_.add(iaddr);
            buf_.append(OpCodes.getOpName(op)).append(" #:").append(lNm);
            opc.opCode_ = buf_.toString();
            return;
        }
        }
    }


    private void decompile4(int position, byte[] buffer, int length,
            Decompiler.OpCode opc) {
        // must be MULTINEWARRAY or WIDE
        switch (buffer[0]) {
        case OpCodes.MULTIANEWARRAY: {
            int v = IO.readU2(buffer, 1);
            buf_.append("MULTIANEWARRAY:{").append(classToData(v)).append("}:").append(
                    IO.readU1(buffer, 3));
            opc.opCode_ = buf_.toString();
            return;
        }

        case OpCodes.WIDE: {
            int op = IO.readU1(buffer, 1);
            int v = IO.readU2(buffer, 2);
            buf_.append(OpCodes.getOpName(op)).append(':').append(v);
            opc.opCode_ = buf_.toString();
            return;
        }
        default:
            throw new IllegalArgumentException("opCode is " + buffer[0]
                    + ", length is " + length);
        }
    }


    private void decompile5(int position, byte[] buffer, int length,
            Decompiler.OpCode opc) {
        // JSR_W GOTO_W INVOKEINTERFACE
        switch (buffer[0]) {
        case OpCodes.JSR_W:
            // falls through
        case OpCodes.GOTO_W: {
            int v = IO.readS4(buffer, 1);
            int addr = position + v;
            Integer iaddr = Integer.valueOf(addr);
            String lNm = String.format("lbl%04x", iaddr);
            labels_.add(iaddr);
            buf_.append((buffer[0] == OpCodes.JSR_W) ? "JSR" : "GOTO").append(
                    " #:").append(lNm);
            opc.opCode_ = buf_.toString();
            return;
        }
        case OpCodes.INVOKEINTERFACE: {
            int v = IO.readU2(buffer, 1);
            String iNm = refToData("iface", v);
            buf_.append("INVOKEINTERFACE:{").append(iNm).append('}');
            opc.opCode_ = buf_.toString();
            return;
        }
        default:
            throw new IllegalArgumentException("opCode is " + buffer[0]
                    + ", length is " + length);
        }
    }


    private void decompile6(int position, byte[] buffer, int length,
            Decompiler.OpCode opc) {
        if( (buffer[0] == OpCodes.WIDE) && (buffer[1] == OpCodes.IINC) ) {
            int v1 = IO.readU2(buffer, 2);
            int v2 = IO.readS2(buffer, 4);
            buf_.append("IINC:").append(v1).append(':').append(v2);
            opc.opCode_ = buf_.toString();
            return;
        }

        throw new IllegalArgumentException("opCode is " + buffer[0]
                + ", length is " + length);
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
        Integer idflt = Integer.valueOf(dflt);
        String l = String.format("lbl%04x", idflt);
        labels_.add(idflt);
        SwitchData sw = new SwitchData(l);

        p += 4;
        int pairs = IO.readS4(buffer, p);
        while( pairs > 0 ) {
            p += 4;
            int match = IO.readS4(buffer, p);
            p += 4;
            int offset = position + IO.readS4(buffer, p);

            // get label and mapping
            Integer ioffset = Integer.valueOf(offset);
            l = String.format("lbl%04x", ioffset);
            labels_.add(ioffset);
            sw.add(Integer.valueOf(match), l);

            pairs--;
        }

        String sl = String.format("switch%04x", Integer.valueOf(position));
        cd_.put(sl, sw);
        buf_.append("LOOKUPSWITCH:").append(sl);
        opc.opCode_ = buf_.toString();
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
        Integer idflt = Integer.valueOf(dflt);
        String l = String.format("lbl%04x", idflt);
        labels_.add(idflt);
        SwitchData sw = new SwitchData(l);

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
            Integer ioffset = Integer.valueOf(offset);
            l = String.format("lbl%04x", ioffset);
            labels_.add(ioffset);
            sw.add(Integer.valueOf(i), l);
        }

        String sl = String.format("switch%04x", Integer.valueOf(position));
        cd_.put(sl, sw);
        buf_.append("TABLESWITCH:").append(sl);
        opc.opCode_ = buf_.toString();
    }


    /**
     * Finish de-compilation and get the result
     * 
     * @return the decompiled code and associated data
     */
    public ClassData finish() {
        StringBuilder buf = new StringBuilder();
        // minimal pretty printing: labels are out-dented and there is a
        // blank line after a branch
        for(Decompiler.OpCode opc:decomp_) {
            Integer p = Integer.valueOf(opc.location_);
            if( labels_.contains(p) ) {
                buf.append("@:").append(String.format("lbl%04x", p)).append(
                        '\n');
            }
            buf.append("    ").append(opc.opCode_).append('\n');
            Byte b = Byte.valueOf(opc.code_);
            if( Parser.OP_EXIT.contains(b) || Parser.BRANCH_OPS.contains(b) )
                buf.append('\n');
        }

        String src = buf.toString();
        result_.put("source", src);
        cd_.sort();
        result_.sort();
        return result_;
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
        if( length == 1 ) {
            opc.opCode_ = OpCodes.getOpName(buffer[0]);
            return;
        }

        buf_.setLength(0);

        if( buffer[0] == OpCodes.TABLESWITCH ) {
            decompileTableSwitch(position, buffer, length, opc);
            return;
        }

        if( buffer[0] == OpCodes.LOOKUPSWITCH ) {
            decompileLookupSwitch(position, buffer, length, opc);
            return;
        }

        switch (length) {
        case 2:
            decompile2(position, buffer, length, opc);
            return;
        case 3:
            decompile3(position, buffer, length, opc);
            return;
        case 4:
            decompile4(position, buffer, length, opc);
            return;
        case 5:
            decompile5(position, buffer, length, opc);
            return;
        case 6:
            decompile6(position, buffer, length, opc);
            return;
        }

        throw new IllegalArgumentException("opCode is " + buffer[0]
                + ", length is " + length);
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
        cd_.clear();
        buf_.setLength(0);
        for(int i = 0;i < code.length;i++) {
            parser.parse(code[i]);
        }
    }


    private String refToData(String prefix, int v) {
        String pNm = String.format(prefix + "%04x", Integer.valueOf(v));

        if( cd_.containsKey(pNm) ) return pNm;

        ConstantRef con = cp_.validate(v, ConstantRef.class);

        List<String> sa = new ArrayList<String>(3);
        sa.add(Code.escapeJava(con.getClassName().get()));
        sa.add(Code.escapeJava(con.getName().get()));
        sa.add(Code.escapeJava(con.getType().get()));

        cd_.putList(String.class, pNm, sa);

        return pNm;
    }

}