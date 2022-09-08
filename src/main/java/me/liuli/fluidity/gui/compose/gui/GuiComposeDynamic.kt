package me.liuli.fluidity.gui.compose.gui

import androidx.compose.runtime.Composable

open class GuiComposeDynamic : AbstractGuiCompose() {

    protected lateinit var content: @Composable () -> Unit

    override fun initGui() {
        initCompose(content)
    }

    override fun onGuiClosed() {
        closeCompose()
    }
}