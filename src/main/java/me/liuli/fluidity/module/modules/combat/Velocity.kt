package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.network.play.server.S27PacketExplosion

class Velocity : Module("Velocity", "Prevent you from knockback", ModuleCategory.COMBAT) {

    private val modeValue = ListValue("Mode", arrayOf("Simple", "Vanilla"), "Vanilla")
    private val horizonValue = FloatValue("Horizon", 1.0f, 0.0f, 1.0f)
    private val verticalValue = FloatValue("Vertical", 1.0f, 0.0f, 1.0f)
    private val onlyDamageValue = BoolValue("OnlyDamage", false)

    private var isLastDamage = false

    @EventMethod
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (onlyDamageValue.get()) {
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

            if (modeValue.get() == "Vanilla") {
                event.cancel()
            } else if (modeValue.get() == "Simple") {
                packet.motionX = (packet.motionX * horizonValue.get()).toInt()
                packet.motionZ = (packet.motionZ * horizonValue.get()).toInt()
                packet.motionY = (packet.motionY * verticalValue.get()).toInt()
            }
        } else if (packet is S27PacketExplosion) {
            if (modeValue.get() == "Vanilla") {
                event.cancel()
            } else if (modeValue.get() == "Simple") {
                packet.field_149152_f = packet.field_149152_f * horizonValue.get()
                packet.field_149153_g = packet.field_149153_g * verticalValue.get()
                packet.field_149159_h = packet.field_149159_h * horizonValue.get()
            }
        }
    }
}