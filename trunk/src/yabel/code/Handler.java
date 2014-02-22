package yabel.code;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yabel.ClassData;
import yabel.constants.ConstantClass;
import yabel.constants.ConstantPool;
import yabel.io.IO;

/**
 * Exception Handler block
 * 
 * @author Simon Greatrix
 * 
 */
public class Handler {
    private static Location getLoc(CompilerOutput out, ClassData cd, String name) {
        String id = cd.get(String.class, name);
        if( id != null ) return out.getLabel(id);
        Integer loc = cd.getSafe(Integer.class, name);
        return new Location(loc.intValue());
    }

    /** Exception type caught */
    final ConstantClass catchType_;

    /** End processing instruction */
    final Location endPC_;

    /** Handler processing instruction */
    final Location handlerPC_;

    /** Start processing instruction */
    final Location startPC_;


    /**
     * New Handler
     * 
     * @param cp
     *            the constant pool
     * @param out
     *            the CompilerOutput that knows where labels are located
     * @param cd
     *            class data representation
     */
    public Handler(ConstantPool cp, CompilerOutput out, ClassData cd) {
        startPC_ = getLoc(out, cd, "start");
        endPC_ = getLoc(out, cd, "end");
        handlerPC_ = getLoc(out, cd, "handler");

        String type = cd.get(String.class, "type");
        if( type == null ) {
            catchType_ = null;
        } else {
            catchType_ = new ConstantClass(cp, type);
        }
    }


    /**
     * New Handler
     * 
     * @param cp
     *            the constant pool
     * @param input
     *            stream
     */
    public Handler(ConstantPool cp, InputStream input) throws IOException {
        startPC_ = new Location(IO.readU2(input));
        endPC_ = new Location(IO.readU2(input));
        handlerPC_ = new Location(IO.readU2(input));

        int type = IO.readU2(input);
        if( type != 0 ) {
            catchType_ = cp.validate(type, ConstantClass.class);
        } else {
            catchType_ = null;
        }
    }


    /**
     * New Handler
     * 
     * @param startPC
     *            start instruction
     * @param endPC
     *            end instruction
     * @param handlerPC
     *            handler instruction
     * @param catchType
     *            catch type
     */
    public Handler(int startPC, int endPC, int handlerPC,
            ConstantClass catchType) {
        startPC_ = new Location(startPC);
        endPC_ = new Location(endPC);
        handlerPC_ = new Location(handlerPC);
        catchType_ = catchType;
    }


    /**
     * Get the class of the catch type of this handler.
     * 
     * @return the catch type or null if this is a catch-all.
     */
    public ConstantClass getCatchType() {
        return catchType_;
    }


    /**
     * Get the name of the catch type of this handler
     * 
     * @return the catch type or null if this is a catch-all.
     */
    public String getCatchTypeName() {
        if( catchType_ == null ) return null;
        return catchType_.getClassName().get();
    }


    /**
     * Get the end PC of this handler. If is is not set a YabelLabelException
     * will be thrown
     * 
     * @return the end PC
     */
    public int getEndPC() {
        endPC_.requirePlaced();
        return endPC_.getLocation();
    }


    /**
     * Get the handler PC of this handler. If is is not set a
     * YabelLabelException will be thrown
     * 
     * @return the handler PC
     */
    public int getHandlerPC() {
        handlerPC_.requirePlaced();
        return handlerPC_.getLocation();
    }


    /**
     * Get the start PC of this handler. If is is not set a YabelLabelException
     * will be thrown
     * 
     * @return the start PC
     */
    public int getStartPC() {
        startPC_.requirePlaced();
        return startPC_.getLocation();
    }


    /**
     * Get a representation of this as ClassData
     * 
     * @param cp
     *            the class's constant pool
     * @return the representation
     */
    public ClassData toClassData(ConstantPool cp) {
        ClassData cd = new ClassData();

        cd.put("end", endPC_.getIdentifier());
        cd.put("start", startPC_.getIdentifier());
        cd.put("handler", handlerPC_.getIdentifier());
        if( catchType_ == null ) {
            cd.put("type", null);
        } else {
            cd.put("type", catchType_.getClassName().get());
        }
        return cd;
    }


    /**
     * Write this handler to the output
     * 
     * @param baos
     *            output
     */
    public void writeTo(ByteArrayOutputStream baos) {
        startPC_.requirePlaced();
        endPC_.requirePlaced();
        handlerPC_.requirePlaced();
        IO.writeU2(baos, startPC_.getLocation());
        IO.writeU2(baos, endPC_.getLocation());
        IO.writeU2(baos, handlerPC_.getLocation());
        if( catchType_ == null ) {
            IO.writeU2(baos, 0);
        } else {
            IO.writeU2(baos, catchType_.getIndex());
        }
    }

}