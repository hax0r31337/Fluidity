/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.ClickBlockEvent
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.*
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.floorPosition
import me.liuli.fluidity.util.move.setClientRotation
import me.liuli.fluidity.util.move.setServerRotation
import me.liuli.fluidity.util.move.toRotation
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import me.liuli.fluidity.util.timing.TheTimer
import me.liuli.fluidity.util.world.getBlock
import me.liuli.fluidity.util.world.getCenterDistance
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color

class Miner : Module("Miner", "Auto mine blocks for you", ModuleCategory.WORLD) {

    private val blockValue = BlockValue("Block", 26)
    private val rangeValue = FloatValue("Range", 5F, 1F, 7F)
    private val switchValue = IntValue("SwitchDelay", 250, 0, 1000)
    private val actionValue = ListValue("Action", arrayOf("Destroy", "Use"), "Destroy")
    private val rotationsValue = ListValue("Rotations", arrayOf("Silent", "Direct", "None"), "Silent")
    private val updateHandleValue = ListValue("UpdateHandle", arrayOf("NotTarget", "Breakable", "None"), "NotTarget")
    private val swingValue = BoolValue("Swing", true)
    private val throughWallsValue = BoolValue("ThroughWalls", false)
    private val bypassValue = BoolValue("Bypass", false)

    private var pos: BlockPos? = null
    private var oldPos: BlockPos? = null
    private var currentDamage = 0F
    private val switchTimer = TheTimer()

    override fun onEnable() {
        pos = null
        oldPos = null
        currentDamage = 0F
        switchTimer.reset()
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (pos == null || pos!!.getCenterDistance() > rangeValue.get()
            || (updateHandleValue.get() == "NotTarget" && (pos!!.getBlock()?.let { Block.getIdFromBlock(it) } ?: -1) != blockValue.get())
            || (updateHandleValue.get() == "Breakable" && (pos!!.getBlock()?.getBlockHardness(mc.theWorld, pos!!) ?: -1f) < 0f)) {
            pos = find(blockValue.get())
        }

        if (pos == null) {
            currentDamage = 0F
            return
        }

        if (oldPos != null && oldPos != pos) {
            currentDamage = 0F
            switchTimer.reset()
        }
        oldPos = pos

        if (!switchTimer.hasTimePassed(switchValue.get())) {
            return
        }

        val rotation = toRotation(Vec3(pos!!.x.toDouble() + 0.5, pos!!.y.toDouble() + 0.5, pos!!.z.toDouble() + 0.5), true)
        when(rotationsValue.get()) {
            "Silent" -> setServerRotation(rotation.first, rotation.second)
            "Direct" -> setClientRotation(rotation.first, rotation.second)
        }

        when(actionValue.get()) {
            "Destroy" -> {
                var block = pos!!.getBlock() ?: return
                var pos = pos
                if (bypassValue.get()) {
                    val blockUp = BlockPos(pos!!.x, pos!!.y + 1, pos!!.z).getBlock()
                    if (blockUp != null && blockUp != Blocks.air) {
                        block = blockUp
                        pos = BlockPos(pos!!.x, pos!!.y + 1, pos!!.z)
                    }
                }

                if (currentDamage == 0F) {
                    val event = ClickBlockEvent(ClickBlockEvent.Type.LEFT, pos, EnumFacing.DOWN)
                    Fluidity.eventManager.call(event)
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.DOWN))

                    if (mc.thePlayer.capabilities.isCreativeMode ||
                        block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld!!, pos!!) >= 1.0F) {
                        if (swingValue.get())
                            mc.thePlayer.swingItem()
                        mc.playerController.onPlayerDestroyBlock(pos!!, EnumFacing.DOWN)

                        currentDamage = 0F
                        pos = null
                        return
                    }
                }

                if (swingValue.get())
                    mc.thePlayer.swingItem()

                currentDamage += block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld!!, pos)
                mc.theWorld!!.sendBlockBreakProgress(mc.thePlayer.entityId, pos, (currentDamage * 10F).toInt() - 1)

                if (currentDamage >= 1F) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        pos, EnumFacing.DOWN))
                    mc.playerController.onPlayerDestroyBlock(pos, EnumFacing.DOWN)
                    currentDamage = 0F
                    pos = null
                }
            }
            "Use" -> {
                if (mc.playerController.onPlayerRightClick(
                        mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, pos, EnumFacing.DOWN,
                        Vec3(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble()))) {
                    if (swingValue.get())
                        mc.thePlayer.swingItem()
                    currentDamage = 0F
                    pos = null
                }
            }
        }
    }

    @Listen
    fun onRender3D(event: Render3DEvent) {
        pos ?: return

        val rx = mc.renderManager.renderPosX
        val ry = mc.renderManager.renderPosY
        val rz = mc.renderManager.renderPosZ

        val axisMining = AxisAlignedBB(
            pos!!.x + 0.5 - (currentDamage * 0.5) - rx,
            pos!!.y + 0.5 - (currentDamage * 0.5) - ry,
            pos!!.z + 0.5 - (currentDamage * 0.5) - rz,
            pos!!.x + 0.5 + (currentDamage * 0.5) - rx,
            pos!!.y + 0.5 + (currentDamage * 0.5) - ry,
            pos!!.z + 0.5 + (currentDamage * 0.5) - rz
        )
        val axisBlock = AxisAlignedBB(
            pos!!.x - rx,
            pos!!.y - ry,
            pos!!.z - rz,
            pos!!.x + 1.0 - rx,
            pos!!.y + 1.0 - ry,
            pos!!.z + 1.0 - rz
        )
        drawAxisAlignedBB(axisMining, Color.GREEN, 0f, 0, 50)
        drawAxisAlignedBB(axisBlock, Color.ORANGE, 1f, 150, 50)
    }

    private fun find(targetID: Int): BlockPos? {
        val floorPos = mc.thePlayer.floorPosition
        val radius = rangeValue.get().toInt() + 1

        var nearestBlockDistance = Double.MAX_VALUE
        var nearestBlock: BlockPos? = null

        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    val blockPos = BlockPos(floorPos.x + x, floorPos.y + y, floorPos.z + z)
                    val block = blockPos.getBlock() ?: continue

                    if (Block.getIdFromBlock(block) != targetID) continue

                    val distance = blockPos.getCenterDistance()
                    if (distance > rangeValue.get()) continue
                    if (nearestBlockDistance < distance) continue
                    if (!throughWallsValue.get() && !isHitable(blockPos)) continue

                    nearestBlockDistance = distance
                    nearestBlock = blockPos
                }
            }
        }

        return nearestBlock
    }

    /**
     * Check if block is hitable (or allowed to hit through walls)
     */
    private fun isHitable(blockPos: BlockPos): Boolean {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        val movingObjectPosition = mc.theWorld!!.rayTraceBlocks(eyesPos,
            Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5), false, true, false)

        return movingObjectPosition != null && movingObjectPosition.blockPos == blockPos
    }
}