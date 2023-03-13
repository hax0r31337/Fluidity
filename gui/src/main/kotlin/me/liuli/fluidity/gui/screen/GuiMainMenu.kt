/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.gui.compose.gui.GuiComposeDynamic
import me.liuli.fluidity.gui.compose.icon.Group
import me.liuli.fluidity.gui.compose.icon.Logout
import me.liuli.fluidity.gui.theme.ThemeManager.background
import me.liuli.fluidity.gui.theme.ThemeManager.scheme
import me.liuli.fluidity.util.client.queueScreen
import me.liuli.fluidity.util.mc
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.resources.I18n

@Preview
@Composable
private fun MainMenu() {
    val offset = Offset(5.0f, 5.0f)

    Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

        @Composable
        fun Space(size: Int = 5) = Spacer(Modifier.height(size.dp))

        Text(Fluidity.NAME, color = scheme.primary,
            style = TextStyle(
                fontSize = 50.sp,
                shadow = Shadow(
                    color = Color.Black,
                    offset = offset,
                    blurRadius = 2f
                )
            )
        )

        Space(25)

        @Composable
        fun IconButton(onClick: () -> Unit, icon: ImageVector, text: String) {
            Button(onClick = onClick, modifier = Modifier.width(200.dp)) {
                Icon(icon, contentDescription = text)
                Spacer(Modifier.width(4.dp))
                Text(text)
            }
        }

        IconButton({ mc.queueScreen { GuiSelectWorld(mc.currentScreen) } }, Icons.TwoTone.Person, I18n.format("menu.singleplayer"))
        Space()
        IconButton({ mc.queueScreen { GuiMultiplayer(mc.currentScreen) } }, Icons.TwoTone.Group, I18n.format("menu.multiplayer"))
        Space()
        IconButton({ mc.queueScreen { GuiOptions(mc.currentScreen, mc.gameSettings) } }, Icons.TwoTone.Settings, I18n.format("menu.options").trim('.', '…'))
        Space()
        IconButton({ mc.shutdown() }, Icons.TwoTone.Logout, I18n.format("menu.quit"))
    }
    Column(modifier = Modifier.fillMaxSize().padding(3.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End) {
        Text(Fluidity.VERSION, color = scheme.onBackground)
    }
}

class GuiMainMenu : GuiComposeDynamic(waitComposeLoad = true) {

    init {
        content = {
            background()
            MainMenu()
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

}