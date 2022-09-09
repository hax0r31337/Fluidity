package me.liuli.fluidity.inject.transformer;

import me.liuli.fluidity.util.other.ASMUtils;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This transformer removes the method call that not supported in java 8
 */
public class SkiaTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass != null && name.startsWith("org.jetbrains.ski")) {
            try {
                final ClassNode classNode = ASMUtils.toClassNode(basicClass);
                AtomicBoolean changed = new AtomicBoolean(false);

                classNode.methods.forEach(methodNode -> {
                    for (int i = 0; i < methodNode.instructions.size(); ++i) {
                        final AbstractInsnNode abstractInsnNode = methodNode.instructions.get(i);
                        if (abstractInsnNode instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) abstractInsnNode;
                            if(min.name.equals("reachabilityFence")) {
                                min.owner = "me/liuli/fluidity/inject/StaticStorage";
                                changed.set(true);
                            }
                        }
                    }
                });

                if (changed.get()) {
                    return ASMUtils.toBytes(classNode);
                }
            }catch(final Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return basicClass;
    }
}
