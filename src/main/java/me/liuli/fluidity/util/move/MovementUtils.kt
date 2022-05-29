package me.liuli.fluidity.util.move

import net.minecraft.client.entity.EntityPlayerSP
import kotlin.math.cos
import kotlin.math.sin

fun EntityPlayerSP.isMoving(): Boolean {
    return this.movementInput.moveForward != 0f || this.movementInput.moveStrafe != 0f
}

val EntityPlayerSP.direction: Double
    get() {
        var rotationYaw = this.rotationYaw
        if (this.moveForward < 0f) rotationYaw += 180f
        var forward = 1f
        if (this.moveForward < 0f) forward = -0.5f else if (this.moveForward > 0f) forward = 0.5f
        if (this.moveStrafing > 0f) rotationYaw -= 90f * forward
        if (this.moveStrafing < 0f) rotationYaw += 90f * forward
        return Math.toRadians(rotationYaw.toDouble())
    }

fun EntityPlayerSP.strafe(value: Float) {
    if (!this.isMoving()) {
        this.motionX = 0.0
        this.motionZ = 0.0
        return
    }

    val yaw = direction
    this.motionX -= sin(yaw) * value
    this.motionZ += cos(yaw) * value
}