/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.processor

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import me.yuugiri.hutil.util.forEach
import me.yuugiri.hutil.util.methods_
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode

class OptimizeProcessor : IClassProcessor {

    private val transformMap = mapOf("net/minecraft/util/EnumFacing" to "facings",
        "net/minecraft/util/EnumChatFormatting" to "chatFormatting",
        "net/minecraft/util/EnumParticleTypes" to "particleTypes",
        "net/minecraft/util/EnumWorldBlockLayer" to "worldBlockLayers")

    override fun selectClass(name: String) = name.startsWith("net/minecraft")

    override fun processClass(obfuscationMap: AbstractObfuscationMap?, map: AbstractObfuscationMap.ClassObfuscationRecord, klass: ClassNode): Boolean {
        if (transformMap.containsKey(map.name)) return false
        var hasChanged = false

        klass.methods_.forEach { method ->
             method.instructions.forEach forEach1@ { node ->
                 if (node !is MethodInsnNode) return@forEach1
                 if (node.opcode != Opcodes.INVOKESTATIC) return@forEach1
                 if (node.name == "values") {
                     var owner = node.owner
                     if (transformMap.containsKey(node.owner)
                         || transformMap.containsKey(AbstractObfuscationMap.classObfuscationRecord(obfuscationMap, node.owner).name.also { owner = it })) {
                         method.instructions.insert(node, FieldInsnNode(Opcodes.GETSTATIC, "me/liuli/fluidity/inject/StaticStorage", transformMap[owner]!!, node.desc.substring(node.desc.indexOf(')')+1)))
                         method.instructions.remove(node)
                         hasChanged = true
                     }
                 } else if (node.owner == "org/lwjgl/opengl/Display" && node.name == "setTitle") {
                     node.owner = "me/liuli/fluidity/inject/StaticStorage"
                     hasChanged = true
                 } else if (node.owner == "java/lang/System" && node.name == "gc") {
                     method.instructions.insert(node, InsnNode(Opcodes.NOP))
                     method.instructions.remove(node)
                     hasChanged = true
                 }
            }
        }

        return hasChanged
    }
}