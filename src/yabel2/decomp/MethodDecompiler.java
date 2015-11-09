package yabel2.decomp;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodDecompiler extends MethodVisitor {
    public MethodDecompiler() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visitParameter(String name, int access) {
        // TODO Auto-generated method stub
        super.visitParameter(name, access);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        // TODO Auto-generated method stub
        super.visitAttribute(attr);
    }

    @Override
    public void visitCode() {
        // TODO Auto-generated method stub
        super.visitCode();
    }


    @Override
    public void visitInsn(int opcode) {
        // TODO Auto-generated method stub
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        // TODO Auto-generated method stub
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        // TODO Auto-generated method stub
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        // TODO Auto-generated method stub
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name,
            String desc) {
        // TODO Auto-generated method stub
        super.visitFieldInsn(opcode, owner, name, desc);
    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        // TODO Auto-generated method stub
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        // TODO Auto-generated method stub
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        // TODO Auto-generated method stub
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        // TODO Auto-generated method stub
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        // TODO Auto-generated method stub
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        // TODO Auto-generated method stub
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
            Label... labels) {
        // TODO Auto-generated method stub
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        // TODO Auto-generated method stub
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        // TODO Auto-generated method stub
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
            String type) {
        // TODO Auto-generated method stub
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
        // TODO Auto-generated method stub
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        // TODO Auto-generated method stub
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO Auto-generated method stub
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        // TODO Auto-generated method stub
        super.visitEnd();
    }
    
    
}
