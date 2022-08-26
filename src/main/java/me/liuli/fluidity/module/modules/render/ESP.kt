package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import me.liuli.fluidity.util.render.quickDrawRect
import me.liuli.fluidity.util.world.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color

object ESP : Module("ESP", "Allows you see your targets through wall", ModuleCategory.RENDER) {

    val onlyShowAttackableValue = BoolValue("OnlyShowAttackable", false)
    private val colorValue = ColorValue("Color", Color.WHITE.rgb)
    private val boxAlphaValue = IntValue("BoxAlpha", 50, 0, 255)
    private val outlineAlphaValue = IntValue("OutlineAlpha", 255, 0, 255)
    private val outlineThicknessValue = FloatValue("OutlineThickness", 1f, 1f, 10f)
    val nameValue = BoolValue("Name", true)
    private val scaleValue = FloatValue("NameScale", 2F, 1F, 4F)
    private val nameBackgroundColorValue = ColorValue("NameBackgroundColor", Color(0, 0, 0, 150).rgb)
    private val scaleMultiplierValue = FloatValue("NameScaleMultiplier", 4F, 1F, 10F)

    @Listen
    fun onRender3D(event: Render3DEvent) {
        val list = mc.theWorld.loadedEntityList.filter { Targets.isTarget(it, onlyShowAttackableValue.get()) }
            .also { if(it.isEmpty()) return }

        val nameAlpha = nameBackgroundColorValue.get() shr 24 and 0xFF

        list.forEach { entity ->
            val entityBox = entity.entityBoundingBox
            val x = entity.renderPosX
            val y = entity.renderPosY
            val z = entity.renderPosZ
            val axisAlignedBB = AxisAlignedBB(
                entityBox.minX - entity.posX + x,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z,
                entityBox.maxX - entity.posX + x,
                entityBox.maxY - entity.posY + y,
                entityBox.maxZ - entity.posZ + z
            )
            drawAxisAlignedBB(axisAlignedBB, if((entity as EntityLivingBase).hurtTime > 0) { 0xFF0000 } else { colorValue.get() }, outlineThicknessValue.get(), outlineAlphaValue.get(), boxAlphaValue.get())

            if (nameValue.get()) {
                GL11.glPushMatrix()

                GL11.glTranslated(x, y + entity.eyeHeight + 0.2, z)
                GL11.glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
                GL11.glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)

                GL11.glDisable(GL11.GL_LIGHTING)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

                val scale = ((mc.thePlayer.getDistanceToEntity(entity) / scaleMultiplierValue.get()).coerceAtLeast(1f) / 150F) * scaleValue.get()
                GL11.glScalef(-scale, -scale, scale)
                GL11.glTranslatef(0f, -mc.fontRendererObj.FONT_HEIGHT * 1.4f, 0f)

                val width = (mc.fontRendererObj.getStringWidth(entity.name) / 2f) + 5f

                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)

                quickDrawRect(-width, mc.fontRendererObj.FONT_HEIGHT * -0.3f, width, mc.fontRendererObj.FONT_HEIGHT * 1.1f, nameBackgroundColorValue.get())
                quickDrawRect(-width, mc.fontRendererObj.FONT_HEIGHT * 1.1f, -width + (width * 2 * entity.healthPercent), mc.fontRendererObj.FONT_HEIGHT * 1.4f, entity.healthColor(nameAlpha).rgb)
                quickDrawRect(-width + (width * 2 * entity.healthPercent), mc.fontRendererObj.FONT_HEIGHT * 1.1f, width, mc.fontRendererObj.FONT_HEIGHT * 1.4f, nameBackgroundColorValue.get())

                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)

                mc.fontRendererObj.drawString(entity.name, -mc.fontRendererObj.getStringWidth(entity.name) / 2f, 0f, Color.WHITE.rgb, false)

                GL11.glEnable(GL11.GL_DEPTH_TEST)

                GL11.glColor4f(1f, 1f, 1f, 1f)
                GL11.glPopMatrix()
            }
        }
    }
}