package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.direction
import me.liuli.fluidity.util.move.isMoving
import me.liuli.fluidity.util.move.strafe
import kotlin.math.cos
import kotlin.math.sin

class Fly : Module("Fly", "Make you like a bird", ModuleCategory.MOVEMENT) {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla"), "Vanilla")
    private val hSpeedValue = FloatValue("HorizonSpeed", 1.0f, 0.0f, 5.0f)
    private val vSpeedValue = FloatValue("VerticalSpeed", 0.5f, 0.0f, 5.0f)
    private val noClipValue = BoolValue("NoClip", false)

    override fun onDisable() {
        if (noClipValue.get()) {
            mc.thePlayer.noClip = false
        }
    }

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        if (noClipValue.get()) {
            mc.thePlayer.noClip = true
        }
        mc.thePlayer.fallDistance = 0f
        when (modeValue.get()) {
            "Vanilla" -> {
                if (mc.gameSettings.keyBindJump.pressed) {
                    mc.thePlayer.motionY = vSpeedValue.get().toDouble()
                } else if (mc.gameSettings.keyBindSneak.pressed) {
                    mc.thePlayer.motionY = -vSpeedValue.get().toDouble()
                } else {
                    mc.thePlayer.motionY = 0.0
                }
                mc.thePlayer.strafe(hSpeedValue.get())
            }
        }
    }
}