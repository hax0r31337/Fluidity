package me.liuli.fluidity.util.client

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Listener
import me.liuli.fluidity.event.Render2DEvent
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.drawCenteredString

object Debugger : Listener {

    @Listen
    fun onRender2D(event: Render2DEvent) {
        if (!mc.gameSettings.showDebugInfo) return

        val string = "Fluidity(${Fluidity.VERSION}) - ${Fluidity.eventManager.timeCost}ns(${String.format("%.2f", (Fluidity.eventManager.timeCost / 1e+5) / 100f)}%)"
        mc.fontRendererObj.drawCenteredString(string, event.scaledResolution.scaledWidth / 2f, 15f, 0xFFFFFF)
    }

    override fun listen() = true
}