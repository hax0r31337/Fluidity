package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.*
import me.liuli.fluidity.util.other.random
import me.liuli.fluidity.util.world.getDistanceToEntityBox

class AimBot : Module("AimBot", "Helps you aim another player", ModuleCategory.COMBAT) {

    private val rangeValue = FloatValue("Range", 4.5f, 1f, 10f)
    private val turnSpeedValue = FloatValue("TurnSpeed", 10f, 1F, 180F)
    private val inViewTurnSpeed = FloatValue("InViewTurnSpeed", 35f, 1f, 180f)
    private val fovValue = FloatValue("FOV", 180F, 1F, 180F)
    private val onlyHoldMouseValue = BoolValue("OnlyHoldMouse", true)
    private val silentRotationValue = BoolValue("SilentRotation", false)

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        if (onlyHoldMouseValue.get() && !mc.gameSettings.keyBindAttack.isKeyDown) return

        val entity = mc.theWorld.loadedEntityList
            .filter {
                Targets.isTarget(it, true) && mc.thePlayer.canEntityBeSeen(it) &&
                        mc.thePlayer.getDistanceToEntityBox(it) <= rangeValue.get() && getRotationDifference(it) <= fovValue.get()
            }
            .minByOrNull { getRotationDifference(it) } ?: return

        val boundingBox = entity.entityBoundingBox ?: return

        val destinationRotation = toRotation(getCenter(boundingBox), true)

        // Figure out the best turn speed suitable for the distance and configured turn speed

        val rotationDiff = getRotationDifference(mc.thePlayer.serverRotationYaw, mc.thePlayer.serverRotationPitch, destinationRotation.first, destinationRotation.second)

        // is enemy visible to player on screen. Fov is about to be right with that you can actually see on the screen. Still not 100% accurate, but it is fast check.
        val supposedTurnSpeed = if (rotationDiff < mc.gameSettings.fovSetting) {
            inViewTurnSpeed.get()
        } else {
            turnSpeedValue.get()
        }
        val gaussian = random.nextGaussian()
        val realisticTurnSpeed = rotationDiff * ((supposedTurnSpeed + (gaussian - 0.5)) / 180)
        val targetRotation = limitAngleChange(mc.thePlayer.serverRotationYaw, mc.thePlayer.serverRotationPitch, destinationRotation.first, destinationRotation.second, realisticTurnSpeed.toFloat())

        if (silentRotationValue.get()) {
            setServerRotation(targetRotation.first, targetRotation.second)
        } else {
            mc.thePlayer.setPositionAndRotation(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, targetRotation.first, targetRotation.second)
        }
    }
}