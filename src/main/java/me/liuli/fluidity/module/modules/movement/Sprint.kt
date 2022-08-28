package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.pathfinder.Pathfinder
import me.liuli.fluidity.util.mc

class Sprint : Module("Sprint", "Automatically make you sprint", ModuleCategory.MOVEMENT) {

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (!Pathfinder.hasPath) {
            mc.thePlayer.isSprinting = true
        }
    }
}
