package me.liuli.fluidity.pathfinder.goals

import me.liuli.fluidity.pathfinder.path.PathMove
import net.minecraft.util.Vec3i

interface IGoal {

    /**
     * @return the distance between node and the goal
     */
    fun heuristic(node: PathMove): Double

    /**
     * @return true if the node has reach the goal
     */
    fun isEnd(pos: Vec3i): Boolean

    /**
     * @return true if the goal has changed and the current path
     * should be invalidated and computed again
     */
    fun hasChanged(): Boolean

    /**
     * @return true if the goal is still valid for the goal
     * for the GoalFollow this would be true if the entity is not null
     */
    fun isValid(): Boolean
}