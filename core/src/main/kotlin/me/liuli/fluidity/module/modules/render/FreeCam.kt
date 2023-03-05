/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.strafe
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class FreeCam : Module("FreeCam", "Allows you to move out of your body", ModuleCategory.RENDER) {

    private val speedValue by FloatValue("Speed", 0.8f, 0.1f, 2f)

    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0
    private var lastFlag = false

    override fun onEnable() {
        if (mc.thePlayer == null) {
            state = false
            return
        }

        posX = mc.thePlayer.posX
        posY = mc.thePlayer.posY
        posZ = mc.thePlayer.posZ
        motionX = mc.thePlayer.motionX
        motionY = mc.thePlayer.motionY
        motionZ = mc.thePlayer.motionZ
        lastFlag = false
    }

    override fun onDisable() {
        mc.thePlayer ?: return
        mc.thePlayer.setPosition(posX, posY, posZ)
        mc.thePlayer.motionX = motionX
        mc.thePlayer.motionY = motionY
        mc.thePlayer.motionZ = motionZ
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.noClip = true
        mc.thePlayer.fallDistance = 0f
        if (mc.gameSettings.keyBindJump.pressed) {
            mc.thePlayer.motionY = speedValue.toDouble()
        } else if (mc.gameSettings.keyBindSneak.pressed) {
            mc.thePlayer.motionY = -speedValue.toDouble()
        } else {
            mc.thePlayer.motionY = 0.0
        }
        mc.thePlayer.strafe(speedValue)
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            if (lastFlag) {
                lastFlag = false
                return
            }
            event.cancel()
        }

        if (packet is S08PacketPlayerPosLook) {
            posX = packet.x
            posY = packet.y
            posZ = packet.z
            // when teleport,motion reset
            motionX = 0.0
            motionY = 0.0
            motionZ = 0.0
            // apply the flag to bypass some anticheat
            lastFlag = true
            mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(posX, posY, posZ, packet.yaw, packet.pitch, false))

            event.cancel()
        }
    }
}