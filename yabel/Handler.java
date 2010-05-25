package yabel;

import yabel.io.IO;

import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Exception Handler block
 * 
 * @author Simon Greatrix
 * 
 */
public class Handler {
    /** Exception type caught */
    final int catchType_;

    /** End processing instruction */
    final int endPC_;

    /** Handler processing instruction */
    final int handlerPC_;

    /** Start processing instruction */
    final int startPC_;


    /**
     * New Handler
     * 
     * @param input
     *            stream
     */
    public Handler(InputStream input) throws IOException {
        startPC_ = IO.readU2(input);
        endPC_ = IO.readU2(input);
        handlerPC_ = IO.readU2(input);
        catchType_ = IO.readU2(input);
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
    public Handler(int startPC, int endPC, int handlerPC, int catchType) {
        startPC_ = startPC;
        endPC_ = endPC;
        handlerPC_ = handlerPC;
        catchType_ = catchType;
    }


    /**
     * Get the name of the catch type of this handler.
     * 
     * @param cp
     *            the ConstantPool containing the name of the catch type
     * @return the catch type or null if this is a catch-all.
     */
    public String getCatchType(ConstantPool cp) {
        if( catchType_ == 0 ) return null;
        return cp.validate(catchType_, ConstantUtf8.class).get();
    }


    /**
     * Get the index of the catch type's identifier, or 0 if this is a catch all
     * 
     * @return the type identifier
     */
    public int getCatchType() {
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
     * Write this handler to the output
     * 
     * @param baos
     *            output
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, startPC_);
        IO.writeU2(baos, endPC_);
        IO.writeU2(baos, handlerPC_);
        IO.writeU2(baos, catchType_);
    }
}