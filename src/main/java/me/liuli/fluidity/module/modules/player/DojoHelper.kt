package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.AttackEvent
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.pathfinder.Pathfinder
import me.liuli.fluidity.pathfinder.PathfinderSimulator
import me.liuli.fluidity.pathfinder.goals.GoalBlock
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.floorPosition
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos

class DojoHelper : Module("DojoHelper", "Hypixel SkyBlock", ModuleCategory.PLAYER) {

    private val modeValue = ListValue("Mode", arrayOf("Swift", "Discipline"), "Swift")

    private val currentGoodBlocks = mutableListOf<BlockPos>()
    private var serverSlot = 0

    override fun onEnable() {
        mc.thePlayer ?: return

        serverSlot = mc.thePlayer.inventory.currentItem
    }

    override fun onDisable() {
        currentGoodBlocks.clear()

        if (Pathfinder.stateGoal != null) {
            Pathfinder.stateGoal = null
            Pathfinder.resetPath(true)
        }
    }

    @Listen
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity

        when (modeValue.get()) {
            "Discipline" -> {
                val slot = disciplinePickSlot(target)
                if (slot != -1 && serverSlot != slot) {
                    event.cancel()
                }
            }
        }
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        when (modeValue.get()) {
            "Swift" -> {
                mc.gameSettings.keyBindSneak.pressed = false
                if (!mc.gameSettings.keyBindJump.pressed && !mc.gameSettings.keyBindSprint.pressed) {
                    var drop = false
                    PathfinderSimulator.simulateUntil({
                        if (!it.onGround) {
                            drop = true
                            true
                        } else {
                            false
                        }
                    }, { _, _, -> }, 20)
                }
                for (block in currentGoodBlocks.map { it }) {
                    val state = mc.theWorld.getBlockState(block)
                    if (state.toString() != "minecraft:wool[color=lime]") {
                        currentGoodBlocks.remove(block)
                    }
                }
                if (currentGoodBlocks.isNotEmpty()) {
                    val last = currentGoodBlocks.last().up()
                    if (Pathfinder.stateGoal == null || Pathfinder.stateGoal !is GoalBlock
                        || (Pathfinder.stateGoal as GoalBlock).let { last.x != it.x || last.y != it.y || last.z != it.z }) {
                        Pathfinder.setGoal(GoalBlock(last.x, last.y, last.z))
                    }
                } else if (Pathfinder.stateGoal != null) {
                    Pathfinder.stateGoal = null
                    Pathfinder.resetPath(true)
                }
            }
            "Discipline" -> {
                val target = mc.objectMouseOver?.entityHit ?: return
                val slot = disciplinePickSlot(target)
                if (slot != -1 && mc.thePlayer.inventory.currentItem != slot) {
                    mc.thePlayer.inventory.currentItem = slot
                }
            }
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            state = false // disable after teleport
        }

        when (modeValue.get()) {
            "Swift" -> {
                if (packet is S23PacketBlockChange) {
                    swiftDojoBlockProc(packet.blockState, packet.blockPosition)
                    if (packet.blockState.toString().contains("air")) {
                        event.cancel()
                    }
                } else if (packet is S22PacketMultiBlockChange) {
                    packet.changedBlocks.forEach {
                        swiftDojoBlockProc(it.blockState, it.pos)
                    }
                }
            }
            "Discipline" -> {
                if (packet is C09PacketHeldItemChange) {
                    serverSlot = packet.slotId
                }
            }
        }
    }

    private fun disciplinePickSlot(target: Entity): Int {
        if (target !is EntityZombie) {
            return -1
        }
        val helmet = (target.inventory[4] ?: return -1).unlocalizedName
        return when(helmet) {
            "item.helmetCloth" -> 0
            "item.helmetIron" -> 1
            "item.helmetGold" -> 2
            "item.helmetDiamond" -> 3
            else -> -1
        }
    }

    private fun swiftDojoBlockProc(state: IBlockState, pos: BlockPos) {
        if (pos.distanceSq(mc.thePlayer.floorPosition) < 100 && state.toString() == "minecraft:wool[color=lime]" && !currentGoodBlocks.contains(pos)) {
            currentGoodBlocks.add(pos)
        }
    }
}