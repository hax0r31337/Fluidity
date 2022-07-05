package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.util.mc

class GameSpeed : Module("GameSpeed", "Change Minecraft tick speed", ModuleCategory.WORLD) {

    private val speedValue = FloatValue("Speed", 1.5f, 0.1f, 5f)

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = speedValue.get()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}