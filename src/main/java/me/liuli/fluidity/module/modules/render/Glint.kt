package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.render.rainbow
import java.awt.Color

object Glint : Module("Glint", "Custom glint effect", ModuleCategory.RENDER) {

    private val modeValue = ListValue("Mode", arrayOf("Rainbow", "Custom"), "Custom")
    private val redValue = IntValue("Red", 255, 0, 255)
    private val greenValue = IntValue("Green", 0, 0, 255)
    private val blueValue = IntValue("Blue", 0, 0, 255)

    fun getColor(): Int {
        return when (modeValue.get()) {
            "rainbow" -> rainbow(1)
            else -> Color(redValue.get(), greenValue.get(), blueValue.get())
        }.rgb
    }
}