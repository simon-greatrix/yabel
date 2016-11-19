package yabel3;

import java.util.HashMap;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import yabel2.Instructions;

public abstract class Instruction {
    private static HashMap<String,Instruction> INSTRUCTIONS = new HashMap<>();
    
    static class Insn extends Instruction {
        Insn(String name, int opcode) {
            super(name, opcode);
        }
        
        void visit(MethodVisitor visitor, Token[] tokens) {
            visitor.visitInsn(opcode_);
        }
    }
    
    static class Iinc extends Instruction {
        Iinc() {
            super("iinc",Opcodes.IINC);
        }
        void visit(MethodVisitor visitor, Token[] tokens) {
            visitor.visitIincInsn(tokens[0].asVariable(), tokens[1].asInt());
        }        
    }
    
    static class IntInsn extends Instruction {
        IntInsn(String name, int opcode) {
            super(name, opcode);
        }
        
        void visit(MethodVisitor visitor, Token[] tokens) {
            int val = tokens[0].asInt();
            switch( val ) {
            case -1:
                visitor.visitInsn(Opcodes.ICONST_M1);
                return;
            case 0:
                visitor.visitInsn(Opcodes.ICONST_0);
                return;
            case 1:
                visitor.visitInsn(Opcodes.ICONST_1);
                return;
            case 2:
                visitor.visitInsn(Opcodes.ICONST_2);
                return;
            case 3:
                visitor.visitInsn(Opcodes.ICONST_3);
                return;
            }
            
            if( Byte.MIN_VALUE <= val && val <= Byte.MAX_VALUE) {
                visitor.visitIntInsn(Opcodes.BIPUSH,val);
                return;
            }
            if( Short.MIN_VALUE <= val && val <= Short.MAX_VALUE) {
                visitor.visitIntInsn(Opcodes.SIPUSH,val);
                return;
            }
            
            visitor.visitLdcInsn(Integer.valueOf(val));
        }
    }
    
    static class VarInsn extends Instruction {
        VarInsn(String name, int opcode) {
            super(name, opcode);
        }
        
        void visit(MethodVisitor visitor, Token[] tokens) {
            visitor.visitVarInsn(opcode_,tokens[0].asVariable());
        }
    }
    
    static class JumpInsn extends Instruction {
        JumpInsn(String name, int opcode) {
            super(name, opcode);
        }
        
        void visit(MethodVisitor visitor, Token[] tokens) {            
            visitor.visitJumpInsn(opcode_,tokens[0].asLabel());
        }
    }
    
    static class TypeInsn extends Instruction {
        TypeInsn(String name, int opcode) {
            super(name, opcode);
        }
        
        void visit(MethodVisitor visitor, Token[] tokens) {
            visitor.visitTypeInsn(opcode_,tokens[0].asType());
        }
    }
    
    static class FieldInsn extends Instruction {
        FieldInsn(String name, int opcode) {
            super(name, opcode);
        }
        
        void visit(MethodVisitor visitor, Token[] tokens) {
            Field f = Field.getField(tokens);
            visitor.visitFieldInsn(opcode_,f.getOwner(),f.getName(),f.getDesc());
        }
    }
    
