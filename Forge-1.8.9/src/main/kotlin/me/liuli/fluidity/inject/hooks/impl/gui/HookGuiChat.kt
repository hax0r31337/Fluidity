/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.gui

import me.liuli.fluidity.command.CommandManager
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.util.mc
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.gui.GuiChat
import java.awt.Color
import java.util.*

class HookGuiChat : HookProvider("net.minecraft.client.gui.GuiChat") {

    @Hook(method = "keyTyped", type = Hook.Type("ENTER"))
    fun keyTyped(param: MethodHookParam) {
        val text = (param.thisObject as GuiChat).inputField.text
        if (text.startsWith(CommandManager.prefix)) {
            val keyCode = param.args[1] as Int
            if (keyCode == 28 || keyCode == 156) {
                CommandManager.handleCommand(text)
                param.result = null
                mc.ingameGUI.chatGUI.addToSentMessages(text)
                mc.displayGuiScreen(null)
            } else {
                CommandManager.autoComplete(text)
            }
        }
    }

    @Hook(method = "setText", type = Hook.Type("ENTER"))
    fun setText(param: MethodHookParam) {
        if (param.args[1] as Boolean) {
            val newChatText = (param.args[0] ?: return) as String
            if (!newChatText.startsWith(CommandManager.prefix)) return
            (param.thisObject as GuiChat).inputField.text = CommandManager.prefix + "say " + newChatText
            param.result = null
        }
    }

    /**
     * Adds client command auto completion and cancels sending an auto completion request packet
     * to the server if the message contains a client command.
     *
     * @author NurMarvin
     */
    @Hook(method = "sendAutocompleteRequest", type = Hook.Type("ENTER"))
    fun sendAutocompleteRequest(param: MethodHookParam) {
        val full = (param.args[0] ?: return) as String
        if (CommandManager.autoComplete(full)) {
            (param.thisObject as GuiChat).waitingOnAutocomplete = true
            val latestAutoComplete = CommandManager.latestAutoComplete
            if (full.lowercase(Locale.getDefault())
                    .endsWith(latestAutoComplete[latestAutoComplete.size - 1].lowercase())) return
            (param.thisObject as GuiChat).onAutocompleteResponse(latestAutoComplete)
            param.result = null
        }
    }

    @Hook(method = "onAutocompleteResponse", type = Hook.Type("INVOKE", "net/minecraft/client/gui/GuiChat;autocompletePlayerNames()V"))
    fun onAutocompleteResponse(param: MethodHookParam) {
        if (CommandManager.latestAutoComplete.isNotEmpty())
            param.result = null
    }

    @Hook(method = "drawScreen", type = Hook.Type("EXIT"))
    fun drawScreen(param: MethodHookParam) {
        if (CommandManager.latestAutoComplete.isNotEmpty()) {
            val inputField = (param.thisObject as GuiChat).inputField
            if (inputField.text.isEmpty() || !inputField.text.startsWith(CommandManager.prefix)) return
            val latestAutoComplete = CommandManager.latestAutoComplete
            val textArray = inputField.text.split(" ")
            val text = textArray[textArray.size - 1]
            val result = latestAutoComplete.filter { it.lowercase().startsWith(text.lowercase()) }
            val resultText = if (result.isNotEmpty()) result[0].substring(result[0].length.coerceAtMost(text.length)) else ""
            mc.fontRendererObj.drawStringWithShadow(resultText, (inputField.xPosition + mc.fontRendererObj.getStringWidth(inputField.text)).toFloat(), inputField.yPosition.toFloat(), Color(165, 165, 165).rgb)
        }
    }
}