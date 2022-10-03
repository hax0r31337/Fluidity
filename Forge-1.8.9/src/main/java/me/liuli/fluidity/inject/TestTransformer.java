package me.liuli.fluidity.inject;

import me.liuli.fluidity.util.other.ASMUtils;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.ClassNode;

public class TestTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        final ClassNode classNode = ASMUtils.toClassNode(basicClass);
        System.out.println(classNode.name);
        return basicClass;
    }
}
