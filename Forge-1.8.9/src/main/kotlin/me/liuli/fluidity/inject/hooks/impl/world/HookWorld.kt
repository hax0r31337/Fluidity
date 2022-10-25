/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.world

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.BlockBBEvent
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.util.AxisAlignedBB

class HookWorld : HookProvider("net.minecraft.world.World") {

    @Hook(method = "getCollidingBoundingBoxes", type = Hook.Type("EXIT"))
    fun getCollidingBoundingBoxes(param: MethodHookParam) {
        Fluidity.eventManager.emit(BlockBBEvent((param.result ?: return) as ArrayList<AxisAlignedBB>))
    }
}