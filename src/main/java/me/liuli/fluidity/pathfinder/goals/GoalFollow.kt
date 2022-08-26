package me.liuli.fluidity.pathfinder.goals

import me.liuli.fluidity.pathfinder.path.PathMove
import me.liuli.fluidity.util.move.distanceXZ
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3i
import kotlin.math.abs

open class GoalFollow(val entity: EntityLivingBase, range: Double) : IGoal {

    protected var x = entity.position.x
    protected var y = entity.position.y
    protected var z = entity.position.z
    protected val rangeSq = range * range

    override fun heuristic(node: PathMove): Double {
        return distanceXZ(this.x - node.x, this.z - node.z).toDouble() + abs(this.y - node.y)
    }

    override fun isEnd(pos: Vec3i): Boolean {
        val dx = this.x - pos.x
        val dy = this.y - pos.y
        val dz = this.z - pos.z
        return (dx * dx + dy * dy + dz * dz) <= this.rangeSq
    }

    override fun hasChanged(): Boolean {
        val px = entity.position.x
        val py = entity.position.y
        val pz = entity.position.z
        val dx = this.x - px
        val dy = this.y - py
        val dz = this.z - pz
        if (dx * dx + dy * dy + dz * dz > rangeSq) {
            this.x = px
            this.y = py
            this.z = pz
            return true
        }
        return false
    }

    override fun isValid() = true
}