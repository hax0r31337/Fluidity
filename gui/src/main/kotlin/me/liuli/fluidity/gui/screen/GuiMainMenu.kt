/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.screen

import me.liuli.fluidity.gui.compose.gui.GuiComposeDynamic
import me.liuli.fluidity.gui.compose.impl.MainMenu

class GuiMainMenu : GuiComposeDynamic({ MainMenu() }) {

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

}