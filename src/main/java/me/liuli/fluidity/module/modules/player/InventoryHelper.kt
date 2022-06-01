package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory

class InventoryHelper : Module("InventoryHelper", "Helps you sort the inventory", ModuleCategory.PLAYER) {

    @EventMethod
    fun onUpdate(event: UpdateEvent) {

    }
}