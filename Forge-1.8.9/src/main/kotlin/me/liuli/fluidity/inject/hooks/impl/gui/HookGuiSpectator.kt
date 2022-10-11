package me.liuli.fluidity.inject.hooks.impl.gui

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.Render2DEvent
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.gui.ScaledResolution

class HookGuiSpectator : HookProvider("net.minecraft.client.gui.GuiSpectator") {

    @Hook(method = "renderTooltip", type = Hook.Type("ENTER"))
    fun renderTooltip(param: MethodHookParam) {
        Fluidity.eventManager.call(Render2DEvent(param.args[0] as ScaledResolution, param.args[1] as Float))
    }
}