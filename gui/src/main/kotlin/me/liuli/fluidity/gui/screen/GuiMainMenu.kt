/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.liuli.fluidity.gui.theme.ThemeManager.scheme
import me.liuli.fluidity.gui.compose.gui.GuiComposeDynamic
import me.liuli.fluidity.gui.theme.ThemeManager.background
import me.liuli.fluidity.util.client.queueScreen
import me.liuli.fluidity.util.mc
import net.minecraft.client.gui.GuiMultiplayer

@Preview
@Composable
private fun MainMenu() {
    Column {
        var text by remember { mutableStateOf("Text") }
        TextField(text, { text = it })

        @Composable
        fun space() = Spacer(Modifier.height(5.dp))

        Text("Hey, this is test :)", color = scheme.primary)
        Text("red background", Modifier.background(Color.Red))

        val clickCounter by remember { mutableStateOf(0) }

        Button(onClick = { mc.queueScreen { GuiMultiplayer(mc.currentScreen) } }) {
            Text("don't click me")
        }
        space()
        Text("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo.")
        space()
        Text("戰爭即和平；自由即奴役；無知即力量。")
        space()
        Text("Amount of clicks: $clickCounter")

        Box(Modifier.weight(10f)) {
            val state = rememberLazyListState()

            LazyColumn(state = state, modifier = Modifier.width(200.dp).fillMaxHeight()) {
                items(100) {
                    Text("Item $it")
                }
            }

            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}

class GuiMainMenu : GuiComposeDynamic() {

    init {
        content = {
            background(Modifier
                .fillMaxSize()
                .blur(
                    radiusX = 10.dp,
                    radiusY = 10.dp,
                    edgeTreatment = BlurredEdgeTreatment(RoundedCornerShape(8.dp))
                )
            )
            MainMenu()
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

}