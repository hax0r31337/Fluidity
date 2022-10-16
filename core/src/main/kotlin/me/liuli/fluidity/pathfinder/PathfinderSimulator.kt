/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.pathfinder

import me.liuli.fluidity.pathfinder.path.PathMove
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.Vec3d
import me.liuli.fluidity.util.move.syncPosition
import me.liuli.fluidity.util.world.EntitySimulatable
import net.minecraft.util.MathHelper
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor

object PathfinderSimulator {

    /**
     * @param goal A function is the goal has been reached or not
     * @param controller Controller that can change the current control State for the next tick
     * @param ticks Number of ticks to simulate
     * @returns [FakePlayer] A player state of the final simulation tick
     */
    fun simulateUntil(goal: (EntitySimulatable) -> Boolean, controller: (EntitySimulatable, Int) -> Unit, ticks: Int = 1, stateIn: EntitySimulatable? = null): EntitySimulatable {
        val state = stateIn ?: EntitySimulatable(mc.thePlayer)

        for (i in 0 until ticks) {
            controller(state, i)
            state.onUpdate()
            if (state.isInLava) return state
            if (goal(state)) return state
        }

        return state
    }

    fun getReached(path: MutableList<PathMove>): (EntitySimulatable) -> Boolean {
        return {
            if (path.isNotEmpty()) {
                val node = path[0]
                val delta = Vec3d(it.posX, it.posY, it.posZ).apply {
                    x = node.postX - x
                    y = node.postY - y
                    z = node.postZ - z
                }
                abs(delta.x) <= 0.35 && abs(delta.z) <= 0.35 && abs(delta.y) < 1
            } else false
        }
    }

    fun getController(nextPoint: PathMove, jump: Boolean, sprint: Boolean, jumpAfter: Int = 0): (EntitySimulatable, Int) -> Unit {
        return { state, tick ->
            val dx = nextPoint.postX - state.posX
            val dz = nextPoint.postZ - state.posZ
            state.rotationYaw = MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(-dx, dz)).toFloat())

            state.moveForward = 1f
            state.jump = jump && tick >= jumpAfter
            state.isSprinting = sprint
        }
    }

    fun canStraightLineBetween(n1: PathMove, n2: PathMove): Boolean {
        val reached = { state: EntitySimulatable ->
            val delta = Vec3d(state.posX, state.posY, state.posZ).apply {
                x -= n2.postX
                y -= n2.postY
                z -= n2.postZ
            }
            val r2 = 0.15 * 0.15
            (delta.x * delta.x + delta.z * delta.z) <= r2 && abs(delta.y) < 0.001 && (state.onGround || state.isInWater)
        }
        val state = EntitySimulatable(mc.thePlayer)
        state.apply {
            posX = n1.postX
            posY = n1.postY
            posZ = n1.postZ
            state.syncPosition()
        }
        this.simulateUntil(reached, this.getController(n2, false, true), floor(5 * Vec3d(n1.postX, n1.postY, n1.postZ).distanceTo(n2.postX, n2.postY, n2.postZ)).toInt(), state)
        return reached(state)
    }

    fun canStraightLine(path: MutableList<PathMove>, sprint: Boolean = false): Boolean {
        if (path.isEmpty()) return false
        val reached = this.getReached(path)
        val state = this.simulateUntil(reached, this.getController(path[0], false, sprint), 200)
        if (reached(state)) return true

        if (sprint) {
            if (this.canSprintJump(path, 0)) return false
        } else {
            if (this.canWalkJump(path, 0)) return false
        }

        for (i in 1 until 7) {
            if (sprint) {
                if (this.canSprintJump(path, i)) return true
            } else {
                if (this.canWalkJump(path, i)) return true
            }
        }
        return false
    }

    fun canSprintJump(path: MutableList<PathMove>, jumpAfter: Int = 0): Boolean {
        if (path.isEmpty()) return false
        val reached = this.getReached(path)
        val state = this.simulateUntil(reached, this.getController(path[0], true, true, jumpAfter), 20)
        return reached(state)
    }

    fun canWalkJump(path: MutableList<PathMove>, jumpAfter: Int = 0): Boolean {
        if (path.isEmpty()) return false
        val reached = this.getReached(path)
        val state = this.simulateUntil(reached, this.getController(path[0], true, false, jumpAfter), 20)
        return reached(state)
    }
}