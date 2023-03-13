/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.entity

import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.module.modules.render.NoFOV
import me.liuli.fluidity.module.modules.render.NoFOV.fovValue
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.init.Items

class HookAbstractClientPlayer : HookProvider("net.minecraft.client.entity.AbstractClientPlayer") {

    @Hook(method = "getFovModifier", type = Hook.Type("ENTER"))
    fun getFovModifier(param: MethodHookParam) {
        if (NoFOV.state) {
            var newFOV = fovValue
            val thisObject = param.thisObject as AbstractClientPlayer

            if (!thisObject.isUsingItem || thisObject.itemInUse.item !== Items.bow) {
                param.result = newFOV
            } else {
                val i = thisObject.itemInUseDuration
                var f1 = i.toFloat() / 20.0f
                f1 = if (f1 > 1.0f) 1.0f else f1 * f1
                newFOV *= 1.0f - f1 * 0.15f
                param.result = newFOV
            }
        }
    }
}