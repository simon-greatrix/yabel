package yabel.attributes;

/**
 * An object that wants to be informed of changes to its attribute list.
 * 
 * @author Simon Greatrix
 * 
 */
public interface AttributeListListener {
    /** Notify this listener that the attributes have changed */
    public void attributesChanged();
}