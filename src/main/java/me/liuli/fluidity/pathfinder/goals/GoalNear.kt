package me.liuli.fluidity.pathfinder.goals

import net.minecraft.util.Vec3i

/**
 * A block position that the player should get within a certain radius of, used for following entities
 */
open class GoalNear(x: Int, y: Int, z: Int, range: Double) : GoalBlock(x, y, z) {

    protected val rangeSq = range * range

    override fun isEnd(pos: Vec3i): Boolean {
        val dx = this.x - pos.x
        val dy = this.y - pos.y
        val dz = this.z - pos.z
        return (dx * dx + dy * dy + dz * dz) <= this.rangeSq
    }
}