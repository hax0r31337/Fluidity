/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.util.move

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

fun distanceXZ(dx: Int, dz: Int): Float {
    val x = abs(dx)
    val z = abs(dz)
    return abs(x - z) + min(x, z) * sqrt(2f)
}

fun distanceXZ(dx: Double, dz: Double): Double {
    val x = abs(dx)
    val z = abs(dz)
    return abs(x - z) + min(x, z) * sqrt(2.0)
}