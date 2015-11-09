package yabel2.decomp;

import java.io.File;
import java.io.FileInputStream;

import org.objectweb.asm.ClassReader;

public class Main {

    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        ClassReader reader;
        try (
            FileInputStream in = new FileInputStream(file);
        ) {
            reader = new ClassReader(in);
        }

        ClassDecompiler d = new ClassDecompiler();
        d.reset();
        reader.accept(d,ClassReader.SKIP_FRAMES);
        
    }

}
