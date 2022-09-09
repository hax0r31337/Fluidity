package me.liuli.fluidity.gui.screen

import me.liuli.fluidity.gui.compose.screen.App
import me.liuli.fluidity.gui.compose.gui.GuiComposeDynamic
import me.liuli.fluidity.util.render.rainbow
import net.minecraft.client.gui.GuiButton
import java.awt.Color

class GuiMainMenu : GuiComposeDynamic() {

    init {
        content = { App() }
    }

//    override fun initGui() {
//        this.buttonList.add(GuiButton(1, this.width / 2 - 100, height - 40, "Ok"))
//    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}