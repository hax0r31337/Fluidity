package me.liuli.fluidity.gui.screen

import me.liuli.fluidity.gui.compose.gui.GuiComposeDynamic
import me.liuli.fluidity.gui.compose.impl.MainMenu

class GuiMainMenu : GuiComposeDynamic({ MainMenu() }) {

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

}