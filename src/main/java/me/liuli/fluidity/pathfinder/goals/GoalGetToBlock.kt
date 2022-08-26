package me.liuli.fluidity.pathfinder.goals

import me.liuli.fluidity.pathfinder.path.PathMove
import me.liuli.fluidity.util.move.distanceXZ
import net.minecraft.util.Vec3i
import kotlin.math.abs

/**
 * Don't get into the block, but get directly adjacent to it. Useful for chests.
 */
open class GoalGetToBlock(val x: Int, val y: Int, val z: Int) : IGoal {

    override fun heuristic(node: PathMove): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble() +
                abs((node.y - this.y).let { if(it < 0) it + 1 else it })
    }

    override fun isEnd(pos: Vec3i): Boolean {
        return abs(this.x - pos.x) + abs(this.z - pos.z) + abs((pos.y - this.y).let { if(it < 0) it + 1 else it }) == 1
    }

    override fun hasChanged() = false

    override fun isValid() = true
}