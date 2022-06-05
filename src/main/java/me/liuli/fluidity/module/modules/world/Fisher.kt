package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.client.PacketUtils
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.client.realMotionX
import me.liuli.fluidity.util.client.realMotionY
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.other.inRange
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S29PacketSoundEffect

class Fisher : Module("Fisher", "Automatically fishing", ModuleCategory.WORLD) {

    private val detectionValue = ListValue("Detection", arrayOf("Motion", "Sound"), "Sound")
    private val recastValue = BoolValue("Recast", true)
    private val recastDelayValue = IntValue("RecastDelay", 500, 0, 1000)

    private var stage = Stage.NOTHING
    private val recastTimer = TheTimer()

    override fun onDisable() {
        stage = Stage.NOTHING
    }

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        if (stage == Stage.RECOVERING) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            stage = if (recastValue.get()) {
                recastTimer.reset()
                Stage.RECASTING
            } else {
                Stage.NOTHING
            }
            return
        } else if (stage == Stage.RECASTING && recastTimer.hasTimePassed(recastDelayValue.get())) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            stage = Stage.NOTHING
        }
    }

    @EventMethod
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (detectionValue.get() == "Sound" && packet is S29PacketSoundEffect && mc.thePlayer?.fishEntity != null
            && packet.soundName == "random.splash" && packet.x.inRange(mc.thePlayer.fishEntity.posX, 1.5) && packet.z.inRange(mc.thePlayer.fishEntity.posZ, 1.5)) {
            recoverFishRod()
        } else if (detectionValue.get() == "Motion" && packet is S12PacketEntityVelocity && mc.thePlayer?.fishEntity != null
            && packet.entityID == mc.thePlayer.fishEntity.entityId && packet.motionX == 0 && packet.motionY != 0 && packet.motionZ == 0) {
            recoverFishRod()
        }
    }

    private fun recoverFishRod() {
        displayAlert("Recovering fish rod")
        if (stage != Stage.NOTHING) {
            return
        }

        stage = Stage.RECOVERING
    }

    private enum class Stage {
        NOTHING,
        RECOVERING,
        RECASTING
    }
}