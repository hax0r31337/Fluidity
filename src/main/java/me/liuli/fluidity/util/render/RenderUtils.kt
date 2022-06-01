package me.liuli.fluidity.util.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color

fun glColor(color: Color) {
    val red = color.red / 255f
    val green = color.green / 255f
    val blue = color.blue / 255f
    val alpha = color.alpha / 255f
    GlStateManager.color(red, green, blue, alpha)
}

fun glColor(color: Color, alpha: Int) {
    glColor(color, alpha / 255f)
}

fun glColor(color: Color, alpha: Float) {
    val red = color.red / 255f
    val green = color.green / 255f
    val blue = color.blue / 255f
    GlStateManager.color(red, green, blue, alpha)
}

fun glColor(hex: Int) {
    val alpha = (hex shr 24 and 0xFF) / 255f
    val red = (hex shr 16 and 0xFF) / 255f
    val green = (hex shr 8 and 0xFF) / 255f
    val blue = (hex and 0xFF) / 255f
    GlStateManager.color(red, green, blue, alpha)
}

fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
    GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
}

fun drawAxisAlignedBB(
    axisAlignedBB: AxisAlignedBB,
    color: Color,
    outlineWidth: Float,
    outlineAlpha: Int = 95,
    boxAlpha: Int = 35
) {
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glDepthMask(false)

    if (outlineWidth != 0f && outlineAlpha != 0) {
        GL11.glLineWidth(outlineWidth)
        glColor(color.red, color.green, color.blue, outlineAlpha)
        drawSelectionBoundingBox(axisAlignedBB)
    }

    if (boxAlpha != 0) {
        glColor(color.red, color.green, color.blue, boxAlpha)
        drawFilledBox(axisAlignedBB)
    }

    GlStateManager.resetColor()
    GL11.glEnable(GL11.GL_TEXTURE_2D)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
    GL11.glDepthMask(true)
    GL11.glDisable(GL11.GL_BLEND)
}

fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
    val tessellator = Tessellator.getInstance()
    val worldrenderer = tessellator.worldRenderer
    worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)

    // Lower Rectangle
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

    // Upper Rectangle
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

    // Upper Rectangle
    worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
    worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
    tessellator.draw()
}

fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
    val tessellator = Tessellator.getInstance()
    val worldRenderer = tessellator.worldRenderer
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
    worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    tessellator.draw()
}

fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float) {
    GL11.glBegin(GL11.GL_QUADS)
    GL11.glVertex2d(x2.toDouble(), y.toDouble())
    GL11.glVertex2d(x.toDouble(), y.toDouble())
    GL11.glVertex2d(x.toDouble(), y2.toDouble())
    GL11.glVertex2d(x2.toDouble(), y2.toDouble())
    GL11.glEnd()
}

fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glEnable(GL11.GL_LINE_SMOOTH)
    glColor(color)
    quickDrawRect(x, y, x2, y2)
    GL11.glEnable(GL11.GL_TEXTURE_2D)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LINE_SMOOTH)
}

fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) {
    drawRect(x, y, x2, y2, color.rgb)
}