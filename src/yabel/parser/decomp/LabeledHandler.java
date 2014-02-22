package yabel.parser.decomp;

import yabel.ClassData;
import yabel.code.Handler;

/**
 * 
 * @author Simon Greatrix
 */
public class LabeledHandler {
    /** Location where the handler stops applying */
    private final Label.Ref endPC_;

    /** Location of the handler */
    private final Label.Ref handlerPC_;

    /** Location where the handler starts applying */
    private final Label.Ref startPC_;

    /** The handler's catch type */
    private final String type_;


    /**
     * Create new labeled handler
     * 
     * @param labels
     *            the label list associated with the code block
     * @param handler
     *            the handler
     */
    public LabeledHandler(LabelList labels, Handler handler) {
        this(labels, handler.getStartPC(), handler.getEndPC(),
                handler.getHandlerPC(), handler.getCatchTypeName());
    }


    /**
     * Create new labeled handler
     * 
     * @param labels
     *            the label list associated with the code block
     * @param startPC
     *            the starting location
     * @param endPC
     *            the end location
     * @param handlerPC
     *            the handler location
     * @param type
     *            the type this handler catches
     */
    public LabeledHandler(LabelList labels, int startPC, int endPC,
            int handlerPC, String type) {
        startPC_ = labels.getRef(startPC);
        endPC_ = labels.getRef(endPC);
        handlerPC_ = labels.getRef(handlerPC);
        type_ = type;
    }


    /**
     * Get this labeled handler as a ClassData object which can be used to
     * reconstruct this handler. Note that if the labels are renamed after this
     * is called, the returned ClassData will be invalid.
     * 
     * @return ClassData representing this handler
     */
    public ClassData toClassData() {
        ClassData cd = new ClassData();
        cd.put("start", startPC_.getName());
        cd.put("end", endPC_.getName());
        cd.put("handler", handlerPC_.getName());
        cd.put("type", type_);
        return cd;
    }
}
