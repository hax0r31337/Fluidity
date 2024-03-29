/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.client

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.*
import me.liuli.fluidity.gui.DependencyDownloader
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.util.mc
import me.yuugiri.hutil.processor.hook.EnumHookShift
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.block.material.Material
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.WorldClient
import org.lwjgl.input.Keyboard

class HookMinecraft : HookProvider("net.minecraft.client.Minecraft") {

    private val disableCustomMenuGui = System.getProperty("fluidity.disableCustomMenu") != null

    @Hook(method = "run", type = Hook.Type("ENTER"))
    fun run() {
        DependencyDownloader.asyncLoad()
        Fluidity.init()
    }

    @Hook(method = "startGame", type = Hook.Type("EXIT"))
    fun startGame() {
        DependencyDownloader.awaitLoad()
        Fluidity.load()
    }

    @Hook(method = "shutdown", type = Hook.Type("ENTER"))
    fun shutdown() {
        if (Fluidity.hasLoaded) {
            Fluidity.shutdown()
        }
    }

    @Hook(method = "runTick", type = Hook.Type("INVOKE", "net/minecraft/client/Minecraft;dispatchKeypresses()V"), shift = EnumHookShift.AFTER)
    fun onKey() {
        if (Keyboard.getEventKeyState()) {
            val key = if (Keyboard.getEventKey() == 0) Keyboard.getEventCharacter().code + 256 else Keyboard.getEventKey()
            if (mc.currentScreen == null) {
                Fluidity.eventManager.emit(KeyEvent(key))
            } else {
                Fluidity.eventManager.emit(GuiKeyEvent(Keyboard.getEventCharacter(), key))
            }
        }
    }

    @Hook(method = "sendClickBlockToController", type = Hook.Type("INVOKE", "net/minecraft/util/MovingObjectPosition;getBlockPos()Lnet/minecraft/util/BlockPos;"), shift = EnumHookShift.AFTER)
    fun onClickBlock() {
        if (mc.leftClickCounter == 0 && mc.theWorld.getBlockState(mc.objectMouseOver?.blockPos)?.block?.material !== Material.air) {
            Fluidity.eventManager.emit(ClickBlockEvent(ClickBlockEvent.Type.LEFT, mc.objectMouseOver.blockPos, mc.objectMouseOver.sideHit))
        }
    }

    @Hook(method = "loadWorld", desc = "(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", type = Hook.Type("ENTER"))
    fun loadWorld(param: MethodHookParam) {
        Fluidity.eventManager.emit(WorldEvent(param.args[0] as WorldClient?))
    }

    @Hook(method = "displayGuiScreen", type = Hook.Type("ENTER"))
    fun displayGuiScreen(param: MethodHookParam) {
        val screen = param.args[0] as GuiScreen?
        Fluidity.eventManager.emit(ScreenEvent(screen))
        if (!disableCustomMenuGui && (screen is GuiMainMenu || (screen == null && mc.theWorld == null))) {
            param.args[0] = me.liuli.fluidity.gui.screen.GuiMainMenu()
        }
    }

    @Hook(method = "clickMouse", type = Hook.Type("ENTER"))
    fun clickMouse() {
        mc.leftClickCounter = 0
    }
}