    static {
        new Insn("aaload",Opcodes.AALOAD);
        new Insn("aastore",Opcodes.AASTORE);
        new Insn("aconst_null",Opcodes.ACONST_NULL);
        new VarInsn("aload",Opcodes.ALOAD);
        new TypeInsn("anewarray",Opcodes.ANEWARRAY);
        new Insn("areturn",Opcodes.ARETURN);
        new Insn("arraylength",Opcodes.ARRAYLENGTH);
        new VarInsn("astore",Opcodes.ASTORE);
        new Insn("athrow",Opcodes.ATHROW);
        new Insn("baload",Opcodes.BALOAD);
        new Insn("bastore",Opcodes.BASTORE);
        new IntInsn("bipush",Opcodes.BIPUSH);
        new Insn("caload",Opcodes.CALOAD);
        new Insn("castore",Opcodes.CASTORE);
        new TypeInsn("checkcast",Opcodes.CHECKCAST);
        new Insn("d2f",Opcodes.D2F);
        new Insn("d2i",Opcodes.D2I);
        new Insn("d2l",Opcodes.D2L);
        new Insn("dadd",Opcodes.DADD);
        new Insn("daload",Opcodes.DALOAD);
        new Insn("dastore",Opcodes.DASTORE);
        new Insn("dcmpg",Opcodes.DCMPG);
        new Insn("dcmpl",Opcodes.DCMPL);
        new Insn("dconst_0",Opcodes.DCONST_0);
        new Insn("dconst_1",Opcodes.DCONST_1);
        new Insn("ddiv",Opcodes.DDIV);
        new VarInsn("dload",Opcodes.DLOAD);
        new Insn("dmul",Opcodes.DMUL);
        new Insn("dneg",Opcodes.DNEG);
        new Insn("drem",Opcodes.DREM);
        new Insn("dreturn",Opcodes.DRETURN);
        new VarInsn("dstore",Opcodes.DSTORE);
        new Insn("dsub",Opcodes.DSUB);
        new Insn("dup",Opcodes.DUP);
        new Insn("dup2",Opcodes.DUP2);
        new Insn("dup2_x1",Opcodes.DUP2_X1);
        new Insn("dup2_x2",Opcodes.DUP2_X2);
        new Insn("dup_x1",Opcodes.DUP_X1);
        new Insn("dup_x2",Opcodes.DUP_X2);
        new Insn("f2d",Opcodes.F2D);
        new Insn("f2i",Opcodes.F2I);
        new Insn("f2l",Opcodes.F2L);
        new Insn("fadd",Opcodes.FADD);
        new Insn("faload",Opcodes.FALOAD);
        new Insn("fastore",Opcodes.FASTORE);
        new Insn("fcmpg",Opcodes.FCMPG);
        new Insn("fcmpl",Opcodes.FCMPL);
        new Insn("fconst_0",Opcodes.FCONST_0);
        new Insn("fconst_1",Opcodes.FCONST_1);
        new Insn("fconst_2",Opcodes.FCONST_2);
        new Insn("fdiv",Opcodes.FDIV);
        new VarInsn("fload",Opcodes.FLOAD);
        new Insn("fmul",Opcodes.FMUL);
        new Insn("fneg",Opcodes.FNEG);
        new Insn("frem",Opcodes.FREM);
        new Insn("freturn",Opcodes.FRETURN);
        new VarInsn("fstore",Opcodes.FSTORE);
        new Insn("fsub",Opcodes.FSUB);
        new FieldInsn("getfield",Opcodes.GETFIELD);
        new FieldInsn("getstatic",Opcodes.GETSTATIC);
        new JumpInsn("goto",Opcodes.GOTO);
        new Insn("i2b",Opcodes.I2B);
        new Insn("i2c",Opcodes.I2C);
        new Insn("i2d",Opcodes.I2D);
        new Insn("i2f",Opcodes.I2F);
        new Insn("i2l",Opcodes.I2L);
        new Insn("i2s",Opcodes.I2S);
        new Insn("iadd",Opcodes.IADD);
        new Insn("iaload",Opcodes.IALOAD);
        new Insn("iand",Opcodes.IAND);
        new Insn("iastore",Opcodes.IASTORE);
        new Insn("iconst_0",Opcodes.ICONST_0);
        new Insn("iconst_1",Opcodes.ICONST_1);
        new Insn("iconst_2",Opcodes.ICONST_2);
        new Insn("iconst_3",Opcodes.ICONST_3);
        new Insn("iconst_4",Opcodes.ICONST_4);
        new Insn("iconst_5",Opcodes.ICONST_5);
        new Insn("iconst_m1",Opcodes.ICONST_M1);
        new Insn("idiv",Opcodes.IDIV);
        new JumpInsn("if_acmpeq",Opcodes.IF_ACMPEQ);
        new JumpInsn("if_acmpne",Opcodes.IF_ACMPNE);
        new JumpInsn("if_icmpeq",Opcodes.IF_ICMPEQ);
        new JumpInsn("if_icmpge",Opcodes.IF_ICMPGE);
        new JumpInsn("if_icmpgt",Opcodes.IF_ICMPGT);
        new JumpInsn("if_icmple",Opcodes.IF_ICMPLE);
        new JumpInsn("if_icmplt",Opcodes.IF_ICMPLT);
        new JumpInsn("if_icmpne",Opcodes.IF_ICMPNE);
        new JumpInsn("ifeq",Opcodes.IFEQ);
        new JumpInsn("ifge",Opcodes.IFGE);
        new JumpInsn("ifgt",Opcodes.IFGT);
        new JumpInsn("ifle",Opcodes.IFLE);
        new JumpInsn("iflt",Opcodes.IFLT);
        new JumpInsn("ifne",Opcodes.IFNE);
        new JumpInsn("ifnonnull",Opcodes.IFNONNULL);
        new JumpInsn("ifnull",Opcodes.IFNULL);
        new Iinc();
        new VarInsn("iload",Opcodes.ILOAD);
        new Insn("imul",Opcodes.IMUL);
        new Insn("ineg",Opcodes.INEG);
        new TypeInsn("instanceof",Opcodes.INSTANCEOF);
x        new Insn("invokedynamic",Opcodes.INVOKEDYNAMIC);
x        new Insn("invokeinterface",Opcodes.INVOKEINTERFACE);
x        new Insn("invokespecial",Opcodes.INVOKESPECIAL);
x        new Insn("invokestatic",Opcodes.INVOKESTATIC);
x        new Insn("invokevirtual",Opcodes.INVOKEVIRTUAL);
        new Insn("ior",Opcodes.IOR);
        new Insn("irem",Opcodes.IREM);
        new Insn("ireturn",Opcodes.IRETURN);
        new Insn("ishl",Opcodes.ISHL);
        new Insn("ishr",Opcodes.ISHR);
        new VarInsn("istore",Opcodes.ISTORE);
        new Insn("isub",Opcodes.ISUB);
        new Insn("iushr",Opcodes.IUSHR);
        new Insn("ixor",Opcodes.IXOR);
        new JumpInsn("jsr",Opcodes.JSR);
        new Insn("l2d",Opcodes.L2D);
        new Insn("l2f",Opcodes.L2F);
        new Insn("l2i",Opcodes.L2I);
        new Insn("ladd",Opcodes.LADD);
        new Insn("laload",Opcodes.LALOAD);
        new Insn("land",Opcodes.LAND);
        new Insn("lastore",Opcodes.LASTORE);
        new Insn("lcmp",Opcodes.LCMP);
        new Insn("lconst_0",Opcodes.LCONST_0);
        new Insn("lconst_1",Opcodes.LCONST_1);
x        new Insn("ldc",Opcodes.LDC);
        new Insn("ldiv",Opcodes.LDIV);
        new VarInsn("lload",Opcodes.LLOAD);
        new Insn("lmul",Opcodes.LMUL);
        new Insn("lneg",Opcodes.LNEG);
x        new Insn("lookupswitch",Opcodes.LOOKUPSWITCH);
        new Insn("lor",Opcodes.LOR);
        new Insn("lrem",Opcodes.LREM);
        new Insn("lreturn",Opcodes.LRETURN);
        new Insn("lshl",Opcodes.LSHL);
        new Insn("lshr",Opcodes.LSHR);
        new VarInsn("lstore",Opcodes.LSTORE);
        new Insn("lsub",Opcodes.LSUB);
        new Insn("lushr",Opcodes.LUSHR);
        new Insn("lxor",Opcodes.LXOR);
x        new Insn("monitorenter",Opcodes.MONITORENTER);
x        new Insn("monitorexit",Opcodes.MONITOREXIT);
x        new Insn("multianewarray",Opcodes.MULTIANEWARRAY);
        new TypeInsn("new",Opcodes.NEW);
x        new Insn("newarray",Opcodes.NEWARRAY);
        new Insn("nop",Opcodes.NOP);
        new Insn("pop",Opcodes.POP);
        new Insn("pop2",Opcodes.POP2);
        new FieldInsn("putfield",Opcodes.PUTFIELD);
        new FieldInsn("putstatic",Opcodes.PUTSTATIC);
        new VarInsn("ret",Opcodes.RET);
        new Insn("return",Opcodes.RETURN);
        new Insn("saload",Opcodes.SALOAD);
        new Insn("sastore",Opcodes.SASTORE);
        new IntInsn("sipush",Opcodes.SIPUSH);
        new Insn("swap",Opcodes.SWAP);
x        new Insn("tableswitch",Opcodes.TABLESWITCH);
    }
    
    protected final String name_;
    
    protected final int opcode_;
    
    protected Instruction(String name, int opcode) {
        name_ = name.toLowerCase();
        opcode_ = opcode;
        INSTRUCTIONS.put(name_, this);
    }
    
    abstract void visit(MethodVisitor visitor, Token[] tokens);
    
    
}
