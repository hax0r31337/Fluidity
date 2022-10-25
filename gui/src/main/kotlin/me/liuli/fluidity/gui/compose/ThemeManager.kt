/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.compose

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme

object ThemeManager {

    var darkMode = currentSystemTheme != SystemTheme.LIGHT // prefer dark than light?
        private set
    var theme = if(darkMode) darkColorScheme() else lightColorScheme()
        private set

    // TODO: load theme from wallpaper
}