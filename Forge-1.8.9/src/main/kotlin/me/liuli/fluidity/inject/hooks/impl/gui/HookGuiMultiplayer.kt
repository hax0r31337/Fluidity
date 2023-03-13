/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.gui

import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen

class HookGuiMultiplayer : HookProvider("net.minecraft.client.gui.GuiMultiplayer") {

    @Hook(method = "initGui", type = Hook.Type("EXIT"))
    fun initGui(param: MethodHookParam) {
        (param.thisObject as GuiScreen).buttonList.add(GuiButton(996, 8, 8, 98, 20, "AntiForge: " + if (ConfigManager.antiForge) "§aON" else "§cOFF"))
    }

    @Hook(method = "actionPerformed", type = Hook.Type("ENTER"))
    fun actionPerformed(param: MethodHookParam) {
        val button = (param.args[0] ?: return) as GuiButton

        if (button.id == 996) {
            ConfigManager.antiForge = !ConfigManager.antiForge
            button.displayString = "AntiForge: " + if (ConfigManager.antiForge) "§aON" else "§cOFF"
            param.result = null
        }
    }
}