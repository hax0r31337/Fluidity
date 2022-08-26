package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.world.EntitySimulatable

class Test : Module("Test", "T", ModuleCategory.MISC) {

    private var fakePlayer: EntitySimulatable? = null

    override fun onEnable() {
        mc.thePlayer ?: return

        val playerMP = EntitySimulatable(mc.theWorld)
        playerMP.copyLocationAndAnglesFrom(mc.thePlayer)

        mc.theWorld.addEntityToWorld(-1000, playerMP)

        fakePlayer = playerMP
    }

    override fun onDisable() {
        mc.thePlayer ?: return

        mc.theWorld.removeEntity(fakePlayer ?: return)
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        val p = fakePlayer ?: return

        p.isSprinting = true
        p.moveForward = 1f
//        p.moveStrafing = 1f
        p.jump = true
//        repeat(1000) {
//            p.onUpdate()
//        }
    }
}