package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.MotionEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.ClickTimer

class TriggerBot : Module("TriggerBot", "Automatically attack the target you view", ModuleCategory.COMBAT) {

    private val minCpsValue = IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue = IntValue("MaxCPS", 12, 1, 20)
    private val swingItemValue = BoolValue("SwingItem", true)

    private val clickTimer = ClickTimer()

    @EventMethod
    fun onPreMotion(event: MotionEvent) {
        if (clickTimer.canClick() && mc.objectMouseOver?.entityHit != null) {
            if (swingItemValue.get()) {
                mc.thePlayer.swingItem()
            }
            mc.playerController.attackEntity(mc.thePlayer,  mc.objectMouseOver.entityHit)
            clickTimer.update(minCpsValue.get(), maxCpsValue.get())
        }
    }
}