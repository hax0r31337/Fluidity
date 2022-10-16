/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.pathfinder.path

data class PathBreakInfo(val x: Int, val y: Int, val z: Int) {
    constructor(block: PathBlock) : this(block.x, block.y, block.z)
}