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
    /** Exception type caught */
    final ConstantClass catchType_;

    /** End processing instruction */
    final int endPC_;

    /** Handler processing instruction */
    final int handlerPC_;

    /** Start processing instruction */
    final int startPC_;


    /**
     * New Handler
     * 
     * @param cp
     *            the constant pool
     * @param cd
     *            class data representation
     */
    public Handler(ConstantPool cp, ClassData cd) {
        startPC_ = cd.getSafe(Integer.class, "startPC").intValue();
        endPC_ = cd.getSafe(Integer.class, "endPC").intValue();
        handlerPC_ = cd.getSafe(Integer.class, "handlerPC").intValue();

        String type = cd.get(String.class, "catchType");
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
        startPC_ = IO.readU2(input);
        endPC_ = IO.readU2(input);
        handlerPC_ = IO.readU2(input);
        
        int type = IO.readU2(input);
        if( type!=0 ) {
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
        startPC_ = startPC;
        endPC_ = endPC;
        handlerPC_ = handlerPC;
        catchType_ = catchType;
    }


    /**
     * Get the name of the catch type of this handler.
     * 
     * @return the catch type or null if this is a catch-all.
     */
    public ConstantClass getCatchType() {
        return catchType_;
    }


    public int getEndPC() {
        return endPC_;
    }


    public int getHandlerPC() {
        return handlerPC_;
    }


    public int getStartPC() {
        return startPC_;
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
        cd.put("endPC", Integer.valueOf(endPC_));
        cd.put("startPC", Integer.valueOf(startPC_));
        cd.put("handlerPC", Integer.valueOf(handlerPC_));
        if( catchType_ == null ) {
            cd.put("catchType", null);
        } else {
            cd.put("catchType", catchType_.getClassName().get());
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
        IO.writeU2(baos, startPC_);
        IO.writeU2(baos, endPC_);
        IO.writeU2(baos, handlerPC_);
        if( catchType_ == null ) {
            IO.writeU2(baos, 0);
        } else {
            IO.writeU2(baos, catchType_.getIndex());
        }
    }
}