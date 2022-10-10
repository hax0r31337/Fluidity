package me.liuli.fluidity.inject.inject.hooks.impl.client

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.ClickBlockEvent
import me.liuli.fluidity.event.KeyEvent
import me.liuli.fluidity.event.ScreenEvent
import me.liuli.fluidity.event.WorldEvent
import me.liuli.fluidity.inject.inject.hooks.AbstractHookProvider
import me.liuli.fluidity.inject.inject.hooks.Hook
import me.liuli.fluidity.util.mc
import me.yuugiri.hutil.processor.hook.EnumHookShift
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.block.material.Material
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.WorldClient
import org.lwjgl.input.Keyboard

class HookMinecraft : AbstractHookProvider("net.minecraft.client.Minecraft") {

    @Hook(method = "run", type = Hook.Type("ENTER"))
    fun run() {
        Fluidity.init()
    }

    @Hook(method = "startGame", type = Hook.Type("EXIT"))
    fun startGame() {
        Fluidity.init()
    }

    @Hook(method = "shutdown", type = Hook.Type("ENTER"))
    fun shutdown() {
        Fluidity.shutdown()
    }

    @Hook(method = "runTick", type = Hook.Type("INVOKE", "net/minecraft/client/Minecraft;dispatchKeypresses()V"), shift = EnumHookShift.AFTER)
    fun onKey() {
        if (Keyboard.getEventKeyState() && mc.currentScreen == null)
            Fluidity.eventManager.call(KeyEvent(if (Keyboard.getEventKey() == 0) Keyboard.getEventCharacter().code + 256 else Keyboard.getEventKey()))
    }

    @Hook(method = "sendClickBlockToController", type = Hook.Type("INVOKE", "net/minecraft/util/MovingObjectPosition;getBlockPos()Lnet/minecraft/util/BlockPos;"), shift = EnumHookShift.AFTER)
    fun onClickBlock() {
        if (mc.leftClickCounter == 0 && mc.theWorld.getBlockState(mc.objectMouseOver?.blockPos)?.block?.material !== Material.air) {
            Fluidity.eventManager.call(ClickBlockEvent(ClickBlockEvent.Type.LEFT, mc.objectMouseOver.blockPos, mc.objectMouseOver.sideHit))
        }
    }

    @Hook(method = "loadWorld", desc = "(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", type = Hook.Type("ENTER"))
    fun loadWorld(param: MethodHookParam) {
        Fluidity.eventManager.call(WorldEvent(param.args[0] as WorldClient?))
    }

    @Hook(method = "displayGuiScreen", type = Hook.Type("ENTER"))
    fun displayGuiScreen(param: MethodHookParam) {
        val screen = param.args[0] as GuiScreen?
        Fluidity.eventManager.call(ScreenEvent(screen))
        if (screen is GuiMainMenu) {
            param.args[0] = me.liuli.fluidity.gui.screen.GuiMainMenu()
        }
    }

    @Hook(method = "clickMouse", type = Hook.Type("ENTER"))
    fun clickMouse() {
        mc.leftClickCounter = 0
    }
}