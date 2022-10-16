/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.net

import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.yuugiri.hutil.processor.hook.MethodHookParam

class HookForgeHandshakeMessageHandler : HookProvider("net.minecraftforge.fml.common.network.handshake.HandshakeMessageHandler") {

    @Hook(method = "channelRead0", type = Hook.Type("ENTER"))
    fun channelRead0(param: MethodHookParam) {
        if (ConfigManager.antiForge)
            param.result = null
    }
}