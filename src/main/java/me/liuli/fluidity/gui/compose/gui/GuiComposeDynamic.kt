package me.liuli.fluidity.gui.compose.gui

import androidx.compose.runtime.Composable
import me.liuli.fluidity.gui.compose.IDisplayable

open class GuiComposeDynamic(drawBackground: Boolean = true, displayable: IDisplayable? = null) : AbstractGuiCompose(drawBackground) {

    protected lateinit var content: @Composable () -> Unit


    init {
        if (displayable != null) {
            content = { displayable.display() }
        }
    }

    override fun initGui() {
        initCompose(content)
    }

    override fun onGuiClosed() {
        closeCompose()
    }
}