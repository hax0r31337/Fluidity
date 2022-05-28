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
        val sr = ScaledResolution(mc)
        val fontRenderer = mc.fontRendererObj
        val fontHeight = fontRenderer.FONT_HEIGHT

        fontRenderer.drawString("F", 10, 10, rainbow(1).rgb)
        fontRenderer.drawString("luidity", 10 + fontRenderer.getStringWidth("F"), 10, Color.WHITE.rgb)

        var index = 0
        val blank = fontHeight / 2
        GL11.glPushMatrix()
        GL11.glTranslatef(sr.scaledWidth.toFloat(), 0f, 0f)
        val modules = Fluidity.moduleManager.modules.filter { (it.state || it.animate != 0.0) && it.array }
            .sortedBy { -fontRenderer.getStringWidth(it.name) }
        modules.forEach { module ->
            if (module.state) {
                module.animate = 1.0.coerceAtMost(module.animate + pct)
            } else {
                module.animate = 0.0.coerceAtLeast(module.animate - pct)
            }
            val color = rainbow(index + 1)
            GL11.glPushMatrix()
            val width = fontRenderer.getStringWidth(module.name) + blank * 2
            val height = fontHeight + blank
            GL11.glTranslated(-width * if (module.state) { EaseUtils.easeOutCubic(module.animate) } else { EaseUtils.easeInCubic(module.animate) }, 0.0, 0.0)
            drawRect(0f, 0f, width + 0f, height + 0f, color.reAlpha(130).darker())
            fontRenderer.drawString(module.name, blank, (blank * 0.6f).toInt(), color.rgb)
            // draw outline
            val nextWidth = try {
                fontRenderer.getStringWidth(modules[modules.indexOf(module) + 1].name) + blank * 2f - 1
            } catch (e: IndexOutOfBoundsException) {
                0f
            }
            drawRect(0f, height - 1f, width - nextWidth, height + 0f, color)
            drawRect(0f, 0f, 1f, height + 0f, color)
            GL11.glPopMatrix()
            GL11.glTranslatef(0f, fontHeight + blank + 0f, 0f)
            index++
        }
        GL11.glPopMatrix()
    }
}