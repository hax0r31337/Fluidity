package me.liuli.fluidity.inject.inject.processor

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import me.yuugiri.hutil.util.forEach
import me.yuugiri.hutil.util.methods_
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

class OptimizeProcessor : IClassProcessor {

    private val transformMap = mapOf("net/minecraft/util/EnumFacing" to "facings",
        "net/minecraft/util/EnumChatFormatting" to "chatFormatting",
        "net/minecraft/util/EnumParticleTypes" to "particleTypes",
        "net/minecraft/util/EnumBlockLayer" to "worldBlockLayers")

    override fun selectClass(name: String) = name.startsWith("net/minecraft")

    override fun processClass(obfuscationMap: AbstractObfuscationMap?, map: AbstractObfuscationMap.ClassObfuscationRecord, klass: ClassNode): Boolean {
        var hasChanged = false

        klass.methods_.forEach { method ->
             method.instructions.forEach forEach1@ { node ->
                 if (node !is MethodInsnNode) return@forEach1
                 if (node.opcode != Opcodes.INVOKESTATIC) return@forEach1
                 if (node.name == "values") {
                     var owner = node.owner
                     if (transformMap.containsKey(node.owner)
                         || transformMap.containsKey(AbstractObfuscationMap.classObfuscationRecord(obfuscationMap, node.owner).name.also { owner = it })) {
                         node.owner = "me/liuli/fluidity/inject/StaticStorage"
                         node.name = transformMap[owner]!!
                         hasChanged = true
                     }
                 } else if (node.owner == "org/lwjgl/opengl/Display" && node.name == "setTitle") {
                     node.owner = "me/liuli/fluidity/inject/StaticStorage"
                     hasChanged = true
                 } else if (node.owner == "java/lang/System" && node.name == "gc") {
                     node.owner = "me/liuli/fluidity/inject/StaticStorage"
                     node.name = "dummy"
                     hasChanged = true
                 }
            }
        }

        return hasChanged
    }
}