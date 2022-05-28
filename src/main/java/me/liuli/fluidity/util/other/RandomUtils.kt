package me.liuli.fluidity.util.other

import java.util.Random

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