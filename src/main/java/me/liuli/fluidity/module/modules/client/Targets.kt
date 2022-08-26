package me.liuli.fluidity.module.modules.client

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer

object Targets : Module("Targets", "Target types that can be attacked", ModuleCategory.CLIENT, canToggle = false) {

    private val playerValue = BoolValue("Player", true)
    private val playerTeamValue = BoolValue("PlayerTeam", true)
    private val animalValue = BoolValue("Animal", false)
    private val mobValue = BoolValue("Mob", true)
    private val invisibleValue = BoolValue("Invisible", false)
    private val deadValue = BoolValue("Dead", false)

    private val antibotValue = ListValue("AntiBot", arrayOf("None"), "None")

    fun Entity.isTarget(canAttackCheck: Boolean = true): Boolean {
        if (this is EntityLivingBase && (deadValue.get() || this.isEntityAlive()) && this !== mc.thePlayer) {
            if (invisibleValue.get() || !this.isInvisible()) {
                if (playerValue.get() && this is EntityPlayer) {
                    if (canAttackCheck) {
                        if (this.isBot) {
                            return false
                        }

                        if (this.isSpectator) {
                            return false
                        }

                        if (this.isPlayerSleeping) {
                            return false
                        }

                        if (playerTeamValue.get()) {
                            return !this.isTeammate
                        }

                        return true
                    }

                    return true
                }
                return mobValue.get() && this.isMob || animalValue.get() && this.isAnimal
            }
        }
        return false
    }

    private val EntityPlayer.isTeammate: Boolean
        get() {
            if (mc.thePlayer.displayName != null && this.displayName != null)
                return false

            val targetName = this.displayName.formattedText.replace("§r", "")
            val clientName = mc.thePlayer.displayName.formattedText.replace("§r", "")
            return targetName.startsWith("§${clientName[1]}")
        }

    private val Entity.isAnimal: Boolean
        get() = this is EntityAnimal || this is EntitySquid || this is EntityGolem || this is EntityVillager || this is EntityBat

    private val Entity.isMob: Boolean
        get() = this is EntityMob || this is EntitySlime || this is EntityGhast || this is EntityDragon

    private val EntityPlayer.isBot: Boolean
        get() = false // TODO: implement AntiBot
}