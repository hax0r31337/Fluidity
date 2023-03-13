/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.pathfinder.Pathfinder
import me.liuli.fluidity.pathfinder.PathfinderSimulator
import me.liuli.fluidity.pathfinder.goals.GoalBlock
import me.liuli.fluidity.pathfinder.goals.GoalFollow
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.Vec3d
import me.liuli.fluidity.util.move.floorPosition
import me.liuli.fluidity.util.move.setClientRotation
import me.liuli.fluidity.util.move.toRotation
import me.liuli.fluidity.util.world.getBlock
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.*
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.floor

class DojoHelper : Module("DojoHelper", "Hypixel SkyBlock", ModuleCategory.PLAYER) {

    private val modeValue by ListValue("Mode", arrayOf("Swift", "Discipline", "Force", "Mastery", "Control"), "Swift")
    private val latencyValue by IntValue("Latency", 300, 100, 1000)

    private val prevPositionMap = mutableMapOf<Int, Vec3d>()
    private val currentGoodBlocks = mutableListOf<BlockPos>()
    private var serverSlot = 0

    override fun onEnable() {
        mc.thePlayer ?: return

        serverSlot = mc.thePlayer.inventory.currentItem
    }

    override fun onDisable() {
        currentGoodBlocks.clear()
        prevPositionMap.clear()

        if (Pathfinder.stateGoal != null) {
            Pathfinder.stateGoal = null
            Pathfinder.resetPath(true)
        }
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        when (modeValue) {
            "Swift" -> {
                mc.gameSettings.keyBindSneak.pressed = false
                if (!mc.gameSettings.keyBindJump.pressed) {
                    var drop = false
                    PathfinderSimulator.simulateUntil({
                        if (!it.onGround) {
                            drop = true
                            true
                        } else {
                            false
                        }
                    }, { _, _, -> }, 20)
                    if (drop) {
                        mc.gameSettings.keyBindSneak.pressed = true
                    }
                }
                for (block in currentGoodBlocks.map { it }) {
                    if (mc.thePlayer.getDistanceSq(block.x + 0.5, block.y.toDouble() + 1, block.z + 0.5) < 0.3) {
                        currentGoodBlocks.remove(block)
                    }
                }
                if (currentGoodBlocks.isNotEmpty()) {
                    val last = currentGoodBlocks.last().up()
                    if (Pathfinder.stateGoal == null || Pathfinder.stateGoal !is GoalBlock
                        || (Pathfinder.stateGoal as GoalBlock).let { last.x != it.x || last.y != it.y || last.z != it.z }) {
                        Pathfinder.setGoal(GoalBlock(last.x, last.y, last.z))
                    }
                } else if (Pathfinder.stateGoal != null) {
                    Pathfinder.stateGoal = null
                    Pathfinder.resetPath(true)
                }
            }
            "Discipline" -> {
                val zombie =
                    mc.theWorld.loadedEntityList.filter { it is EntityZombie }
                        .sortedBy { it.getDistanceSqToEntity(mc.thePlayer) }
                        .firstOrNull { it.inventory[4] != null } ?: return
                if (zombie.getDistanceSqToEntity(mc.thePlayer) > 900) return
                if (!(Pathfinder.stateGoal is GoalFollow && (Pathfinder.stateGoal as GoalFollow).entity == zombie)) {
                    Pathfinder.setGoal(GoalFollow(zombie, 2.0))
                }

                val target = mc.objectMouseOver?.entityHit ?: return
                val slot = disciplinePickSlot(target)
                if (slot != -1 && mc.thePlayer.inventory.currentItem != slot) {
                    mc.thePlayer.inventory.currentItem = slot
                }
            }
            "Mastery" -> {
                if (mc.thePlayer.heldItem?.item !is ItemBow) {
                    mc.gameSettings.keyBindUseItem.pressed = false
                    return
                }

                val myPos = mc.thePlayer.floorPosition
                val regex = Regex("[§a-z]{4}\\d:\\d{3}")
                val targets = mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.getDistanceSq(myPos) < 900 && it.name.matches(regex) }
                mc.gameSettings.keyBindUseItem.pressed = true
                if (targets.isEmpty()) {
                    return
                }
                val latency = latencyValue / 1000f
                val bestTargetPair = targets.filter { it.name.contains("§e") }.map { Pair(it, it.name.substring(4).replace(":", ".").toFloat()) }
                    .maxByOrNull {
                        if (it.second > latency) { -1.0f } else { it.second }
                    } ?: return
                if (bestTargetPair.second > latency) {
                    return
                }
                val bestTarget = bestTargetPair.first
                val rotation = toRotation(Vec3(bestTarget.posX, bestTarget.posY + bestTarget.height + 1.7, bestTarget.posZ), true)
                setClientRotation(rotation.first, rotation.second)
                if (mc.thePlayer.itemInUseDuration > 20) {
                    mc.gameSettings.keyBindUseItem.pressed = false
                }
            }
            "Control" -> {
                val skeleton =
                    mc.theWorld.loadedEntityList.filter { it.getDistanceSqToEntity(mc.thePlayer) < 900 && it is EntitySkeleton }
                        .firstOrNull { it.inventory[4] != null } ?: return
                val latencyMultiplier = latencyValue / 100f
                val prev = prevPositionMap[skeleton.entityId] ?: Vec3d(skeleton.prevPosX, skeleton.prevPosY, skeleton.prevPosZ).also {
                    displayAlert("Failed to load previous position of entity ${skeleton.entityId}")
                }

                val xCoord = skeleton.posX + (skeleton.serverPosX / 32.0 - prev.x) * latencyMultiplier
                val yCoord = skeleton.posY + (skeleton.serverPosY / 32.0 - prev.y) * (latencyMultiplier * 0.4)
                val zCoord = skeleton.posZ + (skeleton.serverPosZ / 32.0 - prev.z) * latencyMultiplier
                var groundYCoord = floor(yCoord)
                while(groundYCoord > yCoord - 5) {
                    val bl = BlockPos(xCoord, groundYCoord, zCoord)
                    if (bl.getBlock() != Blocks.air) {
                        groundYCoord += bl.getBlock()?.let { it.blockBoundsMaxY } ?: 1.0
                        break
                    }
                    groundYCoord--
                }
                val center = Vec3(xCoord, groundYCoord + (yCoord - groundYCoord) * 0.3 + skeleton.eyeHeight, zCoord)
                toRotation(center, true).let {
                    setClientRotation(it.first, it.second)
                }
            }
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            state = false // disable after teleport
        }

