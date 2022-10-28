/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.render

import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.util.mc
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.item.ItemSword

class HookItemRenderer : HookProvider("net.minecraft.client.renderer.ItemRenderer") {

    @Hook(method = "transformFirstPersonItem", type = Hook.Type("ENTER"))
    fun transformFirstPersonItem(param: MethodHookParam) {
        if (mc.thePlayer.itemInUseCount != 0 && mc.thePlayer.heldItem?.item is ItemSword) {
            param.args[0] = 0.2f
            param.args[1] = mc.thePlayer.getSwingProgress(mc.timer.renderPartialTicks)
        }
    }
}