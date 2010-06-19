package yabel.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.xml.sax.SAXException;

import yabel.ClassBuilder;
import yabel.ClassData;
import yabel.Method;
import yabel.code.Code;
import yabel.io.XMLDataReader;

public class Test {
    
    static void checkBytes(byte[] original, byte[] compiled) {
        int len = Math.min(original.length, compiled.length);
        int diff=-100;
        for(int i = 0;i < len;i++) {
            if( ClassBuilder.DEBUG ) {
                System.out.format("%5d : %02x , %02x %s\n", Integer.valueOf(i-14),
                        Integer.valueOf(0xff & original[i]), Integer.valueOf(0xff & compiled[i]),
                        original[i]==compiled[i] ? "" : "****" );
            }
            if( original[i] != compiled[i] ) diff=i-14;
        }
        if ( diff!=-100 ) throw new Error("Compiled code differs at byte "+diff);
        if( original.length != compiled.length ) {
            throw new Error("Compiled code length differs (original="
                    + original.length + ", compiled=" + compiled.length);
        }        
    }
    
    static void testMethod(ClassBuilder cls, Method m) {
        if( (m.getAccess() & (ClassBuilder.ACC_ABSTRACT | ClassBuilder.ACC_NATIVE)) != 0 ) return;
        System.out.println("Testing "+cls.getName()+" : "+m.getName()+m.getType());
        Code code = m.getCode();
        ClassData dna = code.decompile();
        if(ClassBuilder.DEBUG) System.out.println(dna);
        
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        code.writeTo(baos);
        byte[] original = baos.toByteArray();

        baos.reset();
        code.reset();
        code.compile(dna.get(String.class,"source"), dna.get(ClassData.class,"data"));
        List<ClassData> la = dna.getList(ClassData.class,"handlers");
        for(ClassData da:la) {
            code.addHandler(da.get(String.class,"start"), da.get(String.class,"end"),
                    da.get(String.class,"handler"), da.get(String.class,"type"));
        }
        code.writeTo(baos);
        byte[] compiled = baos.toByteArray();

        checkBytes(original,compiled);
    }
    
    
    static void checkBytes(ClassBuilder b1, ClassBuilder b2) {
        ByteArrayOutputStream o1 = new ByteArrayOutputStream();
        b1.writeTo(o1);
        byte[] original = o1.toByteArray();
        o1.reset();
        b2.writeTo(o1);
        byte[] compiled = o1.toByteArray();
        
        checkBytes(original,compiled);
    }
    
    static void testClass(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        ClassBuilder builder = new ClassBuilder(in);
        in.close();
        
        ClassData cd1 = builder.toClassData();
        System.out.println(cd1);
        ClassBuilder b2 = new ClassBuilder(cd1);
        ClassData cd2 = b2.toClassData();
        
        if(! cd1.equals(cd2) ) {
            System.out.println(cd1);
            System.out.println(cd2);
            throw new Error("ClassData differs");
        }

        String sdna = cd1.toString();
        ByteArrayInputStream in2 = new ByteArrayInputStream(sdna.getBytes(Charset.forName("UTF-8")));
        cd2 = null;
        try {
            cd2 = XMLDataReader.read(in2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String sd2 = String.valueOf(cd2);
        if(! sd2.equals(sdna) ) {
            System.out.println(sdna);
            System.out.println(sd2);
            throw new Error();
        }

        Method[] ms = builder.getMethods();
        for(Method m:ms) {
            testMethod(builder, m);
        }

        
        cd1 = builder.toClassData();
        b2 = new ClassBuilder(cd1);
        cd2 = b2.toClassData();
        if(! cd1.equals(cd2) ) {
            System.out.println(cd1);
            System.out.println(cd2);
            throw new Error("ClassData differs");
        }
        
        //checkBytes(builder,b2);
    }
    
    
    static void testFolder(File f) throws IOException {
        File[] fs = f.listFiles();
        for(File i : fs) {
            if( i.isDirectory() ) {
                testFolder(i);
            } else {
                if( i.getName().endsWith(".class") ) {
                    testClass(i);
                }
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        if( f.isDirectory() ) testFolder(f);
        if( f.isFile() ) {
            ClassBuilder.DEBUG = true;
            testClass(f);
        }
    } 
}
