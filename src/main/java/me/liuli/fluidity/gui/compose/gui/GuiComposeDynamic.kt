package me.liuli.fluidity.gui.compose.gui

import androidx.compose.runtime.Composable
import me.liuli.fluidity.gui.compose.IDisplayable
import org.jetbrains.skia.Color

open class GuiComposeDynamic(backgroundColor: Int = Color.WHITE, repeatKeys: Boolean = true, displayable: IDisplayable? = null) : AbstractGuiCompose(backgroundColor, repeatKeys) {

    protected lateinit var content: @Composable () -> Unit


    init {
        if (displayable != null) {
            content = { displayable.display() }
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