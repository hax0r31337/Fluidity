/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.net

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.MethodProcess
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.util.forEach
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodNode

class HookNettyCompressionDecoder : HookProvider("net.minecraft.network.NettyCompressionDecoder") {


    /**
     * prevent netty overflow kick (chunkban, bookban, etc.)
     */
    @MethodProcess(method = "decode")
    fun getMouseOver(obfuscationMap: AbstractObfuscationMap?, method: MethodNode) {
        var hasHooked = false
        method.instructions.forEach { node ->
            if (node !is InsnNode || node.opcode != Opcodes.ATHROW) return@forEach
            method.instructions.insert(node, InsnNode(Opcodes.POP))
            method.instructions.remove(node)
            hasHooked = true
        }
        if (!hasHooked) throw UnknownError("unable to found hook point for net.minecraft.network.NettyCompressionDecoder.decode")
    }
}