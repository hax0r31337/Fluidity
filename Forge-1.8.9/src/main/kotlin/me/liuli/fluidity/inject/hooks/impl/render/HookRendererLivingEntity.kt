/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.render

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.module.modules.render.ESP
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.entity.Entity

class HookRendererLivingEntity : HookProvider("net.minecraft.client.renderer.entity.RendererLivingEntity") {

    @Hook(method = "canRenderName", type = Hook.Type("ENTER"))
    fun canRenderName(param: MethodHookParam) {
        if (ESP.state && ESP.nameValue.get() && (param.args[0] as Entity).isTarget(ESP.onlyShowAttackableValue.get()))
            param.result = false
    }
}