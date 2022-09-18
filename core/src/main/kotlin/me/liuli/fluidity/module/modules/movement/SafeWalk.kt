package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class SafeWalk : Module("SafeWalk", "Automatically make you walk safely", ModuleCategory.MOVEMENT) {

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = false
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        val underBlock = mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block
        mc.gameSettings.keyBindSneak.pressed = (underBlock == Blocks.air) && mc.thePlayer.onGround
    }
}
