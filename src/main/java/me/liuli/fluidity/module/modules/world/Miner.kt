package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BlockValue
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import net.minecraft.util.BlockPos

//class Miner : Module("Miner", "Auto mine blocks for you", ModuleCategory.WORLD) {
//
//    private val blockValue = BlockValue("Block", 26)
//    private val rangeValue = FloatValue("Range", 5F, 1F, 7F)
//    private val swingValue = BoolValue("Swing", true)
//    private val rotationsValue = BoolValue("Rotations", true)
//
//    private var pos: BlockPos? = null
//    private var currentDamage = 0F
//
//    override fun onEnable() {
//        pos = null
//    }
//
//    @EventMethod
//    fun onUpdate(event: UpdateEvent) {
//
//    }
//
//}