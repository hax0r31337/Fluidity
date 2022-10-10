package me.liuli.fluidity.inject.inject.hooks.impl.net

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.inject.inject.hooks.AbstractHookProvider
import me.liuli.fluidity.inject.inject.hooks.Hook
import me.liuli.fluidity.util.client.PacketUtils
import me.liuli.fluidity.util.client.PacketUtils.getPacketType
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.network.Packet

class HookNetworkManager : AbstractHookProvider("net.minecraft.network.NetworkManager") {

    @Hook(method = "channelRead0", type = Hook.Type("ENTER"))
    fun channelRead0(param: MethodHookParam) {
        val packet = param.args[1] as Packet<*>? ?: return

        if (getPacketType(packet) !== PacketUtils.PacketType.SERVERSIDE) return

        val event = PacketEvent(packet, PacketEvent.Type.RECEIVE)
        Fluidity.eventManager.call(event)

        if (event.cancelled) param.result = null
    }

    @Hook(method = "sendPacket", desc = "(Lnet/minecraft/network/Packet;)V", type = Hook.Type("ENTER"))
    fun sendPacket(param: MethodHookParam) {
        val packet = param.args[0] as Packet<*>? ?: return

        if (getPacketType(packet) !== PacketUtils.PacketType.CLIENTSIDE) return

        val event = PacketEvent(packet, PacketEvent.Type.SEND)
        Fluidity.eventManager.call(event)

        if (event.cancelled) param.result = null
    }
}