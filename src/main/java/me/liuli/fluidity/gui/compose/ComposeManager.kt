package me.liuli.fluidity.gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.liuli.fluidity.util.mc
import org.jetbrains.skia.*
import org.jetbrains.skiko.currentNanoTime
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer
import java.util.concurrent.Executors


class ComposeManager(private var width: Int, private var height: Int, content: @Composable () -> Unit) {

    lateinit var scene: ComposeScene
        private set
    lateinit var bitmap: Bitmap
    var canvas = createCanvas(width, height)
        private set
    private var textureNew: ByteBuffer? = null
    private var hasRenderUpdate = false

    val texId: Int

    init {
        exec {
            scene = ComposeScene(coroutineContext) { hasRenderUpdate = true }.apply {
                setContent(content)
                constraints = Constraints(maxWidth = width, maxHeight = height)
            }
        }
        texId = GL11.glGenTextures()
    }

    fun awaitSceneLoad() {
        while (!this::scene.isInitialized) {
            Thread.sleep(1L)
        }
    }

    fun finalize() {
        exec {
            if (this::scene.isInitialized) {
                scene.close()
            }
            if (!canvas.isClosed) canvas.close()
            if (this::bitmap.isInitialized) bitmap.close()
        }
        mc.addScheduledTask {
            GL11.glDeleteTextures(texId)
        }
    }

    fun sendPointerEvent(
        eventType: PointerEventType,
        position: Offset,
        scrollDelta: Offset = Offset(0f, 0f),
        timeMillis: Long = (currentNanoTime() / 1E6).toLong(),
        type: PointerType = PointerType.Mouse,
        buttons: PointerButtons? = null,
        keyboardModifiers: PointerKeyboardModifiers? = null,
        nativeEvent: Any? = null,
        button: PointerButton? = null
    ) {
        if (!this::scene.isInitialized) return

        exec {
            scene.sendPointerEvent(eventType, position, scrollDelta, timeMillis, type, buttons, keyboardModifiers, nativeEvent, button)
        }
    }

    fun sendKeyEvent(event: KeyEvent) {
        if (!this::scene.isInitialized) return

        exec {
            scene.sendKeyEvent(event)
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
            canvas = createCanvas(widthNow, heightNow)
            scene.constraints = Constraints(maxWidth = widthNow, maxHeight = heightNow)
            width = widthNow
            height = heightNow
        }
    }

    fun printCanvas() {
        if (!this::scene.isInitialized || !this::bitmap.isInitialized) return

        canvas.clear(Color.TRANSPARENT)
        scene.render(canvas, System.nanoTime())

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

    fun updateCanvas(widthNow: Int, heightNow: Int) {
        if (!this::scene.isInitialized) return

        if (hasRenderUpdate) {
            resizeCanvas(widthNow, heightNow)
            exec {
                printCanvas()
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
        private val executor = Executors.newSingleThreadExecutor()
        private val coroutineContext = executor.asCoroutineDispatcher()

        fun exec(func: () -> Unit) {
            executor.execute(func)
        }
    }
}