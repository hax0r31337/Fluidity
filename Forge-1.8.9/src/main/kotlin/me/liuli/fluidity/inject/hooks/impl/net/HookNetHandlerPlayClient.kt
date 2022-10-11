package me.liuli.fluidity.inject.hooks.impl.net

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.module.modules.misc.NoRotateSet
import me.liuli.fluidity.util.mc
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook.EnumFlags

class HookNetHandlerPlayClient : HookProvider("net.minecraft.client.network.NetHandlerPlayClient") {

    /**
     * anti client crash
     */
    @Hook(method = "handlePlayerPosLook", type = Hook.Type("ENTER"))
    fun handlePlayerPosLook_Enter(param: MethodHookParam) {
        val pk = param.args[0] as S08PacketPlayerPosLook

        val x = pk.x
        val y = pk.y
        val z = pk.z
        if (x >= Int.MAX_VALUE || x <= Int.MIN_VALUE || y >= Int.MAX_VALUE || y <= Int.MIN_VALUE || z >= Int.MAX_VALUE || z <= Int.MIN_VALUE) {
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, pk.yaw, pk.pitch, false))
            param.result = null
        }
    }

    @Hook(method = "handlePlayerPosLook", type = Hook.Type("INVOKE", "net/minecraft/entity/player/EntityPlayer;setPositionAndRotation(DDDFF)V"))
    fun handlePlayPosLook_Invoke(param: MethodHookParam) {
        if (NoRotateSet.state) {
            val var1 = param.args[0] as S08PacketPlayerPosLook

            val var2 = mc.thePlayer
            var var3 = var1.x
            var var5 = var1.y
            var var7 = var1.z
            var var9 = var1.getYaw()
            var var10 = var1.getPitch()
            if (var1.func_179834_f().contains(EnumFlags.X)) {
                var3 += var2.posX
            } else {
                var2.motionX = 0.0
            }

            if (var1.func_179834_f().contains(EnumFlags.Y)) {
                var5 += var2.posY
            } else {
                var2.motionY = 0.0
            }

            if (var1.func_179834_f().contains(EnumFlags.Z)) {
                var7 += var2.posZ
            } else {
                var2.motionZ = 0.0
            }

            if (var1.func_179834_f().contains(EnumFlags.X_ROT)) {
                var10 += var2.rotationPitch
            }

            if (var1.func_179834_f().contains(EnumFlags.Y_ROT)) {
                var9 += var2.rotationYaw
            }

            var2.setPosition(var3, var5, var7)
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(var3, var5, var7, var9, var10, false))

            param.result = null
        }
    }
}