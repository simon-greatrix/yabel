package yabel.attributes;

/**
 * An object that wants to be informed of changes to its attribute list.
 * 
 * @author Simon Greatrix
 * 
 */
public interface AttributeListListener {
    /**
     * Notify this listener that the attributes have changed.
     * 
     * @param attrId
     *            the name of the changed attribute
     * @param attr
     *            the new value of the attribute (null if removed)
     */
    public void attributeChanged(String attrId, Attribute attr);
}