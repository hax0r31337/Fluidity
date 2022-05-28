package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation

class UnbypassableAura : Module("UnbypassableAura", "KillAura like bedrock", ModuleCategory.COMBAT) {
    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if ((!entity.equals(mc.thePlayer)) && entity is EntityLivingBase && mc.thePlayer.getDistanceToEntity(entity) < 5) {
                mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
                mc.netHandler.addToSendQueue(C0APacketAnimation())
            }
        }
    }
}