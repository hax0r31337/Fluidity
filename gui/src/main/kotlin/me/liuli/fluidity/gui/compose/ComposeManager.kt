/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.asCoroutineDispatcher
import me.liuli.fluidity.util.client.logInfo
import me.liuli.fluidity.util.mc
import org.jetbrains.skia.*
import org.jetbrains.skiko.currentNanoTime
import org.lwjgl.BufferChecks
import org.lwjgl.opengl.ContextCapabilities
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class ComposeManager(private var width: Int, private var height: Int, content: @Composable () -> Unit, private val backgroundColor: Int = Color.TRANSPARENT) {

    lateinit var scene: ComposeScene
        private set
    lateinit var pixmap: Pixmap
    lateinit var pixData: Data
    lateinit var surface: Surface
        private set
    private val drawLock = ReentrantLock()
    private var queueDraw = false
    private var queueRefreshTexture = false

    val texId: Int

    init {
        exec {
            surface = createSurface(width, height)
            scene = ComposeScene(coroutineContext) {
                queueDraw = true
            }.apply {
                setContent(content)
                constraints = Constraints(maxWidth = width, maxHeight = height)
            }
        }
        texId = GL11.glGenTextures()
    }

    fun finalize() {
        exec {
            if (this::scene.isInitialized) scene.close()
            if (this::surface.isInitialized && !surface.isClosed) surface.close()
            if (this::pixmap.isInitialized && !pixmap.isClosed) pixmap.close()
            if (this::pixData.isInitialized && !pixData.isClosed) pixData.close()
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

    private fun createSurface(width: Int, height: Int): Surface {
        if (this::pixmap.isInitialized && !pixmap.isClosed) pixmap.close()
        if (this::pixData.isInitialized && !pixData.isClosed) pixData.close()
        if (this::surface.isInitialized && !surface.isClosed) surface.close()

        val info = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
        pixData = Data.makeUninitialized(width * height * 4)
        logInfo("created data with size ${pixData.size}")
        pixmap = Pixmap.make(info, pixData, info.minRowBytes)
        return Surface.makeRasterDirect(pixmap)
    }

    fun resizeCanvas(widthNow: Int, heightNow: Int): Boolean {
        if (widthNow != width || heightNow != height) {
            drawLock.withLock {
                surface = createSurface(widthNow, heightNow)
                scene.constraints = Constraints(maxWidth = widthNow, maxHeight = heightNow)
                width = widthNow
                height = heightNow
            }
            return true
        }
        return false
    }

    fun drawCanvas() {
        if (!this::scene.isInitialized || !this::pixmap.isInitialized || !this::pixData.isInitialized) return

        drawLock.withLock {
            val canvas = surface.canvas
            canvas.clear(backgroundColor)
            scene.render(canvas, System.nanoTime())
            queueRefreshTexture = true
        }
    }

    fun updateCanvas(widthNow: Int, heightNow: Int) {
        if (!this::scene.isInitialized) return

        if (resizeCanvas(widthNow, heightNow)) {
            queueRefreshTexture = false
            exec {
                drawCanvas()
            }
            return
        }
        if (queueRefreshTexture) {
            drawLock.withLock {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId)

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
                nglTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, pixmap.info.width, pixmap.info.height,
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixData.writableData())

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
            }
            queueRefreshTexture = false
        }
        if (queueDraw) {
            exec {
                drawCanvas()
            }
            queueDraw = false
        }
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor { Thread(it, "Compose thread") }
        private val coroutineContext = executor.asCoroutineDispatcher()
        private val gl11_nglTexImage2D = (GL11::class.java.declaredMethods.firstOrNull { it.name == "nglTexImage2D" } ?: throw NoSuchMethodException("GL11.nglTexImage2D")).also {
            it.isAccessible = true
        }
        private val cap_glTexImage2D = ContextCapabilities::class.java.getDeclaredField("glTexImage2D").also {
            it.isAccessible = true
        }

        fun exec(func: () -> Unit) {
            executor.execute(func)
        }

        fun nglTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Long) {
            val caps = GLContext.getCapabilities()
            val function_pointer = cap_glTexImage2D.get(caps) as Long
            BufferChecks.checkFunctionAddress(function_pointer)
            gl11_nglTexImage2D.invoke(null, target, level, internalformat, width, height, border, format, type, pixels, function_pointer)
        }
    }
}