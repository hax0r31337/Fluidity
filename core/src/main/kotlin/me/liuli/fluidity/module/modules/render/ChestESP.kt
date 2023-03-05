/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.event.WorldEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import net.minecraft.block.BlockChest
import net.minecraft.network.play.server.S24PacketBlockAction
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import java.awt.Color

class ChestESP : Module("ChestESP", "Allow you see chests through wall", ModuleCategory.RENDER) {

    private val colorValue by ColorValue("Color", Color.BLUE.rgb)
    private val boxAlphaValue by IntValue("BoxAlpha", 50, 0, 255)
    private val outlineAlphaValue by IntValue("OutlineAlpha", 255, 0, 255)
    private val outlineThicknessValue by FloatValue("OutlineThickness", 1f, 1f, 10f)
    private val notOpenedValue by BoolValue("NotOpened", false)

    private val openedChests = mutableListOf<BlockPos>()

    override fun onDisable() {
        openedChests.clear()
    }

    @Listen
    fun onWorld(event: WorldEvent) {
        openedChests.clear()
    }

    @Listen
    fun onRender3D(event: Render3DEvent) {
        mc.theWorld.loadedTileEntityList.filterIsInstance<TileEntityChest>()
            .let {
                if (notOpenedValue) {
                    it.filter { !openedChests.contains(it.pos) }
                } else {
                    it
                }
            }.forEach { entity ->
                val axisAlignedBB = AxisAlignedBB(
                    entity.pos.x - mc.renderManager.renderPosX,
                    entity.pos.y - mc.renderManager.renderPosY,
                    entity.pos.z - mc.renderManager.renderPosZ,
                    entity.pos.x + 1.0 - mc.renderManager.renderPosX,
                    entity.pos.y + 1.0 - mc.renderManager.renderPosY,
                    entity.pos.z + 1.0 - mc.renderManager.renderPosZ
                )
                drawAxisAlignedBB(axisAlignedBB, colorValue, outlineThicknessValue, outlineAlphaValue, boxAlphaValue)
            }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (notOpenedValue && packet is S24PacketBlockAction && packet.blockType is BlockChest && packet.data2 == 1) {
            openedChests.add(packet.blockPosition)
        }
    }
}