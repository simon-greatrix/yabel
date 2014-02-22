package yabel.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Write out a ClassData object in XML format.
 * 
 * @author Simon Greatrix
 * 
 */
public class XMLDataWriter implements ClassDataWriter {

    /** The current indentation */
    private int indent_ = 0;

    /** The writer to write out to */
    private Writer writer_;

    /** The encoding used */
    private String encoding_;


    /**
     * Create a new XMLDataWriter writing to the given writer.
     * 
     * @param writer
     *            the writer to write to
     */
    public XMLDataWriter(Writer writer) {
        writer_ = writer;
        if( writer_ instanceof OutputStreamWriter ) {
            encoding_ = ((OutputStreamWriter) writer).getEncoding();
        } else {
            encoding_ = null;
        }
    }


    /**
     * Create a new XMLDataWriter writing to the given writer.
     * 
     * @param writer
     *            the writer to write to
     * @param encoding
     *            the character encoding
     */
    public XMLDataWriter(Writer writer, String encoding) {
        writer_ = writer;
        encoding_ = encoding;
    }


    /**
     * Create a new XMLDataWriter writing to the given stream. The XML will be
     * encoded in UTF-8
     * 
     * @param out
     *            the stream to write to
     */
    public XMLDataWriter(OutputStream out) {
        this(new OutputStreamWriter(out, Charset.forName("UTF-8")), "UTF-8");
    }


    /**
     * Create a new XMLDataWriter writing to the given file. The XML will be
     * encoded in UTF-8
     * 
     * @param file
     *            the file to write to
     */
    public XMLDataWriter(File file) throws IOException {
        this(new FileOutputStream(file));
    }


    /** {@inheritDoc} */
    @Override
    public void finish() throws IOException {
        writer_.flush();
    }


    /** {@inheritDoc} */
    @Override
    public void start() throws IOException {
        if( encoding_ == null ) writer_.write("<?xml version=\"1.0\"?>\n");
        else
            writer_.write("<?xml version=\"1.0\" encoding=\"" + encoding_
                    + "\"?>\n");

        writer_.write(XMLType.getDocType());
    }


    /**
     * Insert a new-line and then the current level of indentation
     * @throws IOException
     */
    void indent() throws IOException {
        writer_.write('\n');
        for(int i = 0;i < indent_;i++) {
            writer_.write("  ");
        }
    }
    
    
    /**
     * Increment the current indent.
     */
    void incrIndent() {
        indent_ ++;
    }
    
    
    /**
     * Decrease the current indent.
     */
    void decrIndent() {
        indent_ --;
    }


    /**
     * Convert a string into a CDATA block if necessary
     * 
     * @param val
     *            the string to convert
     * @return the CDATA or raw string
     */
    static String cdata(String val) {
        // check if we need a CDATA block
        boolean ok = true;
        for(int i = 0;i < val.length();i++) {
            char c = val.charAt(i);
            if( (c < 0x21) || (c > 0x7e) || (c == '\'') || (c == '"')
                    || (c == '&') || (c == '<') || (c == '>') ) {
                ok = false;
                break;
            }
        }
        // if no block needed just return input
        if( ok && (val.length()>0)) return val;

        StringBuilder buf = new StringBuilder(val.length() + 15);
        buf.append("<![CDATA[");
        // can't put ]]> inside a CDATA block so break on those
        int p = 0;
        while( true ) {
            int q = val.indexOf("]]>", p);
            if( q != -1 ) {
                buf.append(val, p, q);
                buf.append("]]]]><![CDATA[>");
                p = q + 3;
            } else {
                break;
            }
        }
        // finish the CDATA
        buf.append(val, p, val.length());
        buf.append("]]>");
        return buf.toString();
    }


    /**
     * Get an attribute value, where all special characters have been escaped.
     * 
     * @param val
     *            the source for the attribute value
     * @return the escaped value
     */
    static String attr(String val) {
        boolean ok = true;
        for(int i = 0;i < val.length();i++) {
            char c = val.charAt(i);
            if( (c < 0x20) || (c > 0x7e) || (c == '\'') || (c == '"')
                    || (c == '&') || (c == '<') || (c == '>') ) {
                ok = false;
                break;
            }
        }
        if( ok ) return val;
        StringBuilder buf = new StringBuilder(val.length());
        for(int i = 0;i < val.length();i++) {
            char c = val.charAt(i);
            switch (c) {
            case '\'':
                buf.append("&apos;");
                break;
            case '"':
                buf.append("&quot;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            case '<':
                buf.append("&lt;");
                break;
            case '>':
                buf.append("&gt;");
                break;
            default:
                if( (c < 0x20) || (c > 0x7e) ) {
                    buf.append("&#x").append(Integer.toHexString(c)).append(';');
                } else {
                    buf.append(c);
                }
            }
        }
        return buf.toString();
    }


    /** {@inheritDoc} */
    @Override
    public void write(Object v) throws IOException {
        XMLType t = (v==null) ? XMLType.NULL : XMLType.forClass(v.getClass());
        t.write(this,v);
    }
    
    
    /**
     * Write the given string to the output
     * @param s the string to write out
     * @throws IOException
     */
    void write(String s) throws IOException {
        writer_.write(s);
    }
}
