/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.special

import me.liuli.fluidity.event.AttackEvent
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Listener
import me.liuli.fluidity.event.WorldEvent
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.entity.EntityLivingBase

object CombatManager : Listener {

    var target: EntityLivingBase? = null
        get() = if (!attackTimer.hasTimePassed(3000L)) field else {
            field = null
            null
        }
    val hasTarget: Boolean
        get() = target != null
    private val attackTimer = TheTimer()

    @Listen
    fun onAttack(event: AttackEvent) {
        if (!event.targetEntity.isTarget(true)) return
        target = event.targetEntity as EntityLivingBase
        attackTimer.reset()
    }

    @Listen
    fun onWorld(event: WorldEvent) {
        target = null
    }

    override fun listen() = true
}