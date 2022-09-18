package me.liuli.fluidity.util.world

import me.liuli.fluidity.util.mc
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

fun getBlockName(id: Int): String = Block.getBlockById(id).localizedName

fun Vec3.getBlock(): Block? = BlockPos(this).getBlock()

fun BlockPos.getBlock(): Block? = mc.theWorld?.getBlockState(this)?.block

fun BlockPos.getCenterDistance() =
    mc.thePlayer!!.getDistance(this.x + 0.5, this.y + 0.5, this.z + 0.5)