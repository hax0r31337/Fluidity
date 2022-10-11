package me.liuli.fluidity.inject.hooks.impl.entity

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.StrafeEvent
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.module.modules.combat.HitBox
import me.liuli.fluidity.util.mc
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.entity.Entity

class HookEntity : HookProvider("net.minecraft.entity.Entity") {

    @Hook(method = "moveFlying", type = Hook.Type("ENTER"))
    fun moveFlying(param: MethodHookParam) {
        if (param.thisObject != mc.thePlayer)
            return

        val strafeEvent = StrafeEvent(param.args[0] as Float, param.args[1] as Float, param.args[2] as Float)
        Fluidity.eventManager.call(strafeEvent)

        if (strafeEvent.cancelled)
            param.result = null
    }

    @Hook(method = "getCollisionBorderSize", type = Hook.Type("ENTER"))
    fun getCollisionBorderSize(param: MethodHookParam) {
        if (HitBox.state && (param.thisObject as Entity).isTarget(true))
            param.result = 0.1f + HitBox.sizeValue.get()
    }
}