package yabel.test;

import yabel.ClassBuilder;
import yabel.ClassData;
import yabel.Method;
import yabel.code.Code;

public class Test2 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ClassBuilder cb = new ClassBuilder(0, "TestClass");
        Method m = cb.addMethod(0,"method", "()V");
        Code c = m.getCode();
        ClassData cd = new ClassData();
        cd.put("val",Integer.valueOf(4));
        c.compile("NUMber:{val}",cd);
    }

}
