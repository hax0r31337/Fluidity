package me.liuli.fluidity.inject.inject.hooks.impl.net

import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.inject.inject.hooks.AbstractHookProvider
import me.liuli.fluidity.inject.inject.hooks.Hook
import me.yuugiri.hutil.processor.hook.MethodHookParam

class HookForgeNetworkDispatcher : AbstractHookProvider("net.minecraftforge.fml.common.network.handshake.NetworkDispatcher") {

    @Hook(method = "handleVanilla", type = Hook.Type("ENTER"))
    fun handleVanilla(param: MethodHookParam) {
        if (ConfigManager.antiForge)
            param.result = false
    }
}