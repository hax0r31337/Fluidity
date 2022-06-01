package me.liuli.fluidity.module.modules.client

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.Render2DEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.*
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.awt.Color

class HUD : Module("HUD", "Display hud of the client", ModuleCategory.CLIENT, defaultOn = true) {
    private var lastUpdate = System.currentTimeMillis()

    @EventMethod
    fun onRender2d(event: Render2DEvent) {
        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / 800.0
        lastUpdate = time
        val fontRenderer = mc.fontRendererObj
        val fontHeight = fontRenderer.FONT_HEIGHT

        fontRenderer.drawString("F", 10, 10, rainbow(1).rgb)
        fontRenderer.drawString("luidity", 10 + fontRenderer.getStringWidth("F"), 10, Color.WHITE.rgb)

        val modules = Fluidity.moduleManager.modules.filter { (it.state || it.animate != 0.0) && it.array }
            .sortedBy { -fontRenderer.getStringWidth(it.name) }
        if (modules.isEmpty())
            return

        var index = 0
        val blank = fontHeight / 2f
        GL11.glPushMatrix()
        GL11.glTranslatef(event.scaledResolution.scaledWidth.toFloat(), 0f, 0f)
        var color = rainbow(1)
        var nWidth = fontRenderer.getStringWidth(modules[0].name) + blank * 2f
        modules.forEachIndexed { idx, module ->
            if (module.state) {
                module.animate = 1.0.coerceAtMost(module.animate + pct)
            } else {
                module.animate = 0.0.coerceAtLeast(module.animate - pct)
            }
            val width = nWidth
            val height = fontHeight + blank
            val xOffset = (-width * if (module.state) { EaseUtils.easeOutCubic(module.animate) } else { EaseUtils.easeInCubic(module.animate) }).toFloat()
            // draw outline
            nWidth = if (modules.size > idx + 1) {
                fontRenderer.getStringWidth(modules[idx + 1].name) + blank * 2f
            } else {
                0f
            }

            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glShadeModel(GL11.GL_SMOOTH)

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

            GL11.glShadeModel(GL11.GL_FLAT)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            fontRenderer.drawString(module.name, blank + xOffset, blank * 0.6f, color.rgb, false)

            GL11.glTranslatef(0f, fontHeight + blank, 0f)
            index++
            color = nColor
        }
        GL11.glPopMatrix()
    }
}