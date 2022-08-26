package me.liuli.fluidity.pathfinder

import me.liuli.fluidity.pathfinder.path.PathMove
import me.liuli.fluidity.pathfinder.path.PathBlock
import me.liuli.fluidity.pathfinder.path.PathBreakInfo
import me.liuli.fluidity.pathfinder.path.PathPlaceInfo
import me.liuli.fluidity.util.mc
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockDoor
import net.minecraft.block.BlockFalling
import net.minecraft.block.BlockFire
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockStone
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import kotlin.math.sqrt

class PathfinderController(
    /**
     * Total computation timeout.
     */
    val searchTimeout: Int = 10000,
    /**
     * Total computation timeout.
     */
    val searchTickTimeout: Int = 20,
    /**
     * Max distance to search.
     */
    val searchRadius: Int = -1,
    val losWhenPlacingBlocks: Boolean = true,
    /**
     * Boolean to allow breaking blocks
     */
    var canDig: Boolean = false,
    /**
     * Additional cost for breaking blocks.
     */
    var digCost: Float = 1f,
    /**
     * Additional cost for placing blocks.
     */
    var placeCost: Float = 1f,
    /**
     * Additional cost for interacting with liquids.
     */
    var liquidCost: Float = 1f,
    /**
     * Do not break blocks that touch liquid blocks.
     */
    var dontCreateFlow: Boolean = true,
    /**
     * Allow pillaring up on 1x1 towers.
     */
    var allow1by1Towers: Boolean = true,
    /**
     * Allow to walk to the next node/goal in a strait line if terrain allows it.
     */
    var allowFreeMotion: Boolean = true,
    /**
     * Allow parkour jumps like jumps over gaps bigger then 1 block
     */
    var allowParkour: Boolean = true,
    /**
     * Allow sprinting when moving.
     */
    var allowSprinting: Boolean = true,
    /**
     * Do not break blocks that have gravityBlock above.
     */
    var dontMineUnderFallBlock: Boolean = true,
    /**
     * Max drop down distance. Only considers drops that have blocks to land on.
     */
    var maxDropDown: Float = 4f,
    /**
     * Option to ignore maxDropDown distance when the landing position is in water.
     */
    var infiniteLiquidDropdownDistance: Boolean = true,
    /**
     * Option to allow bot to open doors
     */
    var canOpenDoors: Boolean = true) {

    /**
     * is the block can be broken by the bot when [canDig] is enabled
     */
    fun canBreakBlock(block: PathBlock): Boolean {
        return false // TODO
    }

    /**
     * is the bot need to avoid with collide with the block
     */
    fun needAvoidBlock(block: Block): Boolean {
        return block is BlockFire
    }

    /**
     * is the block can be replaced during bot bridging
     */
    fun replaceableBlock(block: Block, blockPos: BlockPos): Boolean {
        return false
    }

    /**
     * is the item can be used during bot bridging
     */
//    fun bridgeableItem(item: ItemStack): Boolean {
//        return false
//    }

    /**
     * @return a positive number (includes 0) that defines extra cost for that specific Block.
     * 0 means no extra cost, 100 means it is impossible for pathfinder to consider this move.
     */
    open fun exclusionBreak(block: PathBlock) = 0f

    /**
     * @return a positive number (includes 0) that defines extra cost for that specific Block.
     * 0 means no extra cost, 100 means it is impossible for pathfinder to consider this move.
     */
    open fun exclusionStep(block: PathBlock) = 0f

    /**
     * @return a positive number (includes 0) that defines extra cost for that specific Block.
     * 0 means no extra cost, 100 means it is impossible for pathfinder to consider this move.
     */
    open fun exclusionPlace(block: PathBlock) = 0f

    /**
     * get height of the block
     */
    open fun getBlockHeight(block: Block, pos: BlockPos, state: IBlockState) = ((block.getCollisionBoundingBox(mc.theWorld, pos, state)?.maxY ?: pos.y.toDouble()) - pos.y)

    /**
     * TODO: search for usable bridge item
     */
//    open fun searchBridgeableItem(): ItemStack? = null

    /**
     * TODO: get amount of bridgeable items
     */
    open fun countBridgeableItems(): Int = 0

    /**
     * TODO: get best harvest item
     */
//    open fun bestHarvestItem(block: Block) = mc.thePlayer.inventory.currentItem

    /**
     * is the block can be walk on safely (like height smaller than 0.1)
     */
    open fun isSafeToWalkOn(block: Block, pos: BlockPos, state: IBlockState): Boolean {
        val height = getBlockHeight(block, pos, state)
        return height <= 0.1
    }

    /**
     * is the block is fence liked (height over 1)
     */
//    open fun isFenceLikedBlock(block: PathBlock): Boolean {
//        val height = getBlockHeight(block)
//        return height > 0.1
//    }

    open fun getBlockAt(x: Int, y: Int, z: Int): PathBlock {
        val pos = BlockPos(x, y, z)
        val block = mc.theWorld.getBlockState(pos)
            ?: return PathBlock(x, y, z, Blocks.air, false, false, false, false, false, false, .0, false)
        return PathBlock(x, y, z, block.block, replaceableBlock(block.block, pos), block.block is BlockFalling,
            !needAvoidBlock(block.block) && (isSafeToWalkOn(block.block, pos, block) || block.block is BlockLadder),
            block.block.getCollisionBoundingBox(mc.theWorld, pos, block) != null, block.block is BlockLiquid,
            block.block is BlockLadder, y +
                    getBlockHeight(block.block, pos, block), block.block is BlockDoor)
    }

    /**
     * Takes into account if the block is within a break exclusion area.
     */
    open fun safeToBreak(block: PathBlock): Boolean {
        if (!this.canDig) return false

        if (this.dontCreateFlow) {
            if (this.getBlockAt(block.x, block.y + 1, block.z).liquid) return false
            if (this.getBlockAt(block.x - 1, block.y, block.z).liquid) return false
            if (this.getBlockAt(block.x + 1, block.y, block.z).liquid) return false
            if (this.getBlockAt(block.x, block.y, block.z - 1).liquid) return false
            if (this.getBlockAt(block.x, block.y, block.z + 1).liquid) return false
        }

        if (this.dontMineUnderFallBlock) {
            if (this.getBlockAt(block.x, block.y + 1, block.z).canFall) return false
        }

        return this.canBreakBlock(block) && this.exclusionBreak(block) < 100
    }

    /**
     * Takes into account if the block is within the stepExclusionAreas. And returns 100 if a block to be broken is within break exclusion areas.
     * @return cost
     */
    open fun safeOrBreak(block: PathBlock, toBreak: MutableList<PathBreakInfo>): Float {
        if (block.safe) return 0f
        if (!this.safeToBreak(block)) return 100f
        toBreak.add(PathBreakInfo(block))
        val digTime = block.block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, block)
        return 1 + digTime * 0.3f * this.digCost + exclusionBreak(block)
    }

    open fun getMoveJumpUp(node: PathMove, dir: Pair<Int, Int>, neighbors: MutableList<PathMove>) {
        val blockA = getBlockAt(node.x, node.y + 2, node.z)
        val blockH = getBlockAt(node.x + dir.first, node.y + 2, node.z + dir.second)
        val blockB = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)

        var cost = 2f
        val toBreak = mutableListOf<PathBreakInfo>()
        val toPlace = mutableListOf<PathPlaceInfo>()

        if (!blockC.physical) {
            if (node.remainingBlocks == 0) return

            val blockD = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
            if (!blockD.physical) {
                if (node.remainingBlocks == 1) return

                if (!blockD.replaceable) {
                    if (!safeToBreak(blockD)) return
                    cost += exclusionBreak(blockD)
                    toBreak.add(PathBreakInfo(blockD))
                }
                cost += exclusionPlace(blockD)
                toPlace.add(PathPlaceInfo(node.x, node.y - 1, node.z, dir.first, 0, dir.second, Vec3i(node.x, node.y, node.z)))
                cost += this.placeCost
            }

            if (!blockC.replaceable) {
                if (!safeToBreak(blockC)) return
                cost += exclusionBreak(blockC)
                toBreak.add(PathBreakInfo(blockC))
            }
            cost += exclusionBreak(blockC)
            toPlace.add(PathPlaceInfo(node.x + dir.first, node.y - 1, node.z + dir.second, 0, 1, 0))
            cost += this.placeCost

            blockC.height += 1
        }

        val block0 = getBlockAt(node.x, node.y - 1, node.z)
        if (blockC.height - block0.height > 1.2) return

        cost += safeOrBreak(blockA, toBreak)
        if (cost > 100) return
        cost += safeOrBreak(blockH, toBreak)
        if (cost > 100) return
        cost += safeOrBreak(blockB, toBreak)
        if (cost > 100) return

        neighbors.add(PathMove(blockB.x, blockB.y, blockB.z, node.remainingBlocks - toPlace.size, cost, toBreak, toPlace))
    }

    open fun getMoveForward(node: PathMove, dir: Pair<Int, Int>, neighbors: MutableList<PathMove>) {
        val blockB = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)
        val blockD = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)

        var cost = 1f
        cost += exclusionStep(blockC)

        val toBreak = mutableListOf<PathBreakInfo>()
        val toPlace = mutableListOf<PathPlaceInfo>()

        if(!blockD.physical && !blockC.liquid) {
            if (node.remainingBlocks == 0) return

            if (!blockD.replaceable) {
                if (!safeToBreak(blockD)) return
                cost += exclusionBreak(blockD)
                toBreak.add(PathBreakInfo(blockD))
            }
            cost += exclusionPlace(blockC)
            toPlace.add(PathPlaceInfo(node.x, node.y - 1, node.z, dir.first, 0, dir.second))
            cost += this.placeCost
        }

        cost += safeOrBreak(blockB, toBreak)
        if (cost > 100) return

        // Open fence gates
        if (this.canOpenDoors && blockC.openable && blockC.block.getCollisionBoundingBox(mc.theWorld, blockC, mc.theWorld.getBlockState(blockC)) != null) {
            toPlace.add(PathPlaceInfo(node.x + dir.first, node.y, node.z + dir.second, 0, 0, 0, useOne = true )) // Indicate that a block should be used on this block not placed
        } else {
            cost += safeOrBreak(blockC, toBreak)
            if (cost > 100) return
        }

        if (getBlockAt(node.x, node.y, node.z).liquid) cost += this.liquidCost
        neighbors.add(PathMove(blockC.x, blockC.y, blockC.z, node.remainingBlocks - toPlace.size, cost, toBreak, toPlace))
    }

    open fun getMoveDiagonal(node: PathMove, dir: Pair<Int, Int>, neighbors: MutableList<PathMove>) {
        var cost = sqrt(2f)
        val toBreak = mutableListOf<PathBreakInfo>()

        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)
        val y = if (blockC.physical) 1 else 0

        val block0 = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)

        var cost1 = 0f
        val toBreak1 = mutableListOf<PathBreakInfo>()
        val blockB1 = getBlockAt(node.x, node.y + y + 1, node.z + dir.second)
        val blockC1 = getBlockAt(node.x, node.y + y, node.z + dir.second)
        val blockD1 = getBlockAt(node.x, node.y + y - 1, node.z + dir.second)
        cost1 += safeOrBreak(blockB1, toBreak1)
        cost1 += safeOrBreak(blockC1, toBreak1)
        if (blockD1.height - block0.height > 1.2) cost1 += safeOrBreak(blockD1, toBreak1)

        var cost2 = 0f
        val toBreak2 = mutableListOf<PathBreakInfo>()
        val blockB2 = getBlockAt(node.x + dir.first, node.y + y + 1, node.z)
        val blockC2 = getBlockAt(node.x + dir.first, node.y + y, node.z)
        val blockD2 = getBlockAt(node.x + dir.first, node.y + y - 1, node.z)
        cost2 += safeOrBreak(blockB2, toBreak2)
        cost2 += safeOrBreak(blockC2, toBreak2)
        if (blockD2.height - block0.height > 1.2) cost2 += safeOrBreak(blockD2, toBreak2)

        if (cost1 > cost2) {
            cost += cost1
            toBreak.addAll(toBreak1)
        } else {
            cost += cost2
            toBreak.addAll(toBreak2)
        }
        if (cost > 100) return
        cost += safeOrBreak(getBlockAt(node.x + dir.first, node.y + y, node.z + dir.second), toBreak)
        if (cost > 100) return
        cost += safeOrBreak(getBlockAt(node.x + dir.first, node.y + y + 1, node.z + dir.second), toBreak)
        if (cost > 100) return

        if (getBlockAt(node.x, node.y, node.z).liquid) cost += this.liquidCost

        val blockD = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)
        if (y == 1) { // Case jump up by 1
            if (blockC.height - block0.height > 1.2) return
            cost += safeOrBreak(getBlockAt(node.x, node.y + 2, node.z), toBreak)
            if (cost > 100) return
            cost += 1
            neighbors.add(PathMove(blockC.x, blockC.y + 1, blockC.z, node.remainingBlocks, cost, toBreak = toBreak))
        } else if (blockD.physical || blockC.liquid) {
            neighbors.add(PathMove(blockC.x, blockC.y, blockC.z, node.remainingBlocks, cost, toBreak = toBreak))
        } else if (getBlockAt(node.x + dir.first, node.y - 2, node.z + dir.second).physical || blockD.liquid) {
            if (!blockD.safe) return // don't self-immolate
            neighbors.add(PathMove(blockC.x, blockC.y - 1, blockC.z, node.remainingBlocks, cost, toBreak = toBreak))
        }
    }

    open fun getLandingBlock(node: PathMove, dir: Pair<Int, Int>): PathBlock? {
        var blockLand = getBlockAt(node.x + dir.first, node.y - 2, node.z + dir.second)
        while (blockLand.y > 0) {
            if (blockLand.liquid && blockLand.safe) return blockLand
            if (blockLand.physical) {
                if (node.y - blockLand.y <= this.maxDropDown) return getBlockAt(blockLand.x, blockLand.y + 1, blockLand.z)
                return null
            }
            if (!blockLand.safe) return null
            blockLand = getBlockAt(blockLand.x, blockLand.y - 1, blockLand.z)
        }
        return null
    }

    open fun getMoveDropDown(node: PathMove, dir: Pair<Int, Int>, neighbors: MutableList<PathMove>) {
        val blockB = getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second)
        val blockC = getBlockAt(node.x + dir.first, node.y, node.z + dir.second)
        val blockD = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)

        var cost = 1f
        val toBreak = mutableListOf<PathBreakInfo>()

        val blockLand = getLandingBlock(node, dir) ?: return
        if (!this.infiniteLiquidDropdownDistance && (node.y - blockLand.y) > this.maxDropDown) return

        cost += this.safeOrBreak(blockB, toBreak)
        if (cost > 100) return
        cost += this.safeOrBreak(blockC, toBreak)
        if (cost > 100) return
        cost += this.safeOrBreak(blockD, toBreak)
        if (cost > 100) return
        if (blockC.liquid) return

        neighbors.add(PathMove(blockLand.x, blockLand.y, blockLand.z, node.remainingBlocks, cost, toBreak))
    }

    open fun getMoveDown(node: PathMove, neighbors: MutableList<PathMove>) {
        val block0 = getBlockAt(node.x, node.y - 1, node.z)

        var cost = 1f
        val toBreak = mutableListOf<PathBreakInfo>()
        val blockLand = getLandingBlock(node, 0 to 0) ?: return

        cost += this.safeOrBreak(block0, toBreak)
        if (cost > 100) return

        if (getBlockAt(node.x, node.y, node.z).liquid) return

        neighbors.add(PathMove(blockLand.x, blockLand.y, blockLand.z, node.remainingBlocks, cost, toBreak))
    }

    open fun getMoveUp(node: PathMove, neighbors: MutableList<PathMove>) {
        val block1 = getBlockAt(node.x, node.y, node.z)
        if (block1.liquid) return

        val block2 = getBlockAt(node.x, node.y + 2, node.z)
        var cost = 1f

        val toBreak = mutableListOf<PathBreakInfo>()
        val toPlace = mutableListOf<PathPlaceInfo>()

        cost += this.safeOrBreak(block2, toBreak)
        if (cost > 100) return

        if (!block1.climbable) {
            if (!this.allow1by1Towers || node.remainingBlocks == 0) return

            if (!block1.replaceable) {
                if (!this.safeToBreak(block1)) return
                toBreak.add(PathBreakInfo(block1))
            }

            val block0 = getBlockAt(node.x, node.y - 1, node.z)
            if (block0.physical && block0.height - node.y < -0.2) return

            cost += this.exclusionPlace(block1)
            toPlace.add(PathPlaceInfo(node.x, node.y - 1, node.z, 0, 1, 0, jump = true))
            cost += this.placeCost
        }

        if (cost > 100) return

        neighbors.add(PathMove(node.x, node.y + 1, node.z, node.remainingBlocks - toPlace.size, cost, toBreak, toPlace))
    }

    open fun getMoveParkourForward(node: PathMove, dir: Pair<Int, Int>, neighbors: MutableList<PathMove>) {
        val block0 = getBlockAt(node.x, node.y - 1, node.z)
        val block1 = getBlockAt(node.x + dir.first, node.y - 1, node.z + dir.second)
        if ((block1.physical && block1.height >= block0.height) ||
            !getBlockAt(node.x + dir.first, node.y, node.z + dir.second).safe ||
            !getBlockAt(node.x + dir.first, node.y + 1, node.z + dir.second).safe) return

        if (getBlockAt(node.x, node.y, node.z).liquid) return // can't jump from water

        // if we have a block on the ceiling, we cannot jump but we can still fall
        var ceilingClear = getBlockAt(node.x, node.y + 2, node.z).safe && getBlockAt(node.x + dir.first, node.y + 2, node.z + dir.second).safe

        // similarly for the down path
        var floorCleared = !getBlockAt(node.x + dir.first, node.y - 2, node.z + dir.second).physical

        val maxD = if(this.allowSprinting) 4 else 2

        for (d in 2..maxD) {
            val dx = dir.first * d
            val dz = dir.second * d
            val blockA = getBlockAt(node.x + dx, node.y + 2, node.z + dz)
            val blockB = getBlockAt(node.x + dx, node.y + 1, node.z + dz)
            val blockC = getBlockAt(node.x + dx, node.y + 0, node.z + dz)
            val blockD = getBlockAt(node.x + dx, node.y - 1, node.z + dz)

            if (ceilingClear && blockB.safe && blockC.safe && blockD.physical) {
                // forward
                neighbors.add(PathMove(blockC.x, blockC.y, blockC.z, node.remainingBlocks, this.exclusionStep(blockB) + 1, parkour = true))
                break
            } else if (ceilingClear && blockB.safe && blockC.physical) {
                // up
                if (blockA.safe) {
                    if (blockC.height - block0.height > 1.2) break // Too high to jump
                    neighbors.add(PathMove(blockB.x, blockB.y, blockB.z, node.remainingBlocks, this.exclusionStep(blockA) + 1, parkour = true))
                    break
                }
            } else if ((ceilingClear || d === 2) && blockB.safe && blockC.safe && blockD.safe && floorCleared) {
                val blockE = getBlockAt(node.x + dx, node.y - 1, node.z + dz)
                if (blockE.physical) {
                    neighbors.add(PathMove(blockE.x, blockE.y, blockE.z, node.remainingBlocks, this.exclusionStep(blockD) + 1, parkour = true))
                }
                floorCleared = !blockE.physical
            } else if (!blockB.safe || !blockC.safe) {
                break
            }

            ceilingClear = ceilingClear && blockA.safe
        }
    }

    open fun getNeighbors(node: PathMove): MutableList<PathMove> {
        val neighbors = mutableListOf<PathMove>()

        for (dir in cardinalDirections) {
            this.getMoveForward(node, dir, neighbors)
            this.getMoveJumpUp(node, dir, neighbors)
            this.getMoveDropDown(node, dir, neighbors)
            if (this.allowParkour) {
                this.getMoveParkourForward(node, dir, neighbors)
            }
        }

        for (dir in diagonalDirections) {
            this.getMoveDiagonal(node, dir, neighbors)
        }

        this.getMoveDown(node, neighbors)
        this.getMoveUp(node, neighbors)

        return neighbors
    }

    companion object {
        val cardinalDirections = arrayOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val diagonalDirections = arrayOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    }
}