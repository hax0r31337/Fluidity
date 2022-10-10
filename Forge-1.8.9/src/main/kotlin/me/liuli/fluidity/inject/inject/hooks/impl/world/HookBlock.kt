package me.liuli.fluidity.inject.inject.hooks.impl.world

import me.liuli.fluidity.inject.inject.hooks.AbstractHookProvider
import me.liuli.fluidity.inject.inject.hooks.Hook
import me.liuli.fluidity.module.modules.misc.BetterButton
import me.liuli.fluidity.module.modules.world.FastMine
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.block.Block
import net.minecraft.block.BlockButton
import net.minecraft.block.BlockLever

class HookBlock : AbstractHookProvider("net.minecraft.block.Block") {

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
        if (param.result is Float && FastMine.state) {
            param.result = FastMine.multiplier.get() * (param.result as Float)
        }
    }

}