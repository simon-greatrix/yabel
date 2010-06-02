package yabel.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import yabel.ClassData;
import yabel.ClassData.TypedList;

/**
 * Read a ClassData from its XML encoding
 * 
 * @author Simon Greatrix
 * 
 */
public class XMLDataReader {
    /**
     * Cast a general list to a specific type list
     * 
     * @param <E>
     *            the specific type
     * @param clss
     *            the class of the objects contained in the list
     * @param list
     *            the list to cast
     * @return the cast list
     */
    static <E> TypedList<E> castList(Class<E> clss, List<?> list) {
        List<E> val = new ArrayList<E>();
        for(Object o:list) {
            E v = clss.cast(o);
            val.add(v);
        }
        return new TypedList<E>(val, clss);
    }


    /**
     * Check that an element has exactly one child and return that child node
     * 
     * @param value
     *            the element to check
     * @return the child node
     */
    static Node checkHasOneChild(Element value) {
        if( value.hasChildNodes() ) {
            Node n = value.getFirstChild();
            if( n.isSameNode(value.getLastChild()) ) return n;
        }
        throw new IllegalArgumentException("Element " + value.getTagName()
                + " does not have one child node");
    }


    /**
     * Get the child elements of the provided element.
     * 
     * @param parent
     *            the parent element
     * @param type1
     *            the required type of the child elements
     * @param type2
     *            the alternate required type of the child elements
     * @return the children
     */
    static List<Element> getChildElements(Element parent, XMLType type1,
            XMLType type2) {
        List<Element> children = new ArrayList<Element>();
        NodeList nl = parent.getChildNodes();
        for(int i = 0;i < nl.getLength();i++) {
            // find the next element in the parent
            Node n = nl.item(i);
            if( !(n instanceof Element) ) continue;

            // verify element is an entry
            Element entry = (Element) n;
            String tagName = entry.getTagName();
            if( !(tagName.equals(type1.getTag()) || tagName.equals(type2.getTag())) )
                if( type1 == type2 ) throw new IllegalArgumentException(
                        parent.getTagName() + " contains node with tag "
                                + entry.getTagName() + ". Only "
                                + type1.getTag() + " is allowed.");
                else
                    throw new IllegalArgumentException(parent.getTagName()
                            + " contains node with tag " + entry.getTagName()
                            + ". Only " + type1.getTag() + " or "
                            + type2.getTag() + " is allowed.");
            children.add(entry);
        }
        return children;
    }


    /**
     * Get the text contents of the element. If the element contains a NULL tag,
     * null is returned.
     * 
     * @param value
     *            the element to get the contents of
     * @return the text or null
     */
    static String getText(Element value) {
        if( !value.hasChildNodes() ) return "";
        Node n = checkHasOneChild(value);
        if( n.getNodeType() == Node.TEXT_NODE ) return ((Text) n).getData();
        if( (n.getNodeType() == Node.ELEMENT_NODE)
                && ((Element) n).getTagName().equals(XMLType.NULL.getTag()) ) {
            return null;
        }
        throw new IllegalArgumentException("Element " + value.getTagName()
                + " contains a " + n);

    }


    /**
     * Test the ability to read an XML file
     * 
     * @param args
     *            the file name
     * @throws IOException
     * @throws SAXException
     */
    public static void main(String[] args) throws IOException, SAXException {
        ClassData cd = read(new File(args[0]));
        System.out.println(cd);
    }


    /**
     * Read a ClassData from its XML document
     * 
     * @param doc
     *            the XML document containing the ClassData
     * @return the ClassData
     */
    public static ClassData read(Document doc) {
        Element elem = doc.getDocumentElement();
        if( !elem.getTagName().equals(XMLType.DATA.getTag()) ) {
            throw new IllegalArgumentException("Root element is not "
                    + XMLType.DATA.getTag() + " but " + elem.getTagName());
        }

        return (ClassData) XMLType.DATA.read(elem);
    }


    /**
     * Read a ClassData from a file
     * 
     * @param file
     *            the file
     * @return the ClassData
     * @throws IOException
     * @throws SAXException
     */
    public static ClassData read(File file) throws IOException, SAXException {
        // Create a builder factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        factory.setValidating(true);

        // Create the builder and parse the file
        Document doc;
        try {
            doc = factory.newDocumentBuilder().parse(file);
        } catch (ParserConfigurationException e) {
            // should not fail as our configuration is fixed and simple
            throw new Error("Standard XML configuration failed", e);
        }
        return read(doc);
    }


    /**
     * Read a ClassData from a stream
     * 
     * @param in
     *            the input stream reading the XML
     * @return the ClassData
     * @throws IOException
     * @throws SAXException
     */
    public static ClassData read(InputStream in) throws IOException,
            SAXException {
        // Create a builder factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        factory.setValidating(true);

        // Create the builder and parse the file
        Document doc;
        try {
            doc = factory.newDocumentBuilder().parse(in);
        } catch (ParserConfigurationException e) {
            // should not fail as our configuration is fixed and simple
            throw new Error("Standard XML configuration failed", e);
        }
        return read(doc);
    }
}
