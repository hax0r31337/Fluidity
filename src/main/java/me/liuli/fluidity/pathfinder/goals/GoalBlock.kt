package me.liuli.fluidity.pathfinder.goals

import me.liuli.fluidity.pathfinder.path.PathMove
import me.liuli.fluidity.util.move.distanceXZ
import net.minecraft.util.Vec3i
import kotlin.math.abs

/**
 * One specific block that the player should stand inside at foot level
 */
open class GoalBlock(val x: Int, val y: Int, val z: Int) : IGoal {

    override fun heuristic(node: PathMove): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble() + abs(this.y - node.y)
    }

    override fun isEnd(pos: Vec3i): Boolean {
        return pos.x == this.x && pos.y == this.y && pos.z == this.z
    }

    override fun hasChanged() = false

    override fun isValid() = true
}