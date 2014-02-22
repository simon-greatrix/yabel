package yabel.code;

/**
 * Record of the usage of a label
 * 
 * @author Simon Greatrix
 * 
 */
class LabelUse {
    /** Where this label is used */
    int location_;

    /** Location of op-code */
    int opLoc_;

    /** Width of the location */
    int width_ = 2;
}