package me.liuli.fluidity.util.other

import java.util.*

private val random = Random()

fun randomString(length: Int): String {
    return randomString(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")
}

fun randomString(length: Int, chars: String): String {
    return randomString(length, chars.toCharArray())
}

fun randomString(length: Int, chars: CharArray): String {
    val stringBuilder = StringBuilder()
    for (i in 0 until length) stringBuilder.append(chars[random.nextInt(chars.size)])
    return stringBuilder.toString()
}

fun nextInt(startInclusive: Int, endExclusive: Int): Int {
    return if (endExclusive - startInclusive <= 0) startInclusive else startInclusive + Random().nextInt(endExclusive - startInclusive)
}

fun nextDouble(startInclusive: Double, endInclusive: Double): Double {
    return if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0) startInclusive else startInclusive + (endInclusive - startInclusive) * Math.random()
}

fun nextFloat(startInclusive: Float, endInclusive: Float): Float {
    return if (startInclusive == endInclusive || endInclusive - startInclusive <= 0f) startInclusive else (startInclusive + (endInclusive - startInclusive) * Math.random()).toFloat()
}
