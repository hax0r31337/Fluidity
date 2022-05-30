package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc

object NoJumpDelay : Module("NoJumpDelay", "Removes delay between jumps", ModuleCategory.MISC) {

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.jumpTicks = 0
    }
}