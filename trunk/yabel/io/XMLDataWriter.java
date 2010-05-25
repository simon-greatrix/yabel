package yabel.io;

import yabel.ClassData;
import yabel.SwitchData;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;

public class XMLDataWriter implements ClassDataWriter {
    private int indent_ = 0;

    private Writer writer_;
    
    
    public XMLDataWriter(Writer writer) {
        writer_ = writer;
    }


    @Override
    public void endClassData() throws IOException {
        indent_--;
        indent();
        writer_.write("</classdata>\n");
    }


    @Override
    public void finish() throws IOException {
        writer_.flush();
    }


    @Override
    public void start() throws IOException {
        writer_.write("<?xml version=\"1.0\"?>\n");
        writer_.write("<!DOCTYPE classdata [\n");
        writer_.write("  <!ELEMENT classdata (classdata | switch | list | integer | long | float | double | string | null)*>\n");
        writer_.write("  <!ATTLIST classdata key CDATA>\n");
        writer_.write("  <!ELEMENT switch case*>\n");
        writer_.write("  <!ATTLIST switch key CDATA #REQUIRED default CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT case EMPTY>\n");
        writer_.write("  <!ATTLIST case value CDATA #REQUIRED label CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT list ( item* | classdata* )>\n");
        writer_.write("  <!ATTLIST list key CDATA #REQUIRED type CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT item #PCDATA>\n");
        writer_.write("  <!ELEMENT integer #PCDATA>\n");
        writer_.write("  <!ATTLIST integer key CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT long #PCDATA>\n");
        writer_.write("  <!ATTLIST long key CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT float #PCDATA>\n");
        writer_.write("  <!ATTLIST float key CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT double #PCDATA>\n");
        writer_.write("  <!ATTLIST double key CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT string #PCDATA>\n");
        writer_.write("  <!ATTLIST string key CDATA #REQUIRED>\n");
        writer_.write("  <!ELEMENT null EMPTY>\n");
        writer_.write("  <!ATTLIST null key CDATA #REQUIRED>\n");
        writer_.write("]>\n");
    }
    
    
    private void indent() throws IOException {
        for(int i=0;i<indent_;i++) {
            writer_.write("  ");
        }
    }
    
    
    private String cdata(String val) {
        boolean ok = true;
        for(int i=0;i<val.length();i++) {
            char c = val.charAt(i);
            if( (c<0x21) || (c>0x7e) || (c=='\'') || (c=='"') || (c=='&') || (c=='<') || (c=='>') ) {
                ok=false;
                break;
            }
        }
        if( ok ) return val;
        
        StringBuilder buf = new StringBuilder(val.length() + 15);
        buf.append("<![CDATA[");
        int p = 0;
        while( true ) {
            int q = val.indexOf("]]>",p);
            if( q!=-1 ) {
                buf.append(val,p,q);
                buf.append("]]>]]><![CDATA[");
                p = q+3;
            } else {
                break;
            }
        }
        buf.append(val,p,val.length());
        buf.append("]]>");
        return buf.toString();
    }
    
    
    private String attr(String val) {
        boolean ok = true;
        for(int i=0;i<val.length();i++) {
            char c = val.charAt(i);
            if( (c<0x20) || (c>0x7e) || (c=='\'') || (c=='"') || (c=='&') || (c=='<') || (c=='>') ) {
                ok=false;
                break;
            }
        }
        if( ok ) return val;
        StringBuilder buf = new StringBuilder(val.length());
        for(int i=0;i<val.length();i++) {
            char c = val.charAt(i);
            switch( c ) {
            case '\'' : buf.append("&apos;"); break;
            case '"' : buf.append("&quot;"); break;
            case '&' : buf.append("&amp;"); break;
            case '<' : buf.append("&lt;"); break;
            case '>' : buf.append("&gt;"); break;
            default:
                if( (c<0x20) || (c>0x7e) ) {
                    buf.append("&#x").append(Integer.toHexString(c)).append(';');
                } else {
                    buf.append(c);
                }
            }
        }
        return buf.toString();
    }


    @Override
    public void startClassData(String name) throws IOException {
        indent();
        if( name==null ) {
            writer_.write("<classdata>\n");
        } else {
            writer_.write("<classdata key=\"");
            writer_.write(attr(name));
            writer_.write("\">\n");
        }
        indent_ ++;
    }


    @Override
    public void write(String k, Object v) throws IOException {
        k = attr(k);
        indent();
        if( v==null ) {
            writer_.write("<null key=\""+k+"\" />\n");
            return;
        }

        Class<?> clss = v.getClass();
        String type;
        if( Integer.class.equals(clss) ) {
            type="integer";
        } else if( Long.class.equals(clss) ) {
            type="long";
        } else if( Float.class.equals(clss) ) {
            type="float";
        } else if( Double.class.equals(clss) ) {
            type="double";
        } else if( String.class.equals(clss) ) {
            type="string";
        } else if( SwitchData.class.equals(clss) ) {
            type="switchdata";
        } else {
            throw new IOException("Class "+clss.getName()+" is not serializable via XMLDataWriter");
        }
        
        if( v instanceof Number ) {
            writer_.write("<"+type+" key=\""+k+"\">");
            writer_.write(v.toString());
            writer_.write("</"+type+">\n");
            return;
        }
        if( v instanceof String ) {
            writer_.write("<string key=\""+k+"\">");
            writer_.write(cdata((String) v));
            writer_.write("</string>\n");
        }
        if( v instanceof SwitchData ) {
            SwitchData sd = (SwitchData) v;
            String dflt = attr(sd.getDefault());
            writer_.write("<switch key=\""+k+"\" default=\""+dflt+"\">\n");
            indent_++;
            for(Entry<Integer,String> e : sd) {
                indent();
                writer_.write("<case value=\""+e.getKey()+"\" label=\""+attr(e.getValue())+"\" />");
            }
            indent_--;
            indent();
            writer_.write("</switch>\n");
        }
    }


    @Override
    public <T> void writeList(String k, Class<T> clss, List<T> list) throws IOException {
        k = attr(k);
        
        String type;
        if( Integer.class.equals(clss) ) {
            type="integer";
        } else if( Long.class.equals(clss) ) {
            type="long";
        } else if( Float.class.equals(clss) ) {
            type="float";
        } else if( Double.class.equals(clss) ) {
            type="double";
        } else if( String.class.equals(clss) ) {
            type="string";
        } else if( ClassData.class.equals(clss) ) {
            type="classdata";
        } else {
            throw new IOException("Class "+clss.getName()+" is not serializable via XMLDataWriter");
        }
     
        indent();
        writer_.write("<list key=\""+attr(k)+"\" type=\""+type+"\">\n");
        indent_++;
        for(T o : list) {
            if( type.equals("classdata") ) {
                ((ClassData) o).writeTo(this);
            } else if( type.equals("string") ) {
                indent();
                writer_.write("<item>");
                writer_.write(cdata((String) o));
                writer_.write("</item>\n");
            } else {
                indent();
                writer_.write("<item>");
                writer_.write(String.valueOf(o));
                writer_.write("</item>\n");
            }
        }
        indent_--;
        indent();
        writer_.write("</list>\n");
    }

}
