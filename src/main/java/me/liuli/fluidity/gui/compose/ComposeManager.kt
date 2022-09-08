package me.liuli.fluidity.gui.compose

import kotlinx.coroutines.asCoroutineDispatcher
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL44
import java.nio.ByteBuffer
import java.util.concurrent.Executors


object ComposeManager {

    var width = Display.getWidth()
        private set
    var height = Display.getHeight()
        private set
    lateinit var bitmap: Bitmap
    var canvas = createCanvas()
        private set
    val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    val texId: Int

    init {
        texId = GL11.glGenTextures()
    }

    private fun createCanvas(): Canvas {
        bitmap = Bitmap().also {
            if (!it.allocN32Pixels(Display.getWidth(), Display.getHeight(), false))
                error("Could not allocate the required resources for rendering the compose gui!")
        }
        return Canvas(bitmap)
    }

    fun refreshCanvas() {
        if (Display.getWidth() != width || Display.getHeight() != height) {
            bitmap.close()
            canvas.close()
            canvas = createCanvas()
            width = Display.getWidth()
            height = Display.getHeight()
            println("REFRESH CANVAS")
        }
    }

    fun printCanvas() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
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
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bitmap.width, bitmap.height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer)
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    }
}