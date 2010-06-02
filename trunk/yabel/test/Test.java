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
    
    static void testMethod(String clsName, Method m) {
        if( (m.getAccess() & (ClassBuilder.ACC_ABSTRACT | ClassBuilder.ACC_NATIVE)) != 0 ) return;
        System.out.println("Testing "+clsName+" : "+m.getName()+m.getType());
        Code code = m.getCode();
        ClassData dna = code.decompile();
        if(ClassBuilder.DEBUG) System.out.println(dna);
        
        String sdna = dna.toString();
        ByteArrayInputStream in = new ByteArrayInputStream(sdna.getBytes(Charset.forName("UTF-8")));
        ClassData cd2 = null;
        try {
            cd2 = XMLDataReader.read(in);
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
        
        
//        System.out.println("max locals = " + code.getMaxLocals());
//        System.out.println("max stack = " + code.getMaxStack());

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

        if( original.length != compiled.length ) {
            throw new Error("Compiled code length differs (original="
                    + original.length + ", compiled=" + compiled.length);
        }
        for(int i = 0;i < original.length;i++) {
            if( ClassBuilder.DEBUG ) {
                System.out.println((i-14)+" : "+(0xff & original[i])+" , "+(0xff & compiled[i]));
            }
            if( original[i] != compiled[i] ) {
                throw new Error("Compiled code differs at byte " + (i-14));
            }
        }        
    }
    
    static void testClass(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        ClassBuilder builder = new ClassBuilder(in);
        in.close();

        Method[] ms = builder.getMethods();
        for(Method m:ms) {
            testMethod(builder.getName(), m);
        }
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
