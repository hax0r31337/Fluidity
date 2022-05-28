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

    fun isTarget(entity: Entity, canAttackCheck: Boolean = true): Boolean {
        if (entity is EntityLivingBase && (deadValue.get() || entity.isEntityAlive()) && entity !== mc.thePlayer) {
            if (invisibleValue.get() || !entity.isInvisible()) {
                if (playerValue.get() && entity is EntityPlayer) {
                    if (canAttackCheck) {
                        if (isBot(entity)) {
                            return false
                        }

                        if (entity.isSpectator) {
                            return false
                        }

                        if (entity.isPlayerSleeping) {
                            return false
                        }

                        if (playerTeamValue.get()) {
                            return !isTeammate(entity)
                        }

                        return true
                    }

                    return true
                }
                return mobValue.get() && isMob(entity) || animalValue.get() && isAnimal(entity)
            }
        }
        return false
    }

    private fun isTeammate(entity: EntityPlayer): Boolean {
        if (mc.thePlayer.displayName != null && entity.displayName != null)
            return false

        val targetName = entity.displayName.formattedText.replace("§r", "")
        val clientName = mc.thePlayer.displayName.formattedText.replace("§r", "")
        return targetName.startsWith("§${clientName[1]}")
    }

    private fun isAnimal(entity: Entity): Boolean {
        return entity is EntityAnimal || entity is EntitySquid || entity is EntityGolem || entity is EntityVillager || entity is EntityBat
    }

    private fun isMob(entity: Entity): Boolean {
        return entity is EntityMob || entity is EntitySlime || entity is EntityGhast || entity is EntityDragon
    }

    private fun isBot(entity: EntityPlayer): Boolean {
        return false // TODO: implement AntiBot
    }
}