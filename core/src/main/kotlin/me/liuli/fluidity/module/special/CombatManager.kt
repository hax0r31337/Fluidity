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
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object CombatManager : Listener {

    var target: EntityLivingBase? = null
        get() = if (!attackTimer.hasTimePassed(3000L)) field else {
            field = null
            null
        }
    val hasTarget: Boolean
        get() = target != null
    private val attackTimer = TheTimer()

    var isPacketBlocking = false
        private set

    fun blockSword() {
        if (!isPacketBlocking) {
            isPacketBlocking = true
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), -1, mc.thePlayer.heldItem, 0f, 0f, 0f))
        }
    }

    fun unblockSword() {
        if (isPacketBlocking) {
            isPacketBlocking = false
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(0, 0, 0), EnumFacing.DOWN))
        }
    }

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