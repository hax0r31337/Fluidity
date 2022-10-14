package me.liuli.fluidity.inject.hooks.impl.net

import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.network.handshake.client.C00Handshake

class HookC00Handshake : HookProvider("net.minecraft.network.handshake.client.C00Handshake") {

    @Hook(method = "writePacketData", type = Hook.Type("ENTER"))
    fun writePacketData(param: MethodHookParam) {
        val pk = param.thisObject as C00Handshake
        val buf = param.args[0] as PacketBuffer

        buf.writeVarIntToBuffer(pk.protocolVersion)
        buf.writeString(pk.ip + if (ConfigManager.antiForge && !Minecraft.getMinecraft().isIntegratedServerRunning) "" else "\u0000FML\u0000")
        buf.writeShort(pk.port)
        buf.writeVarIntToBuffer(pk.requestedState.id)

        param.result = null
    }
}