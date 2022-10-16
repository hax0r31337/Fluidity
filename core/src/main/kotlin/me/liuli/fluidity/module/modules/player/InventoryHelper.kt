/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.ClickTimer
import me.liuli.fluidity.util.world.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import org.lwjgl.input.Mouse
import java.awt.Color

object InventoryHelper : Module("InventoryHelper", "Helps you sort the inventory", ModuleCategory.PLAYER) {

    private val modeValue = ListValue("Mode", arrayOf("Auto", "Manual", "Visual"), "Visual")
    private val stealChestValue = BoolValue("StealChest", false)
    private val autoCloseValue = BoolValue("AutoClose", false)
    private val autoCloseDelayValue = IntValue("AutoCloseDelay", 300, 50, 500)
    private val clickMaxCpsValue = IntValue("ClickMaxCPS", 4, 1, 20)
    private val clickMinCpsValue = IntValue("ClickMinCPS", 2, 1, 20)
    val usefulColorValue = ColorValue("UsefulColor", Color.GREEN.rgb)
    val garbageColorValue = ColorValue("GarbageColor", Color.RED.rgb)
    private val ignoreVehiclesValue = BoolValue("IgnoreVehicles", false)
    private val onlyPositivePotionValue = BoolValue("OnlyPositivePotion", false)
    private val sortSwordValue = IntValue("SortSword", 0, -1, 8)
    private val sortPickaxeValue = IntValue("SortPickaxe", 5, -1, 8)
    private val sortAxeValue = IntValue("SortAxe", 6, -1, 8)

    val usefulItems = mutableListOf<Slot>()
    val garbageItems = mutableListOf<Slot>()
    val normalItems = mutableListOf<Slot>()

    private val clickTimer = ClickTimer()

    override fun onDisable() {
        usefulItems.clear()
        garbageItems.clear()
        normalItems.clear()
        clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        usefulItems.clear()
        garbageItems.clear()
        normalItems.clear()
        if (mc.currentScreen !is GuiContainer) {
            clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
            return
        }

        val gui = mc.currentScreen as GuiContainer
        val container = gui.inventorySlots
        container.inventorySlots.forEach { slot ->
            val lvl = getItemLevel(slot, container)
            if (lvl == ItemLevel.BETTER) {
                usefulItems.add(slot)
            } else if (lvl == ItemLevel.GARBAGE) {
                garbageItems.add(slot)
            } else if (slot.stack != null) {
                normalItems.add(slot)
            }
        }

        val canClick = clickTimer.canClick() && modeValue.get() != "Visual"
        if (!canClick) {
            return
        }
        val sr = ScaledResolution(mc)
        val mouseX = Mouse.getX() * sr.scaledWidth / mc.displayWidth
        val mouseY = sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight - 1
        if (mc.currentScreen is GuiInventory) {
            // sort inventory
            garbageItems.forEach {
                if (processClick(it, gui, ItemLevel.GARBAGE, mouseX, mouseY)) {
                    clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
                    return
                }
            }
            usefulItems.forEach {
                if (processClick(it, gui, ItemLevel.BETTER, mouseX, mouseY)) {
                    clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
                    return
                }
            }
            normalItems.forEach {
                if (it.slotNumber !in 36..44 && processClick(it, gui, ItemLevel.NORMAL, mouseX, mouseY)) {
                    clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
                    return
                }
            }
            if (autoCloseValue.get()) {
                mc.thePlayer.closeScreen()
            }
        } else if (stealChestValue.get()) {
            // steal items
            usefulItems.forEach {
                if (processSteal(it, gui, mouseX, mouseY)) {
                    clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
                    return
                }
            }
            normalItems.forEach {
                if (processSteal(it, gui, mouseX, mouseY)) {
                    clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
                    return
                }
            }
            if (autoCloseValue.get() && clickTimer.hasTimePassed(autoCloseDelayValue.get())) {
                mc.thePlayer.closeScreen()
            }
        }
    }

