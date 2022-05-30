package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.settings.GameSettings

// Not compiled, built by BRAIN-DEVELOPMENT-KIT XD
class InventoryMove : Module("InventoryMove", "Allows you to walk while opening the inventory.", ModuleCategory.MOVEMENT) {

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
         if (mc.currentScreen !is GuiChat && mc.currentScreen !is GuiIngameMenu && mc.currentScreen !is GuiEditSign) {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint)
        }
    }
}
