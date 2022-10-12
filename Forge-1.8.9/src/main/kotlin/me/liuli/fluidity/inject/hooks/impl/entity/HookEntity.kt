package me.liuli.fluidity.inject.hooks.impl.entity

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.MoveEvent
import me.liuli.fluidity.event.StepEvent
import me.liuli.fluidity.event.StrafeEvent
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.MethodProcess
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.module.modules.combat.HitBox
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.other.asmName
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.MethodHookParam
import me.yuugiri.hutil.util.forEach
import net.minecraft.entity.Entity
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

object HookEntity : HookProvider("net.minecraft.entity.Entity") {

    @Hook(method = "moveFlying", type = Hook.Type("ENTER"))
    fun moveFlying(param: MethodHookParam) {
        if (param.thisObject != mc.thePlayer)
            return

        val strafeEvent = StrafeEvent(param.args[0] as Float, param.args[1] as Float, param.args[2] as Float)
        Fluidity.eventManager.call(strafeEvent)

        if (strafeEvent.cancelled)
            param.result = null
    }

    @Hook(method = "getCollisionBorderSize", type = Hook.Type("ENTER"))
    fun getCollisionBorderSize(param: MethodHookParam) {
        if (HitBox.state && (param.thisObject as Entity).isTarget(true))
            param.result = 0.1f + HitBox.sizeValue.get()
    }

    @Hook(method = "moveEntity", type = Hook.Type("ENTER"))
    fun moveEntity(param: MethodHookParam) {
        if (param.thisObject != mc.thePlayer)
            return

        val moveEvent = MoveEvent(param.args[0] as Double, param.args[1] as Double, param.args[2] as Double)
        Fluidity.eventManager.call(moveEvent)

        if (moveEvent.cancelled)
            param.result = null

        param.args[0] = moveEvent.x
        param.args[1] = moveEvent.y
        param.args[2] = moveEvent.z
    }

    @MethodProcess(method = "moveEntity")
    fun moveEntity(obfuscationMap: AbstractObfuscationMap?, method: MethodNode) {
        var hasHooked = false
        method.instructions.forEach {
            if (it !is FieldInsnNode) return@forEach
            val obf = AbstractObfuscationMap.fieldObfuscationRecord(obfuscationMap, it.owner, it.name)
            if (obf.name != "stepHeight") return@forEach
            hasHooked = true
            method.instructions.insert(it, MethodInsnNode(Opcodes.INVOKESTATIC, HookEntity::class.java.asmName, obf.name, "(F)F", false))
        }
        if (!hasHooked) throw UnknownError("unable to found hook point for net.minecraft.entity.Entity.moveEntity(DDD)V")
    }

    @JvmStatic
    fun stepHeight(stepHeightIn: Float): Float {
        val stepEvent = StepEvent(stepHeightIn)
        Fluidity.eventManager.call(stepEvent)
        return stepEvent.stepHeight
    }
}