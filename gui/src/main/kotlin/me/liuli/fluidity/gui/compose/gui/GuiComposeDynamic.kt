/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.compose.gui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import me.liuli.fluidity.gui.theme.ThemeManager.scheme
import org.jetbrains.skia.Color

open class GuiComposeDynamic(backgroundColor: Int = Color.WHITE, repeatKeys: Boolean = true) : AbstractGuiCompose(backgroundColor, repeatKeys) {

    protected var content: @Composable () -> Unit = {}
        set(value) {
            field = {
                MaterialTheme(colorScheme = scheme) {
                    value()
                }
            }
        }

//    init {
//        contentIn?.let {
//            content = contentIn
//        }
//    }

    override fun initGui() {
        initCompose(content)
        super.initGui()
    }

    override fun onGuiClosed() {
        closeCompose()
        super.onGuiClosed()
    }
}