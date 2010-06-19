package yabel.io;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import yabel.ClassData;
import yabel.SwitchData;
import yabel.ClassData.NamedValue;
import yabel.ClassData.TypedList;
import yabel.SwitchData.Case;

/**
 * Types that can be in the XML file.
 * 
 * @author Simon Greatrix
 * 
 */
public enum XMLType {
    /** Integer type */
    INTEGER(Integer.class, "integer") {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            String t = XMLDataReader.getText(value);
            return (t == null) ? null : Integer.valueOf(t);
        }
    },

    /** Long type */
    LONG(Long.class, "long") {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            String t = XMLDataReader.getText(value);
            return (t == null) ? null : Long.valueOf(t);
        }
    },

    /** A List */
    LIST(TypedList.class, "list", false, new String[] { "type" }) {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            String classAttr = value.getAttribute("type");
            if( (classAttr == null) || classAttr.equals("") ) {
                throw new YabelXMLException(
                        "LIST tag is missing CLASS attribute");
            }

            // get the representation for the class
            XMLType type = XMLType.forTag(classAttr);
            if( type == null ) {
                throw new YabelXMLException(
                        "LIST tag TYPE attribute specifies unrepresented type:"
                                + classAttr);
            }

            List<Object> list = new ArrayList<Object>();

            List<Element> elems = XMLDataReader.getChildElements(value, type,
                    NULL);
            for(Element e:elems) {
                list.add(type.read(e));
            }

            return XMLDataReader.castList(type.getType(), list);
        }


        /** {@inheritDoc} */
        @Override
        public void write(XMLDataWriter xdw, Object value) throws IOException {

            TypedList<?> tl = (TypedList<?>) value;
            Class<?> cl = tl.getType();
            XMLType type = XMLType.forClass(cl);
            xdw.write(LIST.getStartTag(type.getTag()));
            xdw.incrIndent();
            List<?> list = tl.get(cl);
            for(Object o:list) {
                xdw.indent();
                type.write(xdw, o);
            }

            xdw.decrIndent();
            xdw.indent();
            xdw.write(LIST.getEndTag());
        }
    },

    /** Float type */
    FLOAT(Float.class, "float") {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            String t = XMLDataReader.getText(value);
            return (t == null) ? null : Float.valueOf(t);
        }
    },

    /** Double type */
    DOUBLE(Double.class, "double") {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            String t = XMLDataReader.getText(value);
            return (t == null) ? null : Double.valueOf(t);
        }
    },

    /** String type */
    STRING(String.class, "string") {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            String t = XMLDataReader.getText(value);
            int size = t.length();
            StringBuilder buf = new StringBuilder(size);
            
            outer:
            for(int i=0;i<size;i++) {
                char ch = t.charAt(i);
                if( ch=='\r' ) {
                    // convert \r\n and \r to \n
                    int j=i+1;
                    if( (j<size) && (t.charAt(j)=='\n') ) continue;
                    ch = '\n';
                }
                
                buf.append(ch);
                if( ch!='&' ) continue;
                
                // is it an &1234; value?
                int chv=0;
                int j=i+1;
                
                // first digit must be present and not ';'
                if( j==size ) continue;
                ch = t.charAt(j);
                int d = Character.digit(ch, 16);
                if( d==-1 ) continue;
                chv = d;
                
                // 2nd to 4th digit
                for(int k=2; k<6; k++) {
                    j++;
                    if( j==size ) continue outer;
                    ch = t.charAt(j);
                    if( ch==';' ) {
                        // it was &1234;
                        buf.setCharAt(buf.length()-1, (char) chv);
                        i+=k;
                    }
                    d = Character.digit(ch, 16);
                    if( d==-1 ) continue outer;
                    chv = chv*16 + d;
                }
                
                // not matched, just leave it as a '&'
            }
            return buf.toString();
        }


        /** {@inheritDoc} */
        @Override
        public void write(XMLDataWriter xdw, Object value) throws IOException {
            if( value == null ) {
                xdw.write(NULL.getStartTag());
                return;
            }
            String v = (String) value;
            StringBuffer buf = new StringBuffer(v.length());
            for(int i=0;i<v.length();i++) {
                char ch = v.charAt(i);
                // these are the characters allowed in CDATA, except for '&' and '\r' (0xd)
                if( ((0x20<=ch) && (ch<=0xd7ff) && (ch!='&')) || (ch==0x9) || (ch==0xa) || ((0xe000<=ch) && (ch<=0xfffd))) {
                    buf.append(ch);
                    continue;
                }
                
                // escape the char
                buf.append('&');
                buf.append(Integer.toHexString(ch));
                buf.append(';');
            }
            xdw.write(getStartTag());
            xdw.write(XMLDataWriter.cdata(buf.toString()));
            xdw.write(getEndTag());
        }
    },

    /** A null in the input */
    NULL(Object.class, "null", true, new String[0]) {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            return null;
        }


        /** {@inheritDoc} */
        @Override
        public void write(XMLDataWriter xdw, Object value) throws IOException {
            xdw.write(getStartTag());
        }
    },

    /** An Entry in a class data element */
    ENTRY(NamedValue.class, "entry", false, new String[] { "key" }) {
        /** {@inheritDoc} */
        @Override
        public Object read(Element entry) {
            String k = entry.getAttribute("key");
            if( k == null || k.equals("") )
                throw new YabelXMLException("Entry has no key");

            // find first child element of entry, which will be the value
            Node n = XMLDataReader.checkHasOneChild(entry);
            if( !(n instanceof Element) ) {
                throw new YabelXMLException("Element "
                        + entry.getTagName() + " does not contain a value");
            }

            // get XMLType for value
            Element value = (Element) n;
            XMLType xt = XMLType.forTag(value.getTagName());
            if( xt == null )
                throw new YabelXMLException(
                        "Unknown child for <entry>: " + value.getTagName());

            // process value
            Object o = xt.read(value);

            return new NamedValue(k, o);
        }


        /** {@inheritDoc} */
        @Override
        public void write(XMLDataWriter xdw, Object value) throws IOException {
            NamedValue nv = (NamedValue) value;
            xdw.write(getStartTag(nv.getKey()));
            Object o = nv.getValue();
            if( o == null ) {
                xdw.write(NULL.getStartTag());
            } else {
                XMLType xt = XMLType.forClass(o.getClass());
                xt.write(xdw, o);
            }
            xdw.write(getEndTag());
        }
    },

    /** ClassData type */
    DATA(ClassData.class, "classdata") {
        /** {@inheritDoc} */
        @Override
        public Object read(Element value) {
            ClassData cd = new ClassData();
            List<Element> children = XMLDataReader.getChildElements(value,
                    ENTRY, ENTRY);
            for(Element el:children) {
                cd.put((NamedValue) ENTRY.read(el));
            }
            return cd;
        }


        /** {@inheritDoc} */
        @Override
        public void write(XMLDataWriter xdw, Object value) throws IOException {
            if( value == null ) {
                xdw.write(NULL.getStartTag());
                return;
            }

            xdw.write(getStartTag());
            xdw.incrIndent();

            // write out the contents as a list of named values
            ClassData cd = (ClassData) value;
            for(Entry<String, Object> e:cd.entrySet()) {
                xdw.indent();

                String k = e.getKey();
                Object v = e.getValue();
                NamedValue nv = new NamedValue(k, v);
                ENTRY.write(xdw, nv);
            }

            // done
            xdw.decrIndent();
            xdw.indent();
            xdw.write(getEndTag());
        }
    },

    /** A case for a switch */
    CASE(Case.class, "case", true, new String[] { "value", "label" }) {
        /** {@inheritDoc} */
        @Override
        public Object read(Element entry) {
            String sv = entry.getAttribute("value");
            if( sv == null || sv.equals("") )
                throw new YabelXMLException("<case> has no value attribute");
            String sl = entry.getAttribute("label");
            if( sl == null || sl.equals("") )
                throw new YabelXMLException("<case> has no label attribute");

            return new Case(Integer.valueOf(sv), sl);
        }


        /** {@inheritDoc} */
        @Override
        public void write(XMLDataWriter xdw, Object value) throws IOException {
            Case c = (Case) value;
            xdw.write(getStartTag(c.getValue().toString(), c.getLabel()));
        }
    },

    /** SwitchData type */
    SWITCH(SwitchData.class, "switch", false, new String[] { "default" }) {
        /** {@inheritDoc} */
        @Override
        public Object read(Element entry) {
            String sv = entry.getAttribute("default");
            if( sv == null || sv.equals("") )
                throw new YabelXMLException("<switch> has no default attribute");
            SwitchData sw = new SwitchData(sv);
            List<Element> children = XMLDataReader.getChildElements(entry,
                    CASE, CASE);
            for(Element el:children) {
                sw.add((Case) CASE.read(el));
            }
            return sw;
        }


        /** {@inheritDoc} */
        @Override
        public void write(XMLDataWriter xdw, Object value) throws IOException {
            SwitchData sd = (SwitchData) value;
            xdw.write(getStartTag(sd.getDefault()));
            xdw.incrIndent();
            for(Entry<Integer, String> e:sd) {
                xdw.indent();
                Case c = new Case(e.getKey(), e.getValue());
                CASE.write(xdw, c);
            }
            xdw.decrIndent();
            xdw.indent();
            xdw.write(getEndTag());
        }
    };

    /**
     * Structure of the XML types. Each sub-array starts with the type it refers
     * to and then lists all the valid sub-elements.
     */
    private final static XMLType[][] STRUCTURE = {
            { CASE },
            { DATA, ENTRY },
            { DOUBLE },
            { ENTRY, DATA, SWITCH, INTEGER, LONG, FLOAT, DOUBLE, NULL, STRING,
                    LIST }, { FLOAT }, { INTEGER },
            { LIST, DATA, DOUBLE, FLOAT, INTEGER, LONG, STRING }, { LONG },
            { NULL }, { STRING }, { SWITCH, CASE } };

    /**
     * The format patterns used to create the DTD.
     */
    private static final String[][] FORMAT_PATTERN = {
            { "CASE", "EMPTY" },
            { "DATA", "( {0}* )" },
            { "DOUBLE", "( #PCDATA )" },
            { "ENTRY",
                    "( {0} | {1} | {2} | {3} | {4} | {5} | {6} | {7} | {8} )" },
            { "FLOAT", "( #PCDATA )" }, { "INTEGER", "( #PCDATA )" },
            { "LIST", "( {0}* | {1}* | {2}* | {3}* | {4}* | {5}* )" },
            { "LONG", "( #PCDATA )" }, { "NULL", "EMPTY" },
            { "STRING", "( #PCDATA )" }, { "SWITCH", "( {0}* )" } };

    /** Class for a tag */
    private final static Map<String, XMLType> TYPE_4_TAG;

    /** Tag for a class */
    private final static Map<Class<?>, XMLType> TYPE_4_CLASS;

    static {
        Map<String, XMLType> sx = new HashMap<String, XMLType>();
        Map<Class<?>, XMLType> cx = new HashMap<Class<?>, XMLType>();
        for(XMLType t:XMLType.values()) {
            sx.put(t.tag_, t);
            if( t.clss_ != null ) cx.put(t.clss_, t);
        }
        TYPE_4_TAG = Collections.unmodifiableMap(sx);
        TYPE_4_CLASS = Collections.unmodifiableMap(cx);
    }


    /**
     * Get the XMLType for the given class
     * 
     * @param cl
     *            the class
     * @return the XMLType
     */
    public static XMLType forClass(Class<?> cl) {
        if( cl == null ) return NULL;
        XMLType t = TYPE_4_CLASS.get(cl);
        if( t == null )
            throw new YabelXMLException("Class " + cl.getName()
                    + " cannot be represented through an XML entry");
        return t;
    }


    /**
     * Get the full DOCTYPE DTD for these XMLTypes
     * 
     * @return the full DOCTYPE
     */
    public static String getDocType() {
        StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE ").append(DATA.getTag()).append(" [\n");
        for(XMLType t:XMLType.values()) {
            buf.append(t.getDTD());
        }
        buf.append("]>\n");
        return buf.toString();
    }


    /**
     * Get the XMLType for the given tag.
     * 
     * @param tag
     *            the tag
     * @return the XMLType
     */
    public static XMLType forTag(String tag) {
        return TYPE_4_TAG.get(tag);
    }

    /** Tag for this type */
    private final String tag_;

    /** Class for this type */
    private final Class<?> clss_;

    /** Attributes of this type */
    private final String[] attrs_;

    /** The opening tag */
    private final String startTag_;

    /** Format of the start tag */
    private final MessageFormat startFormat_;

    /** The closing tag */
    private final String endTag_;


    /**
     * New type
     * 
     * @param clss
     *            the class
     * @param tag
     *            the tag
     * @param isEmpty
     *            is this an empty element?
     * @param attrs
     *            the attributes of this tag
     */
    XMLType(Class<?> clss, String tag, boolean isEmpty, String[] attrs) {
        clss_ = clss;
        tag_ = tag;
        attrs_ = attrs;

        StringBuilder buf = new StringBuilder();
        buf.append("<").append(tag_);
        if( attrs != null && attrs.length > 0 ) {
            for(int i = 0;i < attrs.length;i++) {
                buf.append(' ').append(attrs[i]).append("=\"{").append(i).append(
                        "}\"");
            }
        }
        if( isEmpty ) {
            buf.append(" />");
            startTag_ = buf.toString();
            endTag_ = "";
        } else {
            buf.append(">");
            startTag_ = buf.toString();
            endTag_ = "</" + tag + ">";
        }
        startFormat_ = new MessageFormat(startTag_);
    }


    /**
     * New type
     * 
     * @param clss
     *            the class
     * @param tag
     *            the tag
     */
    XMLType(Class<?> clss, String tag) {
        this(clss, tag, false, new String[0]);
    }


    /**
     * New list type
     * 
     * @param member
     *            the list member type
     */
    XMLType(XMLType member) {
        clss_ = List.class;
        tag_ = member.getTag() + "list";
        attrs_ = new String[0];
        startTag_ = "<" + tag_ + ">";
        endTag_ = "</" + tag_ + ">";
        startFormat_ = null;
    }


    /**
     * Get the DTD entry for this XMLType
     * 
     * @return the DTD entry
     */
    public String getDTD() {
        StringBuilder buf = new StringBuilder();
        buf.append("<!ELEMENT ").append(getTag()).append(' ');

        // find the matching structure record
        XMLType[] subTypes = null;
        for(XMLType[] st:STRUCTURE) {
            if( st[0] == this ) {
                subTypes = st;
                break;
            }
        }
        assert subTypes != null;

        // find the DTD format record
        String name = toString();
        String format = null;
        for(String[] fo:FORMAT_PATTERN) {
            if( fo[0].equals(name) ) {
                format = fo[1];
                break;
            }
        }
        assert format != null;

        // create the DTD
        String[] tags = new String[subTypes.length - 1];
        for(int i = 0;i < tags.length;i++) {
            tags[i] = subTypes[i + 1].getTag();
        }

        // build element DTD entry
        buf.append(MessageFormat.format(format, (Object[]) tags));
        buf.append(" >\n");

        // build ATTLIST DTD entry
        if( (attrs_ != null) && (attrs_.length > 0) ) {
            buf.append("<!ATTLIST ").append(getTag());
            for(int i = 0;i < attrs_.length;i++) {
                buf.append(' ').append(attrs_[i]).append(" CDATA #REQUIRED");
            }
            buf.append(">\n");
        }

        return buf.toString();
    }


    /**
     * Get the tag for this type.
     * 
     * @return the tag
     */
    public String getTag() {
        return tag_;
    }


    /**
     * Get the start tag
     * 
     * @param objects
     *            the attributes
     * @return the start tag
     */
    public String getStartTag(Object... objects) {
        if( objects == null || objects.length == 0 ) return startTag_;
        return startFormat_.format(objects);
    }


    /**
     * Get the end tag
     * 
     * @return the end tag
     */
    public String getEndTag() {
        return endTag_;
    }


    /**
     * Get the class for this type.
     * 
     * @return the class
     */
    public Class<?> getType() {
        return clss_;
    }


    /**
     * Read an XMLType value
     * 
     * @param element
     *            the element holding the value
     * @return the object read
     */
    abstract public Object read(Element element);


    /**
     * Write an XMLType value
     * 
     * @param xdw
     *            the writer to write to
     * @param value
     *            the value to write out
     */
    public void write(XMLDataWriter xdw, Object value) throws IOException {
        if( value == null ) {
            xdw.write(NULL.getStartTag());
            return;
        }
        xdw.write(getStartTag());
        String s = String.valueOf(value);
        if( s.equals("") ) {
            xdw.write("<![CDATA[]]>");
        } else {
            xdw.write(String.valueOf(value));
        }
        xdw.write(getEndTag());
    }
}
