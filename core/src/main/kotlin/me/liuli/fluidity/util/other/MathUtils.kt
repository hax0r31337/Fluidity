/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

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