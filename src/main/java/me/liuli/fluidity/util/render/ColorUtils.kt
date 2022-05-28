package me.liuli.fluidity.util.render

import java.awt.Color
import kotlin.math.abs

private val startTime = System.currentTimeMillis()

fun rainbow(index: Int, lowest: Float = 0.07f, bigest: Float = 0.6f, indexOffset: Int = 300): Color {
    return Color.getHSBColor((abs(((((System.currentTimeMillis() - startTime).toInt() + index * indexOffset) / 5000f) % 2) - 1) * (bigest - lowest)) + lowest, 0.7f, 1f)
}

fun Color.reAlpha(alpha: Int): Color {
    return Color(this.red, this.green, this.blue, alpha)
}