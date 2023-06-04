/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PreMotionEvent
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.setServerRotation
import me.liuli.fluidity.util.move.toRotation
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import net.minecraft.block.BlockAir
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color


class Scaffold : Module("Scaffold", "place blocks automatically", ModuleCategory.WORLD) {

    private var render: BlockPos? = null

    private val extendableFacing = arrayOf(EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.NORTH)

    @Listen
    fun onPreMotion(event: PreMotionEvent) {
        render = null
        if (mc.thePlayer.heldItem?.item !is ItemBlock) return
        val possibilities = searchBlocks(mc.thePlayer.posX, mc.thePlayer.posY,
            mc.thePlayer.posZ, 1)
        val block = possibilities.firstOrNull() ?: return
        render = block
        val facing = getFacing(block) ?: return

        val click = block.subtract(facing.directionVec)
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, click, facing,
            Vec3(0.5, 0.5, 0.5))) {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
        val rot = toRotation(Vec3(click.x + facing.directionVec.x * 0.5, click.y + facing.directionVec.y * 0.5, click.z + facing.directionVec.z * 0.5), true)
        setServerRotation(rot.first, rot.second)
    }

    @Listen
    fun onRender3d(event: Render3DEvent) {
        val block = render ?: return
        drawAxisAlignedBB(AxisAlignedBB(block, block.add(1, 1, 1))
            .offset(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY, -mc.renderManager.renderPosZ),
            Color(134, 206, 203), 0f, 0, 100)
    }

    private fun searchBlocks(offsetX: Double, offsetY: Double, offsetZ: Double, range: Int): List<BlockPos> {
        val possibilities = mutableListOf<BlockPos>()
        val rangeSq = 4.5 * 4.5
        val blockNear = mutableListOf<EnumFacing>()
        val bb = mc.thePlayer.entityBoundingBox.offset(0.0, -1.0, 0.0)
        val standbb = bb.expand(-0.3, 0.0, -0.3)
        val isMoveHorizontal = (mc.thePlayer.posY - mc.thePlayer.prevPosY) < 0.05

        for (x in -range..range) {
            for (z in -range..range) {
                val pos = BlockPos(offsetX + x, offsetY - 0.625, offsetZ + z)
                val block = mc.theWorld.getBlockState(pos)?.block ?: continue
                val posbb = AxisAlignedBB(pos, pos.add(1, 1, 1))
                if (block !is BlockAir) {
                    if (isMoveHorizontal && standbb.intersectsWith(posbb)) {
                        return emptyList()
                    } else {
                        continue
                    }
                } else if (pos.distanceSq(offsetX, offsetY + 1.62, offsetZ) > rangeSq) {
                    continue
                } else if (!bb.intersectsWith(posbb)) {
                    continue
                }
                EnumFacing.values().forEach {
                    val offset = pos.offset(it)
                    if (mc.theWorld.getBlockState(offset)?.block !is BlockAir/*
                        && rayTrace(vectorPosition, pos, it)*/) {
                        blockNear.add(it)
                    }
                }
                if (blockNear.size != 6 && blockNear.isNotEmpty()) {
                    possibilities.add(pos)
                }
                blockNear.clear()
            }
        }

        return possibilities
            .sortedBy { it.distanceSq(offsetX, offsetY-1, offsetZ) }
    }

    private fun rayTrace(eyePos: Vec3, destPos: BlockPos, facing: EnumFacing): Boolean {
        val dstX = destPos.x.toDouble()
        val dstY = destPos.y.toDouble()
        val dstZ = destPos.z.toDouble()
        if(facing == EnumFacing.NORTH) {
            if(rayScan(eyePos, dstX+0.1, dstX+0.9, dstY+0.1, dstY+0.9, dstZ, dstZ)) return true
        }
        if(facing == EnumFacing.SOUTH) {
            if(rayScan(eyePos, dstX+0.1, dstX+0.9, dstY+0.1, dstY+0.9, dstZ+1, dstZ+1)) return true
        }
        if(facing == EnumFacing.WEST) {
            if(rayScan(eyePos, dstX, dstX, dstY+0.1, dstY+0.9, dstZ+0.1, dstZ+0.9)) return true
        }
        if(facing == EnumFacing.EAST) {
            if(rayScan(eyePos, dstX+1, dstX+1, dstY+0.1, dstY+0.9, dstZ+0.1, dstZ+0.9)) return true
        }
        if(facing == EnumFacing.DOWN) {
            if(rayScan(eyePos, dstX+0.1, dstX+0.9, dstY, dstY, dstZ+0.1, dstZ+0.9)) return true
        }
        if(facing == EnumFacing.UP) {
            if(rayScan(eyePos, dstX+0.1, dstX+0.9, dstY+1, dstY+1, dstZ+0.1, dstZ+0.9)) return true
        }
        return false
    }

    private fun rayScan(eyePos: Vec3, x1: Double, x2: Double, y1: Double, y2: Double, z1: Double, z2: Double): Boolean {
        var bx = x1
        while (bx <= x2) {
            var by = y1
            while (by <= y2) {
                var bz = z1
                while (bz <= z2) {
                    val vec3 = Vec3(bx, by, bz)
//                    if (canSee(eyePos, vec3)) return true
                    if (mc.theWorld.rayTraceBlocks(eyePos, vec3) == null) return true
                    bz += 0.4
                }
                by += 0.4
            }
            bx += 0.4
        }
        return false
    }


    private fun getFacing(block: BlockPos): EnumFacing? {
        extendableFacing.forEach {
            val blockExtend = mc.theWorld.getBlockState(block.subtract(it.directionVec))?.block ?: return@forEach
            if (blockExtend !is BlockAir) {
                return it
            }
        }
        return null
    }
}