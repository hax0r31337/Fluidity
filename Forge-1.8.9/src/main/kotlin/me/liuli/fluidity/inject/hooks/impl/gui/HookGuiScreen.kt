/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.gui

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.IChatComponent
import java.util.*

class HookGuiScreen : HookProvider("net.minecraft.client.gui.GuiScreen") {

    @Hook(method = "handleComponentHover", type = Hook.Type("ENTER"))
    fun handleComponentHover(param: MethodHookParam) {
        val component = (param.args[0] ?: return) as IChatComponent
        if (component.chatStyle.chatClickEvent == null) return

        val chatStyle = component.chatStyle

        val clickEvent = chatStyle.chatClickEvent
        val hoverEvent = chatStyle.chatHoverEvent

        (param.thisObject as GuiScreen).drawHoveringText(
            listOf("§c§l" + clickEvent.action.canonicalName.uppercase(Locale.getDefault()) + ": §a" + clickEvent.value),
            param.args[1] as Int, (param.args[2] as Int) - if (hoverEvent != null) 17 else 0)
    }
}