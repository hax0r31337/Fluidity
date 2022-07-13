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
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class Fly : Module("Fly", "Make you like a bird", ModuleCategory.MOVEMENT) {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "Matrix"), "Vanilla")
    private val hSpeedValue = FloatValue("HorizonSpeed", 1.0f, 0.0f, 5.0f)
    private val vSpeedValue = FloatValue("VerticalSpeed", 0.5f, 0.0f, 5.0f)
    private val noClipValue = BoolValue("NoClip", false)

    private var boostMotion = 0
    private var launchY = 0.0

    override fun onEnable() {
        boostMotion = 0
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
//                    displayAlert("REQ ${mc.thePlayer.posX} ${mc.thePlayer.posY} ${mc.thePlayer.posZ}")
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + -sin(yaw) * 1.5, mc.thePlayer.posY + 1, mc.thePlayer.posZ + cos(yaw) * 1.5, false))
                    boostMotion = 1
                } else if (boostMotion == 1) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionZ = 0.0
                } else if (boostMotion == 2) {
                    mc.thePlayer.strafe(hSpeedValue.get()/*1.95f*/)
//                    mc.thePlayer.motionX = -sin(mc.thePlayer.rotationYaw) * 1.6
//                    mc.thePlayer.motionZ = cos(mc.thePlayer.rotationYaw) * 1.6
                    mc.thePlayer.motionY = vSpeedValue.get().toDouble()/*0.65*/
                    boostMotion = 3
                    mc.timer.timerSpeed = 0.15f
//                    displayAlert("SET")
                } else {
                    boostMotion++
                    mc.timer.timerSpeed = 1f
//                    displayAlert("UNSET")
                    if (mc.thePlayer.posY < launchY && boostMotion > 10) {
                        boostMotion = 0
                    }
                }
            }
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (modeValue.get() == "Matrix" && mc.currentScreen == null && packet is S08PacketPlayerPosLook) {
            if (boostMotion == 1) {
                boostMotion = 2
            }
            mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
//            displayAlert("FLAG ${packet.x} ${packet.y} ${packet.z}")
            mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, false))
            event.cancel()
        } else if (modeValue.get() == "Matrix" && mc.currentScreen == null && packet is C03PacketPlayer && boostMotion == 1) {
            event.cancel()
        }
    }
}