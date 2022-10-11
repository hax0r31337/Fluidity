package me.liuli.fluidity.inject.hooks.impl.entity

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.module.modules.movement.KeepSprint
import me.liuli.fluidity.module.modules.movement.KeepSprint.multiplierValue
import me.liuli.fluidity.module.modules.movement.KeepSprint.noBreakSprintValue
import me.yuugiri.hutil.processor.hook.EnumHookShift
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.entity.player.EntityPlayer

class HookEntityPlayer : HookProvider("net.minecraft.entity.player.EntityPlayer") {

    @Hook(method = "onAttackTargetEntityWithCurrentItem", type = Hook.Type("INVOKE", "net/minecraft/entity/player/EntityPlayer;setSprinting(Z)V"), shift = EnumHookShift.AFTER)
    fun onAttackTargetEntityWithCurrentItem(param: MethodHookParam) {
        val thisObject = param.thisObject as EntityPlayer

        if (KeepSprint.state) {
            val multiplier = 0.6f + 0.4f * multiplierValue.get()
            thisObject.motionX = thisObject.motionX / 0.6 * multiplier
            thisObject.motionZ = thisObject.motionZ / 0.6 * multiplier
            if (noBreakSprintValue.get()) {
                thisObject.isSprinting = true
            }
        }
    }
}