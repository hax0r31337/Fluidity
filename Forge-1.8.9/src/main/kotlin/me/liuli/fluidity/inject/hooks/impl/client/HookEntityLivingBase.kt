package me.liuli.fluidity.inject.hooks.impl.client

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.lastReportedPitch
import me.liuli.fluidity.util.move.lastReportedYaw
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

class HookEntityLivingBase : HookProvider("net.minecraft.entity.EntityLivingBase") {

    @Hook(method = "getLook", type = Hook.Type("ENTER"))
    fun getLook(param: MethodHookParam) {
        if (param.thisObject != mc.thePlayer)
            return

        val yaw = lastReportedYaw
        val pitch = lastReportedPitch

        val f = MathHelper.cos(-yaw * 0.017453292f - 3.1415927f)
        val f1 = MathHelper.sin(-yaw * 0.017453292f - 3.1415927f)
        val f2 = -MathHelper.cos(-pitch * 0.017453292f)
        val f3 = MathHelper.sin(-pitch * 0.017453292f)
        param.result = Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }
}