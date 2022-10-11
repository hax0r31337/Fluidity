package me.liuli.fluidity.inject.hooks.impl.render

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.lastReportedPitch
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.model.ModelBiped
import net.minecraft.entity.player.EntityPlayer

class HookModelBiped : HookProvider("net.minecraft.client.model.ModelBiped") {

    @Hook(method = "setRotationAngles", type = Hook.Type("FIELD", "net/minecraft/client/model/ModelBiped;swingProgress"))
    fun setRotationAngles(param: MethodHookParam) {
        val entity = param.args[6]
        if (entity is EntityPlayer && entity == mc.thePlayer) {
            (param.thisObject as ModelBiped).bipedHead.rotateAngleX = lastReportedPitch / (180f / Math.PI.toFloat())
        }
    }
}