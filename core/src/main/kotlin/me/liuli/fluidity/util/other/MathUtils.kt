package me.liuli.fluidity.util.other

fun Float.inRange(base: Float, range: Float): Boolean {
    return this in base - range..base + range
}

fun Int.inRange(base: Int, range: Int): Boolean {
    return this in base - range..base + range
}

fun Double.inRange(base: Double, range: Double): Boolean {
    return this in base - range..base + range
}