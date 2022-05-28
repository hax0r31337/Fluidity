package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc

class Sprint : Module("Sprint", "Automatically make you sprint", ModuleCategory.MOVEMENT) {

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.isSprinting = true
    }
}
