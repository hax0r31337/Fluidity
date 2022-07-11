package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import java.awt.Color

class ChestESP : Module("ChestESP", "Allow you see chests through wall", ModuleCategory.RENDER) {

    private val colorValue = ColorValue("Color", Color.BLUE.rgb)
    private val boxAlphaValue = IntValue("BoxAlpha", 50, 0, 255)
    private val outlineAlphaValue = IntValue("OutlineAlpha", 255, 0, 255)
    private val outlineThicknessValue = FloatValue("OutlineThickness", 1f, 1f, 10f)

    @Listen
    fun onRender3D(event: Render3DEvent) {
        mc.theWorld.loadedTileEntityList.filterIsInstance<TileEntityChest>()
            .forEach { entity ->
                val axisAlignedBB = AxisAlignedBB(
                    entity.pos.x - mc.renderManager.renderPosX,
                    entity.pos.y - mc.renderManager.renderPosY,
                    entity.pos.z - mc.renderManager.renderPosZ,
                    entity.pos.x + 1.0 - mc.renderManager.renderPosX,
                    entity.pos.y + 1.0 - mc.renderManager.renderPosY,
                    entity.pos.z + 1.0 - mc.renderManager.renderPosZ
                )
                drawAxisAlignedBB(axisAlignedBB, colorValue.get(), outlineThicknessValue.get(), outlineAlphaValue.get(), boxAlphaValue.get())
            }
    }
}