        when (modeValue) {
            "Swift" -> {
                if (packet is S23PacketBlockChange) {
                    swiftDojoBlockProc(packet.blockState, packet.blockPosition)
                } else if (packet is S22PacketMultiBlockChange) {
                    packet.changedBlocks.forEach {
                        swiftDojoBlockProc(it.blockState, it.pos)
                    }
                }
            }
            "Discipline" -> {
                if (packet is C09PacketHeldItemChange) {
                    serverSlot = packet.slotId
                } else if (packet is C02PacketUseEntity) {
                    val target = packet.getEntityFromWorld(mc.theWorld) ?: return
                    val slot = disciplinePickSlot(target)
                    if (slot != -1 && serverSlot != slot) {
                        event.cancel()
                    } else if (target is EntityZombie) {
                        mc.theWorld.removeEntity(target)
                    }
                }
            }
            "Force" -> {
                if (packet is C02PacketUseEntity) {
                    val target = packet.getEntityFromWorld(mc.theWorld) ?: return
                    val near = mc.theWorld.loadedEntityList.minByOrNull { if (it != target && it != mc.thePlayer) it.getDistanceSq(target.posX, target.posY + 2.1, target.posZ) else 1000.0 } ?: return
//                    displayAlert(near.name)
                    if (near.name.contains("-")) {
                        event.cancel()
                        mc.theWorld.removeEntity(near)
                        mc.theWorld.removeEntity(target)
                        displayAlert("Removed")
                    }
                }
            }
            "Mastery" -> {
                if (packet is S30PacketWindowItems && mc.thePlayer.heldItem?.item is ItemBow) {
                    event.cancel()
                }
            }
            "Control" -> {
                if (packet is S14PacketEntity) {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    prevPositionMap[entity.entityId] = Vec3d(entity.serverPosX / 32.0, entity.serverPosY / 32.0, entity.serverPosZ / 32.0)
                }
            }
        }
    }

    private fun disciplinePickSlot(target: Entity): Int {
        if (target !is EntityZombie) {
            return -1
        }
        val helmet = (target.inventory[4] ?: return -1).unlocalizedName
        return when(helmet) {
            "item.helmetCloth" -> 0
            "item.helmetIron" -> 1
            "item.helmetGold" -> 2
            "item.helmetDiamond" -> 3
            else -> -1
        }
    }

    private fun swiftDojoBlockProc(state: IBlockState, pos: BlockPos) {
        if (pos.distanceSq(mc.thePlayer.floorPosition) < 900 && state.toString() == "minecraft:wool[color=lime]" && !currentGoodBlocks.contains(pos)) {
            currentGoodBlocks.add(pos)
        }
//        mc.theWorld.invalidateRegionAndSetBlock(pos, state)
    }
}