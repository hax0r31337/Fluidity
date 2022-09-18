package me.liuli.fluidity.gui.compose.access

import me.liuli.fluidity.util.client.queueScreen
import me.liuli.fluidity.util.mc
import net.minecraft.client.gui.GuiMultiplayer

object ComposeMcGuiBridge {

    fun guiMultiplayer() {
        mc.queueScreen(GuiMultiplayer(mc.currentScreen))
    }
}