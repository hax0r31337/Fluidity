package me.liuli.fluidity.gui.compose.gui

import androidx.compose.runtime.Composable
import org.jetbrains.skia.Color

open class GuiComposeDynamic(contentIn: (@Composable () -> Unit)? = null, backgroundColor: Int = Color.WHITE, repeatKeys: Boolean = true) : AbstractGuiCompose(backgroundColor, repeatKeys) {

    protected lateinit var content: @Composable () -> Unit

    init {
        contentIn?.let {
            content = contentIn
        }
    }

    override fun initGui() {
        initCompose(content)
        super.initGui()
    }

    override fun onGuiClosed() {
        closeCompose()
        super.onGuiClosed()
    }
}