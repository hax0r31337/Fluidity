package me.liuli.fluidity.inject.hooks.impl.entity

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.AttackEvent
import me.liuli.fluidity.event.ClickBlockEvent
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.module.modules.combat.Reach
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.entity.Entity
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class HookPlayerControllerMP : HookProvider("net.minecraft.client.multiplayer.PlayerControllerMP") {

    @Hook(method = "attackEntity", type = Hook.Type("ENTER"))
    fun attackEntity(param: MethodHookParam) {
        val targetEntity = (param.args[1] ?: return) as Entity

        val event = AttackEvent(targetEntity)
        Fluidity.eventManager.call(event)
        if (event.cancelled) {
            param.result = null
        }
    }

    @Hook(method = "onPlayerRightClick", type = Hook.Type("ENTER"))
    fun onPlayerRightClick(param: MethodHookParam) {
        Fluidity.eventManager.call(ClickBlockEvent(ClickBlockEvent.Type.RIGHT, param.args[3] as BlockPos?, param.args[4] as EnumFacing?))
    }

    @Hook(method = "getBlockReachDistance", type = Hook.Type("ENTER"))
    fun getBlockReachDistance(param: MethodHookParam) {
        if (Reach.state) {
            param.result = Reach.buildReachValue.get()
        }
    }
}