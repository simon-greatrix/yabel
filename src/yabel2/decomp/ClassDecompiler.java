package yabel2.decomp;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import yabel.ClassData;
import yabel.Field;
import yabel.Method;
import yabel.constants.ConstantClass;
import yabel2.Access;
import yabel2.Version;

public class ClassDecompiler extends ClassVisitor {
    
    private final ClassData data_ = new ClassData();

    public ClassDecompiler() {
        super(Opcodes.ASM5);
    }

    public void reset() {
        data_.clear();
    }

    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        data_.put("javaVersion",Version.forCode(version).id());
        data_.put("access",Access.CLASS.accessCode(access));
        data_.put("name",name.replace('/','.'));
        if( signature!=null ) {
            data_.put("signature",signature);
        }
        if( superName!=null ) {
            data_.put("super",superName.replace('/', '.'));
        }
        if( interfaces!=null && interfaces.length>0 ) {
            List<String> ifaces = new ArrayList<>(interfaces.length);
            for(String s : interfaces) {
                if( s!=null ) {
                    ifaces.add(s.replace('/', '.'));
                }
            }
            data_.putList(String.class, "interfaces", ifaces);
        }
        data_.putList(ClassData.class, "fields", new ArrayList<ClassData>());
        data_.putList(ClassData.class, "methods", new ArrayList<ClassData>());
        
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        // TODO Auto-generated method stub
        super.visitSource(source, debug);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        // TODO Auto-generated method stub
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef,
            TypePath typePath, String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        // TODO Auto-generated method stub
        super.visitAttribute(attr);
    }

    @Override
    public void visitInnerClass(String name, String outerName,
            String innerName, int access) {
        // TODO Auto-generated method stub
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        // TODO Auto-generated method stub
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        // TODO Auto-generated method stub
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        // TODO Auto-generated method stub
        super.visitEnd();
    }
    
    /**
     * Convert the class into a ClassData representation.
     * 
     * @return the class data representation
     */
    public ClassData toClassData() {
        ClassData cd = new ClassData();
        cd.put("access", Access.accessCode(access_ & ~Access.ACC_SUPER));
        cd.put("name", thisClass_.getClassName().get());
        cd.put("super", superClass_.getClassName().get());
        cd.put("version", Integer.valueOf(version_));

        // add interfaces
        List<String> ifaces = new ArrayList<String>(interfaces_.size());
        for(ConstantClass cc:interfaces_) {
            ifaces.add(cc.getClassName().get());
        }
        cd.putList(String.class, "interfaces", ifaces);

        // add fields
        List<ClassData> fields = new ArrayList<ClassData>(fields_.size());
        for(Field f:fields_) {
            fields.add(f.toClassData());
        }
        cd.putList(ClassData.class, "fields", fields);

        // add methods
        List<ClassData> meths = new ArrayList<ClassData>(methods_.size());
        for(Method m:methods_) {
            meths.add(m.toClassData());
        }
        cd.putList(ClassData.class, "methods", meths);

        // and the attributes
        cd.putList(ClassData.class, "attributes", attrList_.toClassData());

        return cd;
    }
   
}
