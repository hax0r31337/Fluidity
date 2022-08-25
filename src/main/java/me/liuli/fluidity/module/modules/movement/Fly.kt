package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.direction
import me.liuli.fluidity.util.move.strafe
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class Fly : Module("Fly", "Make you like a bird", ModuleCategory.MOVEMENT) {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "Matrix"), "Vanilla")
    private val hSpeedValue = FloatValue("HorizonSpeed", 1.0f, 0.0f, 5.0f)
    private val vSpeedValue = FloatValue("VerticalSpeed", 0.5f, 0.0f, 5.0f)
    private val noClipValue = BoolValue("NoClip", false)

    private var recordPosX = 0.0
    private var recordPosY = 0.0
    private var recordPosZ = 0.0
    private var acceptedPk = true
    private var boostMotion = 0
    private var launchY = 0.0
    private val packetBuffer = mutableListOf<C03PacketPlayer>()

    override fun onEnable() {
        boostMotion = 0
        packetBuffer.clear()
        acceptedPk = true
        launchY = mc.thePlayer?.posY ?: 0.0
    }

    override fun onDisable() {
        if (noClipValue.get()) {
            mc.thePlayer.noClip = false
        }
        mc.timer.timerSpeed = 1f
    }

    @Listen
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
            "Matrix" -> {
                if (boostMotion == 0) {
                    val yaw = mc.thePlayer.direction
                    mc.thePlayer.motionX = -sin(yaw) * hSpeedValue.get()
                    mc.thePlayer.motionZ = cos(yaw) * hSpeedValue.get()
                    mc.thePlayer.motionY = vSpeedValue.get().toDouble()
//                    mc.timer.timerSpeed = 0.1f
                    recordPosX = mc.thePlayer.posX
                    recordPosY = mc.thePlayer.posY
                    recordPosZ = mc.thePlayer.posZ
                    packetBuffer.add(C03PacketPlayer.C06PacketPlayerPosLook(recordPosX, recordPosY, recordPosZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                } else if (boostMotion > 10) {
                    boostMotion = -1
                } else if (boostMotion == 1) {
                    acceptedPk = false
                    mc.thePlayer.setPosition(recordPosX, recordPosY, recordPosZ)
                    boostMotion++
                }

                if (acceptedPk) {
                    boostMotion++
                }
            }
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (modeValue.get() == "Matrix" && mc.currentScreen == null) {
            if (packet is S08PacketPlayerPosLook) {
                acceptedPk = true
                if (packet.x != recordPosX && packet.y != recordPosY || packet.z != recordPosZ) {
                    mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
                } else {
                    displayAlert("X ${recordPosX -packet.x} Y ${recordPosY - packet.y} Z ${recordPosZ - packet.z}")
//                mc.timer.timerSpeed = 1f
                    event.cancel()
                    packetBuffer.forEach {
                        mc.netHandler.addToSendQueue(it)
                    }
                }
                packetBuffer.clear()
            } else if (packet is C03PacketPlayer && !acceptedPk) {
                event.cancel()
                packetBuffer.add(packet)
            }
        }
    }
}