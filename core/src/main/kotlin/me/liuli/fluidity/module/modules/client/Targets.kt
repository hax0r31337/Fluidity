/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.client

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.WorldEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.misc.DungeonAssist
import me.liuli.fluidity.module.special.CombatManager
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
import net.minecraft.network.play.server.S0CPacketSpawnPlayer
import net.minecraft.network.play.server.S13PacketDestroyEntities

object Targets : Module("Targets", "Target types that can be attacked", ModuleCategory.CLIENT, canToggle = false) {

    private val playerValue by BoolValue("Player", true)
    private val playerTeamValue by BoolValue("PlayerTeam", true)
    private val animalValue by BoolValue("Animal", false)
    private val mobValue by BoolValue("Mob", true)
    private val invisibleValue by BoolValue("Invisible", false)
    private val deadValue by BoolValue("Dead", false)

    private val antibotValue by ListValue("AntiBot", arrayOf("None", "Custom", "SkyBlockDungeon"), "None")
    private val antibotResetValue by object : BoolValue("AntiBot-Reset", false) {
        override fun set(newValue: Boolean) {
            antibotReset()
        }
    }
    private val antibotSpawnInCombatValue by BoolValue("AntiBot-SpawnInCombat", false)

    private val spawnList = mutableListOf<Int>()
    private val spawnInCombatList = mutableListOf<Int>()

    fun antibotReset() {
        spawnList.clear()
        spawnInCombatList.clear()
    }

    private fun isCustomMatchedBot(entity: EntityLivingBase): Boolean {
        val id = entity.entityId

        if (antibotSpawnInCombatValue && spawnInCombatList.contains(id)) {
            return true
        }

        return false
    }

    override fun onEnable() {
        antibotReset()
    }

    @Listen
    fun onWorld(event: WorldEvent) {
        antibotReset()
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S0CPacketSpawnPlayer) {
            if (!spawnList.contains(packet.entityID)) {
                spawnList.add(packet.entityID)
                if (CombatManager.hasTarget) {
                    spawnInCombatList.add(packet.entityID)
                }
            }
        } else if (packet is S13PacketDestroyEntities) {
            packet.entityIDs.forEach {
                if (mc.theWorld.getEntityByID(it) is EntityPlayer)
                    spawnInCombatList.remove(it)
            }
        }
    }

    fun Entity.isTarget(canAttackCheck: Boolean = true): Boolean {
        if (this is EntityLivingBase && (deadValue || this.isEntityAlive()) && this !== mc.thePlayer) {
            if (invisibleValue || !this.isInvisible()) {
                if (this.isBot) {
                    return false
                }

                if (playerValue && this is EntityPlayer) {
                    if (canAttackCheck) {

                        if (this.isSpectator) {
                            return false
                        }

                        if (this.isPlayerSleeping) {
                            return false
                        }

                        if (playerTeamValue) {
                            return !this.isTeammate
                        }

                        return true
                    }

                    return true
                }
                return mobValue && this.isMob || animalValue && this.isAnimal
            }
        }
        return false
    }

    val EntityPlayer.isTeammate: Boolean
        get() {
            if (mc.thePlayer.displayName != null && this.displayName != null)
                return false

            val targetName = this.displayName.formattedText.replace("§r", "")
            val clientName = mc.thePlayer.displayName.formattedText.replace("§r", "")
            return targetName.startsWith("§${clientName[1]}")
        }

    val Entity.isAnimal: Boolean
        get() = this is EntityAnimal || this is EntitySquid || this is EntityGolem || this is EntityVillager || this is EntityBat

    val Entity.isMob: Boolean
        get() = this is EntityMob || this is EntitySlime || this is EntityGhast || this is EntityDragon

    val EntityLivingBase.isBot: Boolean
        get() =
            when(antibotValue) {
                "SkyBlockDungeon" -> {
                    if (DungeonAssist.inDungeon) {
                        this is EntityBat || !DungeonAssist.getName(this.entityId).let { it.contains("✯") || it.startsWith("§6") || (it.contains("§d") && it.contains("§l")) }
                    } else false
                }
                "Custom" -> isCustomMatchedBot(this)
                else -> false
            }

    override fun listen() = true
}