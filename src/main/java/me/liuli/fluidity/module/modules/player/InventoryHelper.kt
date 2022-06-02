package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
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
    private val clickMaxCpsValue = IntValue("ClickMaxCPS", 4, 1, 20)
    private val clickMinCpsValue = IntValue("ClickMinCPS", 2, 1, 20)
    private val usefulRedValue = IntValue("Useful-Red", 0, 0, 255)
    private val usefulGreenValue = IntValue("Useful-Green", 255, 0, 255)
    private val usefulBlueValue = IntValue("Useful-Blue", 0, 0, 255)
    private val garbageRedValue = IntValue("Garbage-Red", 255, 0, 255)
    private val garbageGreenValue = IntValue("Garbage-Green", 0, 0, 255)
    private val garbageBlueValue = IntValue("Garbage-Blue", 0, 0, 255)
    private val ignoreVehiclesValue = BoolValue("IgnoreVehicles", false)
    private val onlyPositivePotionValue = BoolValue("OnlyPositivePotion", false)
    private val sortSwordValue = IntValue("SortSword", 0, -1, 8)
    private val sortPickaxeValue = IntValue("SortPickaxe", 5, -1, 8)
    private val sortAxeValue = IntValue("SortAxe", 6, -1, 8)

    val usefulItems = mutableListOf<Slot>()
    val garbageItems = mutableListOf<Slot>()
    val normalItems = mutableListOf<Slot>()

    var usefulItemColor = 0
    var garbageItemColor = 0

    private val clickTimer = ClickTimer()

    override fun onDisable() {
        usefulItems.clear()
        garbageItems.clear()
        normalItems.clear()
        clickTimer.update(clickMinCpsValue.get(), clickMaxCpsValue.get())
    }

    @EventMethod
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
            }
        }
        usefulItemColor = Color(usefulRedValue.get(), usefulGreenValue.get(), usefulBlueValue.get()).rgb
        garbageItemColor = Color(garbageRedValue.get(), garbageGreenValue.get(), garbageBlueValue.get()).rgb

        val canClick = clickTimer.canClick() && modeValue.get() != "Visual"
        if (!canClick) {
            return
        }
        val sr = ScaledResolution(mc)
        val mouseX = Mouse.getX() * sr.scaledWidth / mc.displayWidth
        val mouseY = sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight - 1
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

    private fun getItemLevel(slot: Slot, container: Container): ItemLevel {
        val stack = slot.stack ?: return ItemLevel.NORMAL
        val item = stack.item ?: return ItemLevel.NORMAL

        return when {
            item is ItemSword || item is ItemTool -> {
                val dmg = stack.getAttackDamage()
                if (container.inventorySlots.none { (it.stack?.item is ItemSword || it.stack?.item is ItemTool)
                            && it.stack != stack && dmg < it.stack.getAttackDamage() }) {
                    ItemLevel.BETTER
                } else {
                    ItemLevel.GARBAGE
                }
            }
            item is ItemArmor -> {
                val prot = stack.getArmorProtection()
                if (container.inventorySlots.none { (it.stack?.item is ItemArmor) && (it.stack.item as ItemArmor).armorType == item.armorType
                            && it.stack != stack && prot > it.stack.getArmorProtection() }) {
                    ItemLevel.BETTER
                } else {
                    ItemLevel.GARBAGE
                }
            }
            item is ItemBow -> {
                val pwr = stack.getEnchantment(Enchantment.power)
                if (container.inventorySlots.none { it.stack?.item is ItemBow && it.stack != stack && pwr < it.stack.getEnchantment(
                        Enchantment.power) }) {
                    ItemLevel.BETTER
                } else {
                    ItemLevel.GARBAGE
                }
            }
            else -> {
                if (item is ItemFood || stack.unlocalizedName == "item.arrow" ||
                        (item is ItemBlock && item.isUsefulBlock()) ||
                        item is ItemBed || (item is ItemPotion && (!onlyPositivePotionValue.get() || stack.isPositivePotion())) ||
                        item is ItemEnderPearl || item is ItemBucket || ignoreVehiclesValue.get() && (item is ItemBoat || item is ItemMinecart)) {
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