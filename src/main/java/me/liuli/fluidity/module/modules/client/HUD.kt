package me.liuli.fluidity.module.modules.client

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Render2DEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.EaseUtils
import me.liuli.fluidity.util.render.glColor
import me.liuli.fluidity.util.render.rainbow
import me.liuli.fluidity.util.render.reAlpha
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class HUD : Module("HUD", "Display hud of the client", ModuleCategory.CLIENT, defaultOn = true) {
    private var lastUpdate = System.currentTimeMillis()

    @Listen
    fun onRender2d(event: Render2DEvent) {
        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / 800.0
        lastUpdate = time
        val fontRenderer = mc.fontRendererObj
        val fontHeight = fontRenderer.FONT_HEIGHT

        fontRenderer.drawString(Fluidity.NAME.substring(0, 1), 10, 10, rainbow(1).rgb)
        fontRenderer.drawString(Fluidity.NAME.substring(1), 10 + fontRenderer.getStringWidth(Fluidity.NAME.substring(0, 1)), 10, Color.WHITE.rgb)

        val modules = Fluidity.moduleManager.modules.filter { (it.state || it.animate != 0.0) && it.array }
            .sortedBy { -fontRenderer.getStringWidth(it.name) }
        if (modules.isEmpty())
            return

        var idx = 0
        val blank = fontHeight / 2f
        GL11.glPushMatrix()
        GL11.glTranslatef(event.scaledResolution.scaledWidth.toFloat(), 0f, 0f)
        var color = rainbow(1)
        var nWidth = fontRenderer.getStringWidth(modules[0].name) + blank * 2f
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        modules.forEachIndexed { realIndex, module ->
            module.animate = if (module.state) {
                module.animate + pct
            } else {
                module.animate - pct
            }.coerceIn(0.0, 1.0)
            val animate = if (module.state) { EaseUtils.easeOutCubic(module.animate) } else { EaseUtils.easeInCubic(module.animate) }.toFloat()

            if (animate == 0f) {
                nWidth = if (modules.size > realIndex + 1) {
                    fontRenderer.getStringWidth(modules[realIndex + 1].name) + blank * 2f
                } else {
                    0f
                }
                return@forEachIndexed
            } else if (animate != 1f) {
                GL11.glScalef(1f, animate, 1f)
            }

            val width = nWidth
            val height = fontHeight + blank
            val xOffset = -width * animate
            // draw outline
            nWidth = if (modules.size > realIndex + 1) {
                fontRenderer.getStringWidth(modules[realIndex + 1].name) + blank * 2f
            } else {
                0f
            }

            GL11.glDisable(GL11.GL_TEXTURE_2D)

            val nColor = rainbow(idx + 1)

            GL11.glBegin(GL11.GL_QUADS)
            glColor(color.reAlpha(130).darker())
            GL11.glVertex2f(width + xOffset, 0f)
            GL11.glVertex2f(xOffset, 0f)
            glColor(nColor.reAlpha(130).darker())
            GL11.glVertex2f(xOffset, height)
            GL11.glVertex2f(width + xOffset, height)
            GL11.glEnd()

            GL11.glLineWidth(3f)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            glColor(color)
            GL11.glVertex2f(xOffset, 0f)
            glColor(nColor)
            GL11.glVertex2f(xOffset, height)
            GL11.glVertex2f(xOffset + width - nWidth, height)
            GL11.glEnd()

            GL11.glEnable(GL11.GL_TEXTURE_2D)

            fontRenderer.drawString(module.name, blank + xOffset, blank * 0.6f, color.rgb, false)

            if (animate != 1f) {
                GL11.glScalef(1f, 1f / animate, 1f)
            }

            GL11.glTranslatef(0f, (fontHeight + blank) * animate, 0f)
            idx++
            color = nColor
        }
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glPopMatrix()
        GlStateManager.resetColor()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }
}