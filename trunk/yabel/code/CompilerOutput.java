package yabel.code;

import java.util.HashMap;
import java.util.Map;

import yabel.ClassBuilder;
import yabel.ClassData;
import yabel.Field;
import yabel.OpCodes;
import yabel.constants.Constant;
import yabel.constants.ConstantClass;
import yabel.constants.ConstantFieldRef;
import yabel.constants.ConstantInterfaceMethodRef;
import yabel.constants.ConstantMethodRef;
import yabel.constants.ConstantNumber;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantString;
import yabel.io.IO;

/**
 * Intermediate output of the compilation system. Tracks labels and provides
 * references for the code being generated.
 * 
 * @author Simon Greatrix
 */
public class CompilerOutput {

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
    public static int getInt(ClassData cd, String fld, String raw) {
        String r = CodeTokenizer.isReplacement(fld);
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

    /** The class this code is part of */
    private ClassBuilder class_ = null;

    /** The byte code */
    private byte[] code_ = null;

    /** The constant pool for the code block being created */
    private ConstantPool cp_;

    /**
     * Labels in compiled code. First element of list is label target, rest are
     * label references.
     */
    private Map<String, Label> labels_ = new HashMap<String, Label>();

    /** Compiler output (label jump locations unset) */
    private CompileStream output_ = new CompileStream();

    /** Flag indicating if the last op was a WIDE */
    private boolean lastWasWide_ = false;


    /**
     * Create compiler output
     * 
     * @param cp
     *            the constant pool for this compilation
     */
    public CompilerOutput(ConstantPool cp) {
        cp_ = cp;
    }


    /**
     * Append a specific code block to the output
     * 
     * @param code
     *            the code block to append
     */
    public void appendCode(byte[] code) {
        output_.write(code);
        lastWasWide_ = false;
    }


    /**
     * Append a signed 4 byte value to the code currently being compiled.
     * 
     * @param v
     *            the value to append
     */
    public void appendS4(int v) {
        IO.writeS4(output_, v);
        lastWasWide_ = false;
    }


    /**
     * Append the required number of zeros for switch padding. This assumes the
     * switch op-code has just been written.
     */
    public void appendSwitchPadding() {
        int s = 3 - ((output_.size() - 1) % 4);
        while( s > 0 ) {
            output_.write(0);
            s--;
        }
        lastWasWide_ = false;
    }


    /**
     * Append a WIDE instruction, unless the previous instruction was a WIDE
     * added by this method.
     */
    public void appendWide() {
        if( !lastWasWide_ ) output_.write(OpCodes.WIDE);
        lastWasWide_ = true;
    }


    /**
     * Was the last operand a WIDE?
     * 
     * @return true if last was wide
     */
    public boolean wasLastWide() {
        return lastWasWide_;
    }


    /**
     * Append a single byte to the code currently being compiled.
     * 
     * @param b
     *            the byte to append
     */
    public void appendU1(byte b) {
        output_.write(b);
        lastWasWide_ = false;
    }


    /**
     * Append an U2 value to the code currently being compiled.
     * 
     * @param i
     *            the value to append
     */
    public void appendU2(int i) {
        IO.writeU2(output_, i);
        lastWasWide_ = false;
    }


    /**
     * Add a jump to a named location.
     * 
     * @param name
     *            the location's name
     * @param width
     *            the width of the jump (2 or 4 bytes)
     */
    public void compileJump(String name, int width) {
        // get label's list
        Label label = labels_.get(name);
        if( label == null ) {
            // create new label
            label = new Label(name);
            labels_.put(name, label);
        }

        LabelUse use = new LabelUse();
        use.location_ = output_.size();
        use.opLoc_ = output_.getLastOpPosition();
        use.width_ = width;
        label.usage_.add(use);
        for(int i = 0;i < width;i++) {
            output_.write(0);
        }
        lastWasWide_ = false;
    }


    /**
     * Finalize the code being created. Finalization involves populating all the
     * jump destinations.
     * 
     * @return the finalized code.
     */
    public byte[] finalizeCode() {
        if( code_ != null ) return code_;

        code_ = output_.toByteArray();

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

        return code_;
    }


    /**
     * Get a class reference
     * 
     * @param name
     *            name of class
     * @return reference
     */
    public int getClassRef(String name) {
        ConstantClass c = new ConstantClass(cp_, name);
        return c.getIndex();
    }


    /**
     * Get the Constant for a given reference.
     * 
     * @param ref
     *            the reference
     * @return the Constant
     */
    public Constant getConstant(int ref) {
        return cp_.get(ref);
    }


    /**
     * Get a constant reference
     * 
     * @param number
     *            value
     * @return reference
     */
    public int getConstantRef(Number number) {
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
    public int getConstantRef(String string) {
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
    public int getConstantRef(String raw, String type, String value) {
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
    public int getFieldRef(String name) {
        Field f = class_.getField(name);
        if( f == null )
            throw new IllegalStateException("Field \"" + name
                    + "\" is not yet defined in this class");

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
    public int getFieldRef(String clss, String name, String type) {
        ConstantFieldRef ref = new ConstantFieldRef(cp_, clss, name, type);
        return ref.getIndex();
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
    public int getInterfaceMethodRef(String clss, String name, String type) {
        ConstantInterfaceMethodRef c = new ConstantInterfaceMethodRef(cp_,
                clss, name, type);
        return c.getIndex();
    }


    /**
     * Get the label associated with a given name
     * 
     * @param name
     *            the label's name
     * @return the Label
     */
    public Label getLabel(String name) {
        Label label = labels_.get(name);
        if( label == null ) {
            // create new label
            label = new Label(name);
            labels_.put(name, label);
        }
        return label;
    }


    /**
     * Get a label's location by its name
     * 
     * @param lbl
     *            the label's name
     * @return the label's location
     */
    public int getLabelLocation(String lbl) {
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


    /**
     * Get a method reference in this class.
     * 
     * @param name
     *            method name
     * @param type
     *            method type
     * @return reference
     */
    public int getMethodRef(String name, String type) {
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
    public int getMethodRef(String clss, String name, String type) {
        ConstantMethodRef ref = new ConstantMethodRef(cp_, clss, name, type);
        return ref.getIndex();
    }


    /**
     * Reset the output to being compilation from the beginning.
     */
    public void reset() {
        code_ = null;
        output_.reset();
        labels_.clear();
    }


    /**
     * Restart code generation after a <code>finalizeCode</code> invocation.
     */
    public void restart() {
        code_ = null;
    }


    /**
     * Set the builder associated with this compilation
     * 
     * @param builder
     *            the class builder
     */
    public void setClass(ClassBuilder builder) {
        class_ = builder;
        cp_ = builder.getConstantPool();
    }


    /**
     * Define a new label at the current position.
     * 
     * @param name
     *            the label's name
     */
    public void setLabel(String name) {
        Label label = getLabel(name);
        if( label.location_ != -1 ) {
            throw new YabelLabelException("Label \"" + name
                    + "\" defined more than once");
        }
        label.location_ = output_.size();
    }

    
    public void setVariable(int index, String name) {
        // TODO
    }
}
