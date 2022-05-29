package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import java.awt.Color

class ESP : Module("ESP", "Allows you see your targets through wall", ModuleCategory.RENDER) {

    private val colorRedValue = IntValue("ColorRed", 255, 0, 255)
    private val colorGreenValue = IntValue("ColorGreen", 255, 0, 255)
    private val colorBlueValue = IntValue("ColorBlue", 255, 0, 255)
    private val boxAlphaValue = IntValue("BoxAlpha", 50, 0, 255)
    private val outlineAlphaValue = IntValue("OutlineAlpha", 255, 0, 255)
    private val outlineThicknessValue = FloatValue("OutlineThickness", 1f, 1f, 10f)

    @EventMethod
    fun onRender3D(event: Render3DEvent) {
        val color = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
        mc.theWorld.loadedEntityList.filter { Targets.isTarget(it, true) }.forEach { entity ->
            val entityBox = entity.entityBoundingBox
            val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX
            val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY
            val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            val axisAlignedBB = AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05,
                entityBox.maxX - entity.posX + x + 0.05,
                entityBox.maxY - entity.posY + y + 0.15,
                entityBox.maxZ - entity.posZ + z + 0.05
            )
            drawAxisAlignedBB(axisAlignedBB, if((entity as EntityLivingBase).hurtTime > 0) { Color.RED } else { color }, outlineThicknessValue.get(), outlineAlphaValue.get(), boxAlphaValue.get())
        }
    }
}