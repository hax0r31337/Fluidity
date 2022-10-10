package me.liuli.fluidity.inject.inject.hooks.impl.net

import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.inject.inject.hooks.AbstractHookProvider
import me.liuli.fluidity.inject.inject.hooks.Hook
import me.yuugiri.hutil.processor.hook.MethodHookParam

class HookForgeHandshakeMessageHandler : AbstractHookProvider("net.minecraftforge.fml.common.network.handshake.HandshakeMessageHandler") {

    @Hook(method = "channelRead0", type = Hook.Type("ENTER"))
    fun channelRead0(param: MethodHookParam) {
        if (ConfigManager.antiForge)
            param.result = null
    }
}