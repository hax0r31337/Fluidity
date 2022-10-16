/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.pathfinder.path

data class PathMove(val x: Int, val y: Int, val z: Int, val remainingBlocks: Int, val cost: Float,
                    val toBreak: MutableList<PathBreakInfo> = mutableListOf(),
                    val toPlace: MutableList<PathPlaceInfo> = mutableListOf(), val parkour: Boolean = false) {

    var postX = x.toDouble()
    var postY = y.toDouble()
    var postZ = z.toDouble()

    override fun hashCode(): Int {
        return "$x, $y, $z".hashCode()
    }
}