    private fun processSteal(slot: Slot, gui: GuiContainer, mouseX: Int, mouseY: Int): Boolean {
        if (slot.slotNumber !in 0..(gui.inventorySlots.inventorySlots.size - 37)) {
            return false
        }
        if (modeValue.get() == "Manual" && !gui.isMouseOverSlot(slot, mouseX, mouseY)) {
            return false
        }

        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, 0, 1, mc.thePlayer)
        return true
    }

    private fun processClick(slot: Slot, gui: GuiContainer, lvl: ItemLevel, mouseX: Int, mouseY: Int): Boolean {
        if (modeValue.get() == "Manual" && !gui.isMouseOverSlot(slot, mouseX, mouseY)) {
            return false
        }

        return if (lvl == ItemLevel.GARBAGE) {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, 4, 4, mc.thePlayer)
            true
        } else if (lvl == ItemLevel.BETTER) {
            val item = slot.stack?.item ?: return false
            if (item is ItemArmor && !(mc.currentScreen is GuiInventory && slot.slotNumber in 5..8)) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, 0, 1, mc.thePlayer)
                true
            } else if (sortSwordValue.get() != -1 && item is ItemSword && slot.slotNumber != 36 + sortSwordValue.get()) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, sortSwordValue.get(), 2, mc.thePlayer)
                true
            } else if (sortPickaxeValue.get() != -1 && item is ItemPickaxe && slot.slotNumber != 36 + sortPickaxeValue.get()) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, sortPickaxeValue.get(), 2, mc.thePlayer)
                true
            } else if (sortAxeValue.get() != -1 && item is ItemAxe && slot.slotNumber != 36 + sortAxeValue.get()) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, sortAxeValue.get(), 2, mc.thePlayer)
                true
            } else {
                false
            }
        } else {
            // TODO: sort normal items
            false
        }
    }

    private fun compNum(a: Float, b: Float, aSlot: Int, bSlot: Int): Boolean {
        return if (a == b) {
            aSlot > bSlot
        } else {
            a < b
        }
    }

    private fun getItemLevel(slot: Slot, container: Container): ItemLevel {
        val stack = slot.stack ?: return ItemLevel.NORMAL
        val item = stack.item ?: return ItemLevel.NORMAL

        return when {
            item is ItemSword || item is ItemTool -> {
                val dmg = stack.getAttackDamage()
                if (container.inventorySlots.none { (it.stack?.item?.javaClass == item.javaClass)
                            && it.stack != stack && compNum(dmg, it.stack.getAttackDamage(), slot.slotNumber, it.slotNumber) }) {
                    ItemLevel.BETTER
                } else {
                    ItemLevel.GARBAGE
                }
            }
            item is ItemArmor -> {
                val prot = stack.getArmorProtection()
                if (container.inventorySlots.none { (it.stack?.item is ItemArmor) && (it.stack.item as ItemArmor).armorType == item.armorType
                            && it.stack != stack && compNum(it.stack.getArmorProtection(), prot, slot.slotNumber, it.slotNumber) }) {
                    ItemLevel.BETTER
                } else {
                    ItemLevel.GARBAGE
                }
            }
            item is ItemBow -> {
                val pwr = stack.getEnchantment(Enchantment.power).toFloat()
                if (container.inventorySlots.none { it.stack?.item is ItemBow && it.stack != stack
                            && compNum(pwr, it.stack.getEnchantment(Enchantment.power).toFloat(), slot.slotNumber, it.slotNumber) }) {
                    ItemLevel.BETTER
                } else {
                    ItemLevel.GARBAGE
                }
            }
            else -> {
                if (item is ItemFood || stack.unlocalizedName == "item.arrow" ||
                        (item is ItemBlock && item.isUsefulBlock()) ||
                        item is ItemSnowball || item is ItemEgg || (item is ItemPotion && (!onlyPositivePotionValue.get() || stack.isPositivePotion())) ||
                        item is ItemEnderPearl || item is ItemBucket || (ignoreVehiclesValue.get() && (item is ItemBoat || item is ItemMinecart))) {
                    ItemLevel.NORMAL
                } else {
                    ItemLevel.GARBAGE
                }
            }
        }
    }

    enum class ItemLevel {
        BETTER,
        NORMAL,
        GARBAGE
    }
}