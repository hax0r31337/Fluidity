package me.liuli.fluidity.util.move

import me.liuli.fluidity.util.mc
import net.minecraft.client.entity.EntityPlayerSP

fun EntityPlayerSP.isMoving(): Boolean {
    return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0f || mc.thePlayer.movementInput.moveStrafe != 0f)
}