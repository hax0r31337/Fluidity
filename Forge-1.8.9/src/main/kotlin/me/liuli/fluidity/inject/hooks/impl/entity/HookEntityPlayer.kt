/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.entity

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.module.modules.movement.KeepSprint
import me.yuugiri.hutil.processor.hook.EnumHookShift
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.entity.player.EntityPlayer

class HookEntityPlayer : HookProvider("net.minecraft.entity.player.EntityPlayer") {

    @Hook(method = "attackTargetEntityWithCurrentItem", type = Hook.Type("INVOKE", "net/minecraft/entity/player/EntityPlayer;setSprinting(Z)V", "net/minecraft/entity/EntityLivingBase"), shift = EnumHookShift.AFTER)
    fun attackTargetEntityWithCurrentItem(param: MethodHookParam) {
        val thisObject = param.thisObject as EntityPlayer

        if (KeepSprint.state) {
            val multiplier = 0.6f + 0.4f * KeepSprint.multiplierValue.get()
            thisObject.motionX = thisObject.motionX / 0.6 * multiplier
            thisObject.motionZ = thisObject.motionZ / 0.6 * multiplier
            if (KeepSprint.noBreakSprintValue.get()) {
                thisObject.isSprinting = true
            }
        }
    }
}