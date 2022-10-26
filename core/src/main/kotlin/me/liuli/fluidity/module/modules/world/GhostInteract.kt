/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BlockValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.floorPosition
import me.liuli.fluidity.util.world.getBlock
import me.liuli.fluidity.util.world.getEyePositionExpand
import net.minecraft.block.Block
import net.minecraft.util.BlockPos

class GhostInteract : Module("GhostInteract", "Allows you interact blocks through wall", ModuleCategory.WORLD) {

    private val blockValue = BlockValue("Block", 54)
    private val radiusValue = IntValue("Radius", 4, 2, 7)

    private var hasClick = false

    override fun onDisable() {
        hasClick = false
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (!hasClick && mc.gameSettings.keyBindUseItem.pressed) {
            val radius = radiusValue.get()
            val selectedBlock = Block.getBlockById(blockValue.get())
            val floorPos = mc.thePlayer.floorPosition
            val eyeRay = mc.thePlayer.getEyePositionExpand(radius.toFloat())
            val pos = mc.thePlayer.positionVector

            for (x in -radius until radius) {
                for (y in radius downTo -radius + 1) {
                    for (z in -radius until radius) {
                        val blockPos = BlockPos(floorPos.x + x, floorPos.y + y, floorPos.z + z)
                        val block = blockPos.getBlock()
                        if (block == null || block !== selectedBlock) {
                            continue
                        }
                        val aabb = block.getCollisionBoundingBox(mc.theWorld, blockPos, mc.theWorld.getBlockState(blockPos))
                        val hitInfo = aabb.calculateIntercept(pos, eyeRay)
                        if (hitInfo != null) {
                            // successful hit
                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem,
                                    blockPos, hitInfo.sideHit, hitInfo.hitVec)) {
                                mc.thePlayer.swingItem()
                                mc.gameSettings.keyBindUseItem.pressed = false
                                hasClick = true
                                return
                            }
                        }
                    }
                }
            }
        } else if (hasClick && mc.gameSettings.keyBindUseItem.pressed) {
        } else {
            hasClick = false
        }
    }
}