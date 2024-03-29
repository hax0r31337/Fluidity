/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.render.rainbow
import java.awt.Color

object Glint : Module("Glint", "Custom glint effect", ModuleCategory.RENDER) {

    private val modeValue by ListValue("Mode", arrayOf("Rainbow", "Custom"), "Custom")
    private val colorValue by ColorValue("Color", Color.RED.rgb)

    fun getColor(): Int {
        return when (modeValue) {
            "Rainbow" -> rainbow(1).rgb
            else -> colorValue
        }
    }
}