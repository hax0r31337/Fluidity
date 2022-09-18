package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.render.rainbow
import java.awt.Color

object Glint : Module("Glint", "Custom glint effect", ModuleCategory.RENDER) {

    private val modeValue = ListValue("Mode", arrayOf("Rainbow", "Custom"), "Custom")
    private val colorValue = ColorValue("Color", Color.RED.rgb)

    fun getColor(): Int {
        return when (modeValue.get()) {
            "Rainbow" -> rainbow(1).rgb
            else -> colorValue.get()
        }
    }
}