/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.pathfinder.Pathfinder
import me.liuli.fluidity.pathfinder.goals.GoalFollow
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.distanceXZ
import me.liuli.fluidity.util.move.floorPosition
import kotlin.math.abs

class AutoMobs : Module("AutoMobs", "Move to mobs automatically", ModuleCategory.PLAYER) {

    private val yMultiplierValue = FloatValue("YMultiplier", 2f, 0f, 10f)

    override fun onDisable() {
        Pathfinder.stateGoal = null
        Pathfinder.resetPath(true)
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        val pos = mc.thePlayer.floorPosition
        val nearestEntity = mc.theWorld.loadedEntityList.filter { it.isTarget() }
            .minByOrNull { distanceXZ(pos.x - it.posX, pos.z - it.posZ) + abs(pos.y - it.posY) * yMultiplierValue.get() } ?: return
        if (Pathfinder.stateGoal is GoalFollow && (Pathfinder.stateGoal as GoalFollow).entity == nearestEntity) {
            return
        }

        Pathfinder.setGoal(GoalFollow(nearestEntity, 2.0))
    }
}