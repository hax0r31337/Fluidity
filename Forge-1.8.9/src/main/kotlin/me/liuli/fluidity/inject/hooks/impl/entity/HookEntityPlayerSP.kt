/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.entity

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.*
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.MethodProcess
import me.liuli.fluidity.util.move.RotationUtils
import me.liuli.fluidity.util.other.asmName
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.MethodHookParam
import me.yuugiri.hutil.util.forEach
import net.minecraft.client.entity.EntityPlayerSP
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

object HookEntityPlayerSP : HookProvider("net.minecraft.client.entity.EntityPlayerSP") {

    @JvmField
    var slowdown = 0.2f

    @Hook(method = "onUpdateWalkingPlayer", type = Hook.Type("ENTER"))
    fun onUpdateWalkingPlayerEnter() {
        Fluidity.eventManager.emit(PreMotionEvent())
    }

    @Hook(method = "onUpdateWalkingPlayer", type = Hook.Type("EXIT"))
    fun onUpdateWalkingPlayerExit() {
        // update rotations
        RotationUtils.applyVisualYawUpdate()
        RotationUtils.flushLastReported()

        Fluidity.eventManager.emit(PostMotionEvent())
    }

    @MethodProcess(method = "onUpdateWalkingPlayer")
    fun getMouseOver(obfuscationMap: AbstractObfuscationMap?, method: MethodNode) {
        var hasHooked = 0
        val entityClassName = AbstractObfuscationMap.classObfuscationRecordReverse(obfuscationMap, "net/minecraft/entity/Entity").obfuscatedName
        method.instructions.forEach {
            if (it !is FieldInsnNode) return@forEach
            val obf = AbstractObfuscationMap.fieldObfuscationRecord(obfuscationMap, entityClassName, it.name) // TODO: use reverse obfmap check
            if (obf.name != "rotationYaw" && obf.name != "rotationPitch") return@forEach
            method.instructions.insertBefore(it, InsnNode(Opcodes.POP)) // pop "this" on stack
            method.instructions.insertBefore(it, MethodInsnNode(Opcodes.INVOKESTATIC, RotationUtils::class.java.asmName, obf.name, "()F", false))
            method.instructions.remove(it)
            hasHooked++
        }
        if (hasHooked < 2) throw UnknownError("unable to found hook point for net.minecraft.client.entity.EntityPlayerSP.onUpdateWalkingPlayer()V")
    }

    @Hook(method = "onLivingUpdate", type = Hook.Type("ENTER"))
    fun onLivingUpdate(param: MethodHookParam) {
        Fluidity.eventManager.emit(UpdateEvent())

        val player = param.thisObject as EntityPlayerSP
        // call SlowDownEvent and get value
        if (player.isUsingItem && !player.isRiding) {
            val slowDownEvent = SlowDownEvent(0.2f)
            Fluidity.eventManager.emit(slowDownEvent)
            slowdown = slowDownEvent.percentage
        }
    }

    @MethodProcess(method = "onLivingUpdate")
    fun onLivingUpdate(obfuscationMap: AbstractObfuscationMap?, method: MethodNode) {
        var hasHooked = false
        method.instructions.forEach {
            if (it !is LdcInsnNode || it.cst != 0.2f) return@forEach
            method.instructions.insert(it, FieldInsnNode(Opcodes.GETSTATIC, HookEntityPlayerSP::class.java.asmName, "slowdown", "F"))
            method.instructions.remove(it)
            hasHooked = true
        }
        if (!hasHooked) throw UnknownError("unable to found hook point for net.minecraft.client.entity.EntityPlayerSP.onLivingUpdate()V")
    }
}