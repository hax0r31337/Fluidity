package me.liuli.fluidity.gui.compose.gui

import androidx.compose.runtime.Composable
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.unit.Constraints
import me.liuli.fluidity.gui.compose.ComposeManager
import net.minecraft.client.gui.GuiScreen
import org.jetbrains.skia.Color
import org.jetbrains.skiko.FrameDispatcher
import org.lwjgl.opengl.Display

abstract class AbstractGuiCompose : GuiScreen() {

    lateinit var composeScene: ComposeScene
    lateinit var frameDispatcher: FrameDispatcher

    open fun initCompose(content: @Composable () -> Unit) {
        frameDispatcher = FrameDispatcher(ComposeManager.coroutineContext) { onComposeFrame() }
        composeScene = ComposeScene(ComposeManager.coroutineContext, invalidate = frameDispatcher::scheduleFrame)
        composeScene.setContent(content)
    }

    open fun closeCompose() {
        composeScene.close()
        frameDispatcher.cancel()
    }

    open fun onComposeFrame() {
        ComposeManager.surface.canvas.clear(Color.WHITE)
        composeScene.constraints = Constraints(maxWidth = Display.getWidth(), maxHeight = Display.getHeight())
        composeScene.render(ComposeManager.surface.canvas, System.nanoTime())
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        ComposeManager.context.flush()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}