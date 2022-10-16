/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.pathfinder.goals

import me.liuli.fluidity.pathfinder.path.PathMove
import me.liuli.fluidity.util.move.distanceXZ
import net.minecraft.util.Vec3i

/**
 * Useful for long-range goals that don't have a specific Y level
 */
open class GoalXZ(val x: Int, val z: Int) : IGoal {

    override fun heuristic(node: PathMove): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble()
    }

    override fun isEnd(pos: Vec3i): Boolean {
        return pos.x == this.x && pos.z == this.z
    }

    override fun hasChanged() = false

    override fun isValid() = true
}