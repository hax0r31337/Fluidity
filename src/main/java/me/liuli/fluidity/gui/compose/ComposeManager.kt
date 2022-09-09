package me.liuli.fluidity.gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.asCoroutineDispatcher
import me.liuli.fluidity.util.mc
import org.jetbrains.skia.*
import org.jetbrains.skiko.OpenGLApi
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL44
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


class ComposeManager(private var width: Int, private var height: Int, content: @Composable () -> Unit) {

    val scene = ComposeScene(coroutineContext) { hasRenderUpdate = true }
    lateinit var bitmap: Bitmap
    var canvas = createCanvas(width, height)
        private set
    private var textureNew: ByteBuffer? = null
    private var hasRenderUpdate = false

    val texId: Int

    init {
        texId = GL11.glGenTextures()
        scene.setContent(content)
    }

    fun finalize() {
        scene.close()
        mc.addScheduledTask {
            GL11.glDeleteTextures(texId)
        }
    }

    private fun createCanvas(width: Int, height: Int): Canvas {
        bitmap = Bitmap().also {
            if (!it.allocPixels(ImageInfo.Companion.makeN32(width, height, ColorAlphaType.PREMUL)))
                error("Could not allocate the required resources for rendering the compose gui!")
        }
        return Canvas(bitmap)
    }

    fun resizeCanvas(widthNow: Int, heightNow: Int) {
        if (widthNow != width || heightNow != height) {
            bitmap.close()
            canvas.close()
            canvas = createCanvas(widthNow, heightNow)
            width = widthNow
            height = heightNow
        }
    }

    fun printCanvas() {
        val bytes = bitmap.readPixels()
        if (bytes != null) {
            var cache: Byte
            for (i in bytes.indices step 4) {
                cache = bytes[i]
                bytes[i] = bytes[i+2]
                bytes[i+2] = cache
            }
            val byteBuffer = ByteBuffer.allocateDirect(bytes.size)
            byteBuffer.put(bytes)
            byteBuffer.flip()

            textureNew = byteBuffer
        }
    }

    fun drawCanvas() {
        scene.render(canvas, System.nanoTime())
    }

    fun updateCanvas(widthNow: Int, heightNow: Int) {
        if (hasRenderUpdate) {
            scene.constraints = Constraints(maxWidth = widthNow, maxHeight = heightNow)
            resizeCanvas(widthNow, heightNow)
            val t1 = System.nanoTime()
            drawCanvas()
            println("TIME1 ${(System.nanoTime() - t1) / 1_000_000}")
            coroutineContext.executor.execute {
                printCanvas()
                canvas.clear(Color.TRANSPARENT)
            }
            hasRenderUpdate = false
        }
        if (textureNew != null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId)

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bitmap.width, bitmap.height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureNew)

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

            textureNew = null
        }
    }

    companion object {
        val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }
}