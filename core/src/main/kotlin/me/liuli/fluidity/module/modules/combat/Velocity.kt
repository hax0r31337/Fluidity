/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.direction
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.network.play.server.S27PacketExplosion
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class Velocity : Module("Velocity", "Prevent you from knockback", ModuleCategory.COMBAT) {

    private val modeValue by ListValue("Mode", arrayOf("Simple", "Vanilla", "Strafe"), "Vanilla")
    private val horizonValue by FloatValue("Horizon", 1.0f, 0.0f, 1.0f)
    private val verticalValue by FloatValue("Vertical", 1.0f, 0.0f, 1.0f)
    private val onlyDamageValue by BoolValue("OnlyDamage", false)

    private var isLastDamage = false

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (onlyDamageValue) {
            if ((packet is S0BPacketAnimation && packet.animationType == 1 && packet.entityID == mc.thePlayer.entityId)
                || (packet is S19PacketEntityStatus && packet.opCode.toInt() == 2 && packet.getEntity(mc.theWorld) == mc.thePlayer)) {
                isLastDamage = true
            } else if (packet is S12PacketEntityVelocity || packet is S27PacketExplosion) {
                if (isLastDamage) {
                    isLastDamage = false
                } else {
                    return
                }
            }
        }

        if (packet is S12PacketEntityVelocity) {
            if (packet.entityID != mc.thePlayer.entityId) {
                return
            }

            if (modeValue == "Vanilla") {
                event.cancel()
            } else if (modeValue == "Simple") {
                packet.motionX = (packet.motionX * horizonValue).toInt()
                packet.motionZ = (packet.motionZ * horizonValue).toInt()
                packet.motionY = (packet.motionY * verticalValue).toInt()
            } else if (modeValue == "Strafe") {
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                displayAlert("${packet.motionZ} ${(-sin(yaw) * packet.motionX).toInt()}")
                packet.motionX = (-sin(yaw) * abs(packet.motionX)).toInt()
                packet.motionZ = (cos(yaw) * abs(packet.motionZ)).toInt()
            }
        } else if (packet is S27PacketExplosion) {
            if (modeValue == "Vanilla") {
                event.cancel()
            } else if (modeValue == "Simple") {
                packet.field_149152_f = packet.field_149152_f * horizonValue
                packet.field_149153_g = packet.field_149153_g * verticalValue
                packet.field_149159_h = packet.field_149159_h * horizonValue
            }
        }
    }
}