/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.theme.background

import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.util.client.logWarn
import java.io.File

class CommonBackgroundSource(val file: File = File(ConfigManager.rootPath, "background.png")) : IBackgroundSource {

    override fun get() = if (file.exists()) {
        file.inputStream().buffered()
    } else {
        logWarn("no image file found, fallback to default background.")
        CommonBackgroundSource::class.java.getResourceAsStream("/assets/fluidity/ui/background.webp").buffered()
    }
}