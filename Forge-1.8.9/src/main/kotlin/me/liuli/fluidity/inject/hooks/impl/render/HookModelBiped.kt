/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.render

import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.lastReportedPitch
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.client.model.ModelBiped

class HookModelBiped : HookProvider("net.minecraft.client.model.ModelBiped") {

    @Hook(method = "setRotationAngles", type = Hook.Type("INVOKE", "net/minecraft/client/model/ModelBiped;copyModelAngles(Lnet/minecraft/client/model/ModelRenderer;Lnet/minecraft/client/model/ModelRenderer;)V", "net/minecraft/client/model/ModelBase"))
    fun setRotationAngles(param: MethodHookParam) {
        val entity = param.args[6]
        if (entity == mc.thePlayer) {
            (param.thisObject as ModelBiped).bipedHead.rotateAngleX = lastReportedPitch / (180f / Math.PI.toFloat())
        }
    }
}