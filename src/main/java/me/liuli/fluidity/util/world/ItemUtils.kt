package me.liuli.fluidity.util.world

import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.potion.Potion


fun ItemStack.getEnchantment(enchantment: Enchantment): Int {
    if (this.enchantmentTagList == null || this.enchantmentTagList.hasNoTags()) return 0
    for (i in 0 until this.enchantmentTagList.tagCount()) {
        val tagCompound = this.enchantmentTagList.getCompoundTagAt(i)
        if (tagCompound.hasKey("ench") && tagCompound.getShort("ench").toInt() == enchantment.effectId ||
            tagCompound.hasKey("id") && tagCompound.getShort("id").toInt() == enchantment.effectId) {
            return tagCompound.getShort("lvl").toInt()
        }
    }
    return 0
}

fun ItemStack.getAttackDamage(): Float {
    return (this.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount ?: 0.0).toFloat() +
            1.25f * this.getEnchantment(Enchantment.sharpness) +
            1.5f * this.getEnchantment(Enchantment.fireAspect)
}

private val armorDamageReduceEnchantments = mapOf<Enchantment, Float>(Pair(Enchantment.protection, 0.06f), Pair(Enchantment.projectileProtection, 0.032f), Pair(Enchantment.fireProtection, 0.0585f), Pair(Enchantment.blastProtection, 0.0304f))

fun ItemStack.getArmorProtection(): Float {
    val item = this.item as ItemArmor
    var sum = 0.0f
    for ((enchantment, threshold) in armorDamageReduceEnchantments) {
        sum += this.getEnchantment(enchantment) * threshold
    }
    return getArmorDamageReduction(item.armorMaterial.getDamageReductionAmount(item.armorType), 0) * (1 - sum)
}

private fun getArmorDamageReduction(defensePoints: Int, toughness: Int): Float {
    return 1 - 20.0f.coerceAtMost((defensePoints / 5.0f).coerceAtLeast(defensePoints - 1 / (2 + toughness / 4.0f))) / 25.0f
}

private val blackListBlocks = listOf(Blocks.enchanting_table, Blocks.chest, Blocks.ender_chest, Blocks.trapped_chest,
    Blocks.anvil, Blocks.sand, Blocks.web, Blocks.torch, Blocks.crafting_table, Blocks.furnace, Blocks.waterlily,
    Blocks.dispenser, Blocks.stone_pressure_plate, Blocks.wooden_pressure_plate, Blocks.red_flower, Blocks.flower_pot, Blocks.yellow_flower,
    Blocks.noteblock, Blocks.dropper, Blocks.standing_banner, Blocks.wall_banner)

fun Item.isUsefulBlock(): Boolean {
    if (this !is ItemBlock) return false
    return !blackListBlocks.contains(this.block)
}

private val positivePotions = arrayOf(Potion.regeneration.id, Potion.moveSpeed.id, Potion.heal.id, Potion.nightVision.id,
    Potion.jump.id, Potion.invisibility.id, Potion.resistance.id, Potion.waterBreathing.id,
    Potion.absorption.id, Potion.digSpeed.id, Potion.damageBoost.id, Potion.healthBoost.id,
    Potion.fireResistance.id)

fun ItemStack.isPositivePotion(): Boolean {
    if (this.item !is ItemPotion) return false

    (this.item as ItemPotion).getEffects(this).forEach {
        if (positivePotions.contains(it.potionID)) {
            return true
        }
    }

    return false
}
