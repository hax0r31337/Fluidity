package asm;

import org.objectweb.asm.*;

public class ClassDump implements Opcodes {

    public static byte[] dump(final String targetMain, final String targetJarPath) {
        ClassWriter cw = new ClassWriter(0);
//        FieldVisitor fv;
        MethodVisitor mv;
//        AnnotationVisitor av0;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "me/yuugiri/agent/AgentLoader", null, "java/lang/Object", null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "me/yuugiri/agent/AgentLoader", "attach", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, targetMain, "main", "([Ljava/lang/String;)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "attach", "()V", null, null);
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/management/ManagementFactory", "getRuntimeMXBean", "()Ljava/lang/management/RuntimeMXBean;", false);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/management/RuntimeMXBean", "getName", "()Ljava/lang/String;", true);
            mv.visitVarInsn(ASTORE, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, 64);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;", false);
            mv.visitVarInsn(ASTORE, 1);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, "com/sun/tools/attach/VirtualMachine", "attach", "(Ljava/lang/String;)Lcom/sun/tools/attach/VirtualMachine;", false);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(targetJarPath);
            mv.visitLdcInsn("");
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/sun/tools/attach/VirtualMachine", "loadAgent", "(Ljava/lang/String;Ljava/lang/String;)V", false);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/sun/tools/attach/VirtualMachine", "detach", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    public static byte[] dumpTransformer(final String remapperFilePath) {
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
//        AnnotationVisitor av0;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "me/yuugiri/agent/AgentTransformer", null, "java/lang/Object",
                new String[] { "java/lang/instrument/ClassFileTransformer" });

        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "methodMap", "Ljava/util/HashMap;",
                    "Ljava/util/HashMap<Ljava/lang/String;Ljava/lang/String;>;", null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "fieldMap", "Ljava/util/HashMap;",
                    "Ljava/util/HashMap<Ljava/lang/String;Ljava/lang/String;>;", null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "agentmain",
                    "(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/io/BufferedReader");
            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, "java/io/FileReader");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(remapperFilePath);
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/FileReader", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/BufferedReader", "<init>", "(Ljava/io/Reader;)V", false);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "readLine", "()Ljava/lang/String;", false);
            mv.visitVarInsn(ASTORE, 3);
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "java/io/BufferedReader", "java/lang/String" }, 0, null);
            mv.visitVarInsn(ALOAD, 3);
            Label l1 = new Label();
            mv.visitJumpInsn(IFNULL, l1);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitLdcInsn(" ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "split", "(Ljava/lang/String;)[Ljava/lang/String;",
                    false);
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ARRAYLENGTH);
            mv.visitInsn(ICONST_3);
            Label l2 = new Label();
            mv.visitJumpInsn(IF_ICMPNE, l2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(AALOAD);
            mv.visitLdcInsn("F");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            Label l3 = new Label();
            mv.visitJumpInsn(IFEQ, l3);
            mv.visitFieldInsn(GETSTATIC, "me/yuugiri/agent/AgentTransformer", "fieldMap", "Ljava/util/HashMap;");
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_2);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "[Ljava/lang/String;" }, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(AALOAD);
            mv.visitLdcInsn("M");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitFieldInsn(GETSTATIC, "me/yuugiri/agent/AgentTransformer", "methodMap", "Ljava/util/HashMap;");
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_2);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "readLine", "()Ljava/lang/String;", false);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "close", "()V", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(NEW, "me/yuugiri/agent/AgentTransformer");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "me/yuugiri/agent/AgentTransformer", "<init>", "()V", false);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/instrument/Instrumentation", "addTransformer",
                    "(Ljava/lang/instrument/ClassFileTransformer;)V", true);
            mv.visitInsn(RETURN);
            mv.visitMaxs(5, 5);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "transform", "(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B", "(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class<*>;Ljava/security/ProtectionDomain;[B)[B", null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKESPECIAL, "me/yuugiri/agent/AgentTransformer", "readClass", "([B)Lorg/objectweb/asm/tree/ClassNode;", false);
            mv.visitVarInsn(ASTORE, 6);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 7);
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"org/objectweb/asm/tree/ClassNode", Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 7);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/ClassNode", "methods", "Ljava/util/List;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
            Label l1 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, l1);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/ClassNode", "methods", "Ljava/util/List;");
            mv.visitVarInsn(ILOAD, 7);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
            mv.visitTypeInsn(CHECKCAST, "org/objectweb/asm/tree/MethodNode");
            mv.visitVarInsn(ASTORE, 8);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 9);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"org/objectweb/asm/tree/MethodNode", Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 9);
            mv.visitVarInsn(ALOAD, 8);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/MethodNode", "instructions", "Lorg/objectweb/asm/tree/InsnList;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/asm/tree/InsnList", "size", "()I", false);
            Label l3 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, l3);
            mv.visitVarInsn(ALOAD, 8);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/MethodNode", "instructions", "Lorg/objectweb/asm/tree/InsnList;");
            mv.visitVarInsn(ILOAD, 9);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/asm/tree/InsnList", "get", "(I)Lorg/objectweb/asm/tree/AbstractInsnNode;", false);
            mv.visitVarInsn(ASTORE, 10);
            mv.visitVarInsn(ALOAD, 10);
            mv.visitTypeInsn(INSTANCEOF, "org/objectweb/asm/tree/MethodInsnNode");
            Label l4 = new Label();
            mv.visitJumpInsn(IFEQ, l4);
            mv.visitVarInsn(ALOAD, 10);
            mv.visitTypeInsn(CHECKCAST, "org/objectweb/asm/tree/MethodInsnNode");
            mv.visitVarInsn(ASTORE, 11);
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/MethodInsnNode", "owner", "Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitIntInsn(BIPUSH, 47);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/MethodInsnNode", "name", "Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/MethodInsnNode", "desc", "Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitVarInsn(ASTORE, 12);
            mv.visitFieldInsn(GETSTATIC, "me/yuugiri/agent/AgentTransformer", "methodMap", "Ljava/util/HashMap;");
            mv.visitVarInsn(ALOAD, 12);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "containsKey", "(Ljava/lang/Object;)Z", false);
            Label l5 = new Label();
            mv.visitJumpInsn(IFEQ, l5);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitFieldInsn(GETSTATIC, "me/yuugiri/agent/AgentTransformer", "methodMap", "Ljava/util/HashMap;");
            mv.visitVarInsn(ALOAD, 12);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitFieldInsn(PUTFIELD, "org/objectweb/asm/tree/MethodInsnNode", "name", "Ljava/lang/String;");
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"org/objectweb/asm/tree/AbstractInsnNode"}, 0, null);
            Label l6 = new Label();
            mv.visitJumpInsn(GOTO, l6);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 10);
            mv.visitTypeInsn(INSTANCEOF, "org/objectweb/asm/tree/FieldInsnNode");
            mv.visitJumpInsn(IFEQ, l6);
            mv.visitVarInsn(ALOAD, 10);
            mv.visitTypeInsn(CHECKCAST, "org/objectweb/asm/tree/FieldInsnNode");
            mv.visitVarInsn(ASTORE, 11);
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/FieldInsnNode", "owner", "Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitIntInsn(BIPUSH, 47);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitFieldInsn(GETFIELD, "org/objectweb/asm/tree/FieldInsnNode", "name", "Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitVarInsn(ASTORE, 12);
            mv.visitFieldInsn(GETSTATIC, "me/yuugiri/agent/AgentTransformer", "fieldMap", "Ljava/util/HashMap;");
            mv.visitVarInsn(ALOAD, 12);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "containsKey", "(Ljava/lang/Object;)Z", false);
            mv.visitJumpInsn(IFEQ, l6);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitFieldInsn(GETSTATIC, "me/yuugiri/agent/AgentTransformer", "fieldMap", "Ljava/util/HashMap;");
            mv.visitVarInsn(ALOAD, 12);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitFieldInsn(PUTFIELD, "org/objectweb/asm/tree/FieldInsnNode", "name", "Ljava/lang/String;");
            mv.visitLabel(l6);
            mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
            mv.visitIincInsn(9, 1);
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_CHOP,2, null, 0, null);
            mv.visitIincInsn(7, 1);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKESPECIAL, "me/yuugiri/agent/AgentTransformer", "writeClass", "(Lorg/objectweb/asm/tree/ClassNode;)[B", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 13);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "readClass", "([B)Lorg/objectweb/asm/tree/ClassNode;", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "org/objectweb/asm/ClassReader");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "org/objectweb/asm/ClassReader", "<init>", "([B)V", false);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitTypeInsn(NEW, "org/objectweb/asm/tree/ClassNode");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "org/objectweb/asm/tree/ClassNode", "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(ICONST_0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/asm/ClassReader", "accept",
                    "(Lorg/objectweb/asm/ClassVisitor;I)V", false);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 4);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "writeClass", "(Lorg/objectweb/asm/tree/ClassNode;)[B", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "org/objectweb/asm/ClassWriter");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_1);
            mv.visitMethodInsn(INVOKESPECIAL, "org/objectweb/asm/ClassWriter", "<init>", "(I)V", false);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/asm/tree/ClassNode", "accept",
                    "(Lorg/objectweb/asm/ClassVisitor;)V", false);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/asm/ClassWriter", "toByteArray", "()[B", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
            mv.visitFieldInsn(PUTSTATIC, "me/yuugiri/agent/AgentTransformer", "methodMap", "Ljava/util/HashMap;");
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
            mv.visitFieldInsn(PUTSTATIC, "me/yuugiri/agent/AgentTransformer", "fieldMap", "Ljava/util/HashMap;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
