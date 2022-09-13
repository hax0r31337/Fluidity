package me.liuli.fluidity.gui.screen

import me.liuli.fluidity.gui.compose.impl.MainMenu
import me.liuli.fluidity.gui.compose.gui.GuiComposeDynamic

class GuiMainMenu : GuiComposeDynamic(displayable = MainMenu) {

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

}