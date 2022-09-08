package me.liuli.fluidity.gui.compose

import kotlinx.coroutines.asCoroutineDispatcher
import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.Render2DEvent
import org.jetbrains.skia.*
import org.jetbrains.skia.FramebufferFormat.Companion.GR_GL_RGBA8
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import java.util.concurrent.Executors

object ComposeManager {

    val context = DirectContext.makeGL()
    var surface = createSurface()
        private set
    val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    init {
        Fluidity.eventManager.registerFunction(Render2DEvent::class.java) {
            if (Display.getWidth() != surface.width || Display.getHeight() != surface.height) {
                surface.close()
                surface = createSurface()
            }
        }
    }

    private fun createSurface(): Surface {
//        val sr = ScaledResolution(mc)
        val fbId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)
        val renderTarget = BackendRenderTarget.makeGL(Display.getWidth(), Display.getHeight(), 0, 8, fbId, GR_GL_RGBA8)
        return Surface.makeFromBackendRenderTarget(
            context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
        ) ?: throw IllegalStateException("Surface shouldn't be null")
    }
}