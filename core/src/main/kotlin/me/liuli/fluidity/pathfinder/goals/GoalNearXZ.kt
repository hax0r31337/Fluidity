/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.pathfinder.goals

import net.minecraft.util.Vec3i

/**
 * Useful for finding builds that you don't have an exact Y level for, just an approximate X and Z level
 */
open class GoalNearXZ(x: Int, z: Int, range: Double) : GoalXZ(x, z) {

    protected val rangeSq = range * range

    override fun isEnd(pos: Vec3i): Boolean {
        val dx = this.x - pos.x
        val dz = this.z - pos.z
        return (dx * dx + dz * dz) <= this.rangeSq
    }
}