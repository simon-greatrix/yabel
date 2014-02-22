package yabel.code;

import java.util.*;

import yabel.ClassBuilder;
import yabel.Field;
import yabel.OpCodes;
import yabel.code.operand.CodeVar.Var;
import yabel.constants.*;
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
     * @param fld
     *            the field to look up
     * @param raw
     *            the raw text for the op-code
     * @return the integer
     */
    public static int getInt(String fld, String raw) {
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
     * Labels in compiled code.
     */
    private Map<String, Label> labels_ = new HashMap<String, Label>();

    /** Compiler output (label jump locations unset) */
    private CompileStream output_ = new CompileStream();

    /** Flag indicating if the last op was a WIDE */
    private boolean lastWasWide_ = false;

    /** Map of variable name to variable declaration */
    private final Map<String, Var> name2var_ = new HashMap<String, Var>();

    /** Map of variable slot to variable declaration */
    private final Map<Integer, Var> index2var_ = new HashMap<Integer, Var>();


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
     * Append a WIDE instruction, unless the previous instruction was a WIDE
     * added by this method.
     */
    public void appendWide() {
        if( !lastWasWide_ ) output_.write(OpCodes.WIDE);
        lastWasWide_ = true;
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


    private String currentVars() {
        Var[] vars = index2var_.values().toArray(new Var[0]);
        Arrays.sort(vars, new Comparator<Var>() {
            @Override
            public int compare(Var o1, Var o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });

        StringBuilder buf = new StringBuilder();
        for(Var v:vars) {
            buf.append(", ").append(v.getIndex()).append('=').append(
                    v.getName());
        }
        return buf.toString().substring(2);
    }


    /**
     * Define a variable
     * 
     * @param index
     *            the slot used by the variable or -1
     * @param name
     *            the variable's name
     * @param type
     *            the variable's type
     */
    public void defineVariable(int index, String name, String type) {
        Integer index1, index2;
        int isDouble = Var.isDouble(type) ? 1 : 0;
        if( index != -1 ) {
            // explicit index
            index1 = Integer.valueOf(index);
            index2 = Integer.valueOf(index + isDouble);

            Var v = index2var_.get(index1);
            if( v != null )
                throw new YabelBadVariableException("Cannot define variable \""
                        + name + "\" of type " + type + " at " + index
                        + " as this variable slot is in use."
                        + "\nCurrent variables are [" + currentVars() + "]");

            v = index2var_.get(index2);
            if( v != null )
                throw new YabelBadVariableException("Cannot define variable \""
                        + name + "\" of type " + type + " at " + index
                        + " as it requires a double width slot and slot "
                        + (index + 1) + " is in use.\nCurrent variables are ["
                        + currentVars() + "]");
        } else {
            // find available slot
            int i = 0;
            while( true ) {
                index1 = Integer.valueOf(i);
                Var v = index2var_.get(index1);
                if( v == null ) {
                    index2 = Integer.valueOf(i + isDouble);
                    v = index2var_.get(index2);
                    if( v == null ) break;

                    i += 2;
                } else {
                    i += v.isDouble() ? 2 : 1;
                }
            }
        }

        // create variable and store it
        Var var = new Var(index1.intValue(), name, type);

        name2var_.put(name, var);
        index2var_.put(index1, var);
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
                int jump = lbl.getLocation() - use.opLoc_;
                int here = use.location_;
                if( use.width_ == 2 ) {
                    // verify 2 byte offset is possible
                    if( (jump < Short.MIN_VALUE) || (jump > Short.MAX_VALUE) ) {
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
     * Get all the labels used in the compiled code.
     * 
     * @return all the labels
     */
    public Collection<Label> getAllLabels() {
        return Collections.unmodifiableCollection(labels_.values());
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
     * Get or create the label associated with a given name
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
        int loc = li.getLocation();
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
     * Get the slot for a variable
     * 
     * @param name
     *            the variable's name
     * @param raw
     *            the statement
     * @return the slot
     */
    public int getVariable(String name, String raw) {
        Var v = name2var_.get(name);
        if( v != null ) return v.getIndex();
        return getInt(name, raw);
    }


    /**
     * Reset the output to begin compilation from the beginning.
     */
    public void reset() {
        code_ = null;
        output_.reset();
        labels_.clear();
    }


    /**
     * Resolve a location against this output. Absolute locations are unchanged.
     * Named locations are only fixed when the output is complete.
     * 
     * @param location
     *            the location - either a name of an address
     * @return a resolved location
     */
    public Location resolve(Location location) {
        // Labels are unchanged
        if( location instanceof Label ) return location;

        // replace named locations with labels
        if( location instanceof NamedLocation ) {
            NamedLocation loc = (NamedLocation) location;
            String name = loc.getName();
            return getLabel(name);
        }

        return location;
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
        if( label.getLocation() != -1 ) {
            throw new YabelLabelException("Label \"" + name
                    + "\" defined more than once");
        }
        label.setLocation(output_.size());
    }


    /**
     * Undefine a variable
     * 
     * @param name
     *            the variable to undefine
     */
    public void undefineVariable(String name) {
        Var v = name2var_.remove(name);
        if( v != null ) {
            index2var_.remove(Integer.valueOf(v.getIndex()));
            return;
        }

        // maybe we have been given an index
        Integer ind;
        try {
            ind = new Integer(name);
        } catch (NumberFormatException nfe) {
            throw new YabelBadVariableException("Variable \"" + name
                    + "\" cannot be identified. Current variables are: ["
                    + currentVars() + "]");
        }

        v = index2var_.remove(ind);
        if( v != null ) {
            name2var_.remove(v.getName());
            return;
        }

        // it was a number, but it wasn't defined
        throw new YabelBadVariableException("Variable \"" + name
                + "\" cannot be identified. Current variables are: ["
                + currentVars() + "]");
    }


    /**
     * Was the last operand a WIDE?
     * 
     * @return true if last was wide
     */
    public boolean wasLastWide() {
        return lastWasWide_;
    }
}
