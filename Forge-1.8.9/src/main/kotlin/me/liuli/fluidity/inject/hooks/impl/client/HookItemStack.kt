/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.client

import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.module.special.CombatManager
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.item.EnumAction

class HookItemStack : HookProvider("net.minecraft.item.ItemStack") {

    @Hook(method = "getItemUseAction", type = Hook.Type("ENTER"))
    fun getItemUseAction(param: MethodHookParam) {
        if (CombatManager.isPacketBlocking) {
            param.result = EnumAction.BLOCK
        }
    }
}