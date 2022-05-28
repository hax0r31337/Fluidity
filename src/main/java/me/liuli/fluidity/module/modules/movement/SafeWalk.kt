package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc

import net.minecraft.util.BlockPos
import net.minecraft.init.Blocks

class SafeWalk : Module("SafeWalk", "Automatically make you walk safely", ModuleCategory.MOVEMENT) {

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        val underBlock = mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block
        mc.gameSettings.keyBindSneak.pressed = (underBlock == Blocks.air)
    }
}
