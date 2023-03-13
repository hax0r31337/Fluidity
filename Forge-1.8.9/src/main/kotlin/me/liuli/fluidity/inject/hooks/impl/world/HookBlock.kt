/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.world

import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.module.modules.misc.BetterButton
import me.liuli.fluidity.module.modules.world.FastMine
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.block.Block
import net.minecraft.block.BlockButton
import net.minecraft.block.BlockLever

class HookBlock : HookProvider("net.minecraft.block.Block") {

    @Hook(method = "setBlockBounds", type = Hook.Type("ENTER"))
    fun setBlockBounds(param: MethodHookParam) {
        if (BetterButton.state) {
            val block = param.thisObject as Block
            if (block is BlockButton || block is BlockLever) {
                block.apply {
                    minX = 0.0
                    minY = 0.0
                    minZ = 0.0
                    maxX = 1.0
                    maxY = 1.0
                    maxZ = 1.0
                }
                param.result = null
            }
        }
    }

    @Hook(method = "getPlayerRelativeBlockHardness", type = Hook.Type("EXIT"))
    fun getPlayerRelativeBlockHardness(param: MethodHookParam) {
        if (FastMine.state) {
            param.result = FastMine.multiplier.get() * (param.result as Float)
        }
    }

}