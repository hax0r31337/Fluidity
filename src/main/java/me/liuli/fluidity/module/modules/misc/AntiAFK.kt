package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc

class AntiAFK : Module("AntiAFK", "Prevent being kicked out in AFK", ModuleCategory.MISC) {

    private var playerRotationYaw = 0f

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        mc.gameSettings.keyBindForward.pressed = true
        playerRotationYaw += 2f
        mc.thePlayer.rotationYaw = playerRotationYaw % 360f
    }
}
