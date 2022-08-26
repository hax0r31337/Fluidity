/**
MIT License

Copyright (c) 2020 PrismarineJS

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package me.liuli.fluidity.pathfinder

import me.liuli.fluidity.event.*
import me.liuli.fluidity.pathfinder.algorithm.AStar
import me.liuli.fluidity.pathfinder.goals.IGoal
import me.liuli.fluidity.pathfinder.path.PathMove
import me.liuli.fluidity.pathfinder.path.PathPlaceInfo
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.*
import me.liuli.fluidity.util.render.glColor
import me.liuli.fluidity.util.render.rainbow
import net.minecraft.block.Block
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockLiquid
import net.minecraft.block.state.IBlockState
import net.minecraft.network.play.server.S21PacketChunkData
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3i
import org.lwjgl.opengl.GL11
import kotlin.math.abs
import kotlin.math.atan2


object Pathfinder : Listener {

    private val settings: PathfinderController = PathfinderController()
    var stateGoal: IGoal? = null
    private var aStarContext: AStar? = null
    private var aStarTimedOut = false
    private var dynamicGoal = false
    private var path = mutableListOf<PathMove>()
    private var pathUpdated = false
    private var placing = false
    private var placingBlock: PathPlaceInfo? = null
    private var lastNodeTime = System.currentTimeMillis()
    private var returningPos: Vec3i? = null
    private var stopPathing = false

    fun setGoal(goal: IGoal, dynamic: Boolean = false) {
        this.stateGoal = goal
        this.dynamicGoal = dynamic
        resetPath()
    }

    private fun clearControlState() {
        mc.gameSettings.keyBindForward.pressed = false
        mc.gameSettings.keyBindBack.pressed = false
        mc.gameSettings.keyBindLeft.pressed = false
        mc.gameSettings.keyBindRight.pressed = false
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
    }

    fun resetPath(clearState: Boolean = true) {
        placing = false
        pathUpdated = false
        aStarContext = null
        path.clear()
        if (stopPathing) stop()
        else if (clearState) clearControlState()
    }

    fun stop() {
        stopPathing = false
        aStarContext = null
        fullStop()
    }

    fun fullStop() {
        clearControlState()

        // Force horizontal velocity to 0 (otherwise inertia can move us too far)
        // Kind of cheaty, but the server will not tell the difference
        mc.thePlayer.motionX = .0
        mc.thePlayer.motionZ = .0
        val blockX = mc.thePlayer.position.x + 0.5
        val blockZ = mc.thePlayer.position.z + 0.5

        // Make sure our bounding box don't collide with neighboring blocks
        // otherwise recenter the position
        if (abs(mc.thePlayer.position.x - blockX) < 0.2) mc.thePlayer.posX = blockX
        if (abs(mc.thePlayer.position.z - blockZ) < 0.2) mc.thePlayer.posZ = blockZ
        mc.thePlayer.syncPosition()
    }

    private fun postProcessPath(path: MutableList<PathMove>) {
        path.forEachIndexed { i, node ->
            if (node.toBreak.isNotEmpty() || node.toPlace.isNotEmpty()) return@forEachIndexed // TODO: break in original code
            val b = mc.theWorld.getBlockState(BlockPos(node.x, node.y, node.z))
            if (b != null && (b.block is BlockLiquid ||
                        (b.block is BlockLadder && i + 1 < path.size && path[i + 1].y < node.y))) {
                node.postX = node.x + 0.5
                node.postZ = node.z + 0.5
                return@forEachIndexed
            }
            var npY = node.y
            var np = b?.let { getPositionOnTopOf(it.block, it) }
            if (np == null) np = mc.theWorld.getBlockState(BlockPos(node.x, node.y - 1, node.z))?.let { npY -= 1; getPositionOnTopOf(it.block, it) }
            if (np != null) {
                node.postX += np.x
                node.postY = npY + np.y
                node.postZ += np.z
            } else {
                node.postX = node.x + 0.5
                node.postY -= 1
                node.postZ = node.z + 0.5
            }
        }

        if (path.isEmpty()) {
            return
        }
        val newPath = mutableListOf<PathMove>()
        var lastNode = path[0]
        path.forEachIndexed { i, node ->
            if (i == 0) return@forEachIndexed
            if (abs(node.y - lastNode.y) > 0.5 || node.toBreak.isNotEmpty() || node.toPlace.isNotEmpty() || !PathfinderSimulator.canStraightLineBetween(lastNode, node)) {
                newPath.add(path[i - 1])
                lastNode = path[i - 1]
            }
        }
        newPath.add(path.last())

        path.clear()
        path.addAll(newPath)
    }

    /**
     * @return the average x/z position of the highest standing positions in the block.
     */
    private fun getPositionOnTopOf(block: Block, state: IBlockState): Vec3d? {
        val shape = block.getCollisionBoundingBox(mc.theWorld, BlockPos(0, 0, 0), state) ?: return null
        val p = Vec3d(0.5, .0, 0.5)
        var n = 1
        val h = shape.maxY
        if (h == p.y) {
            p.x += (shape.minX + shape.maxX) / 2
            p.z += (shape.minZ + shape.maxZ) / 2
            n++
        } else if (h > p.y) {
            n = 2
            p.x = 0.5 + (shape.minX + shape.maxX) / 2
            p.y = h
            p.z = 0.5 + (shape.minZ + shape.maxZ) / 2
        }

        p.x /= n
        p.z /= n
        return p
    }

    private fun moveToBlock(pos: Vec3i): Boolean {
        val targetPos = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        if (targetPos.distanceTo(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ) > 0.2) {
            lookAt(targetPos.x, targetPos.y, targetPos.z)
            mc.gameSettings.keyBindForward.pressed = true
            return false
        }
        mc.gameSettings.keyBindForward.pressed = false
        return true
    }

//    private fun moveToEdge(block: Vec3i, edgeX: Int, edgeZ: Int): Boolean {
//        // Target viewing direction while approaching edge
//        // The Bot approaches the edge while looking in the opposite direction from where it needs to go
//        // The target Pitch angle is roughly the angle the bot has to look down for when it is in the position
//        // to place the next block
//        val targetPosDelta = Vec3d(mc.thePlayer.posX - block.x + 0.5, mc.thePlayer.posY - block.y, mc.thePlayer.posZ - block.z - 0.5)
//        val targetYaw = MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(-targetPosDelta.x, targetPosDelta.z)).toFloat())
//        val viewVector = getViewVector(targetYaw, -1.421f)
//        // While the bot is not in the right position rotate the view and press back while crouching
//        if (Vec3d(block.x + 0.5 + edgeX, 1.0, block.z + 0.5 + edgeZ).distanceTo(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ) > 0.4) {
//            viewVector.apply {
//                lookAt(x + mc.thePlayer.posX, y + mc.thePlayer.posY, z + mc.thePlayer.posZ)
//            }
//            mc.gameSettings.keyBindSneak.pressed = true
//            mc.gameSettings.keyBindBack.pressed = true
//            return false
//        }
//        mc.gameSettings.keyBindBack.pressed = false
//        return true
//    }

    fun getPathTo(goal: IGoal): AStar.Result {
        val pos = mc.thePlayer.position
        val dy = mc.thePlayer.posY - pos.y
        val block = mc.theWorld.getBlockState(pos)

        val start = PathMove(pos.x, pos.y + if(block != null && dy > 0.001 && mc.thePlayer.onGround) 1 else 0, pos.z,
            settings.countBridgeableItems(), 0f)
        aStarContext = AStar(start, settings, goal)
        return aStarContext!!.compute()
    }

    private fun pathFromPlayer(path: MutableList<PathMove>) {
        if (path.isEmpty()) return
        var minI = 0
        var minDistance = 1000.0
        var isBreak = false
        path.forEachIndexed { i, node ->
            if (isBreak || node.toBreak.isNotEmpty() || node.toPlace.isNotEmpty()) {
                isBreak = true
                return@forEachIndexed
            }
            val dist = Vec3d(node.postX, node.postY, node.postZ).distanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            if (dist < minDistance) {
                minDistance = dist
                minI = i
            }
        }
        // check if we are between 2 nodes
        val n1 = path[minI]
        // check if node already reached
        val dx = n1.postX - mc.thePlayer.posX
        val dy = n1.postY - mc.thePlayer.posY
        val dz = n1.postZ - mc.thePlayer.posZ
        val reached = abs(dx) <= 0.35 && abs(dz) <= 0.35 && abs(dy) < 1
        if (minI + 1 < path.size && n1.toBreak.isEmpty() && n1.toPlace.isEmpty()) {
            val n2 = path[minI + 1]
            val pos2 = Vec3d(n2.postX, n2.postY, n2.postZ)
            val d2 = Vec3d(pos2.x, pos2.y, pos2.z).distanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            val d12 = pos2.distanceSq(n1.postX, n1.postY, n1.postZ)
            minI += if(d12 > d2 || reached) 1 else 0
        }
        path.map { it }.forEachIndexed { i, node ->
            if (i <= minI) {
                path.remove(node)
            }
        }
    }

    private fun isPositionNearPath(x: Int, y: Int, z: Int): Boolean {
        for(node in path) {
            val dx = node.postX - x - 0.5
            val dy = node.postY - y - 0.5
            val dz = node.postZ - z - 0.5
            if (dx <= 1 && dy <= 2 && dz <= 1) return true
        }
        return false
    }

    @Listen
    private fun onPacket(event: PacketEvent) {
        aStarContext ?: return
        val packet = event.packet

        if (packet is S21PacketChunkData) {
            // Reset only if the new chunk is adjacent to a visited chunk
            if (aStarContext!!.visitedChunk(packet.chunkX - 1, packet.chunkZ) ||
                aStarContext!!.visitedChunk(packet.chunkX, packet.chunkZ - 1) ||
                aStarContext!!.visitedChunk(packet.chunkX + 1, packet.chunkZ) ||
                aStarContext!!.visitedChunk(packet.chunkX, packet.chunkZ + 1) ||
                aStarContext!!.visitedChunk(packet.chunkX, packet.chunkZ)) {
                resetPath(false)
            }
        } else if (packet is S23PacketBlockChange) {
            if (isPositionNearPath(packet.blockPosition.x, packet.blockPosition.y, packet.blockPosition.z)) {
                resetPath(false)
            }
        } else if (packet is S22PacketMultiBlockChange) {
            packet.changedBlocks.forEach {
                if (isPositionNearPath(it.pos.x, it.pos.y, it.pos.z)) {
                    resetPath(false)
                    return
                }
            }
        }
    }

    @Listen
    private fun onRender3D(event: Render3DEvent) {
        if (path.isEmpty()) return

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        mc.entityRenderer.disableLightmap()

        GL11.glBegin(GL11.GL_LINE_STRIP)

        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ

        var idx = 0
        for (pos in path) {
            glColor(rainbow(idx))
            idx++
            GL11.glVertex3d(pos.x + 0.5 - renderPosX, pos.y - renderPosY, pos.z + 0.5 - renderPosZ)
        }

        GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
        GL11.glEnd()
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }

    @Listen
    private fun onUpdate(event: UpdateEvent) {
        if (stateGoal != null) {
            if (!stateGoal!!.isValid()) {
                stop()
            } else if (stateGoal!!.hasChanged()) {
                resetPath(false)
            }
        }
        if (aStarContext != null && aStarTimedOut) {
            displayAlert("ASTAR TIMED OUT")
            val result = aStarContext!!.compute()
            displayAlert("${result.path.size}")
            postProcessPath(result.path)
            pathFromPlayer(result.path)
            path = result.path
            aStarTimedOut = result.status == AStar.ResultStatus.PARTIAL
        }
        if (settings.losWhenPlacingBlocks && returningPos != null) {
            if (!moveToBlock(returningPos!!)) return
            returningPos = null
        }
        if (path.isEmpty()) {
            lastNodeTime = System.currentTimeMillis()
            if (stateGoal == null) {
                return
            }
            if (stateGoal!!.isEnd(mc.thePlayer.position)) {
                if (!dynamicGoal) {
                    // goal reached
                    stateGoal = null
                    fullStop()
                }
            } else if (!pathUpdated) {
                displayAlert("PROCESS PATH")
                val result = getPathTo(stateGoal!!)
                displayAlert("${result.path.size}")
                postProcessPath(result.path)
                path = result.path
                aStarTimedOut = result.status == AStar.ResultStatus.PARTIAL
                pathUpdated = true
            }
            if (path.isEmpty()) {
                return
            }
        }

        var nextPoint = path.first()
//        if (settings.bot.controller.isDigging || nextPoint.toBreak.isNotEmpty()) {
//            if (!settings.bot.controller.isDigging && settings.bot.player.onGround) {
//                val toBreak = nextPoint.toBreak.first()
//                val bestItemSlot = settings.bestHarvestItem(settings.bot.world.getBlockAt(toBreak.x, toBreak.y, toBreak.z) ?: Block.AIR)
//                if (bestItemSlot != settings.bot.player.heldItemSlot) {
//                    settings.bot.player.heldItemSlot = bestItemSlot
//                    return
//                }
//                settings.bot.controller.breakBlock(toBreak.x, toBreak.y, toBreak.z)
//                nextPoint.toBreak.remove(toBreak)
//            }
//            return
//        }

        // TODO: sneak when placing or make sure the block is not interactive
        // TODO fix this
//        if (placing || nextPoint.toPlace.isNotEmpty()) {
//            if (!placing) {
//                placing = true
//                placingBlock = nextPoint.toPlace.first().also { nextPoint.toPlace.remove(it) }
//                fullStop()
//            }
//
//            // Open gates or doors
//            if (placingBlock!!.useOne) {
//                settings.bot.controller.useOnBlock(placingBlock!!.x, placingBlock!!.y, placingBlock!!.z)
//                placing = false
//                placingBlock = null
//                return
//            }
//            val block = settings.searchBridgeableItem()
//            if (block == null) {
//                resetPath()
//                return
//            }
//            if (settings.bot.player.heldItemSlot != (block - settings.bot.player.inventory.heldItemSlot)) {
//                settings.bot.player.heldItemSlot = block - settings.bot.player.inventory.heldItemSlot
//            }
//            if (settings.losWhenPlacingBlocks && placingBlock!!.y == floor(settings.bot.player.position.y).toInt() - 1 && placingBlock!!.dy == 0) {
//                if (!moveToEdge(Vec3i(placingBlock!!.x, placingBlock!!.y, placingBlock!!.z), placingBlock!!.dx, placingBlock!!.dz)) return
//            }
//            var canPlace = true
//            if (placingBlock!!.jump) {
//                settings.bot.controller.jump = true
//                canPlace = placingBlock!!.y + 1 < settings.bot.player.position.y
//            }
//            if (canPlace) {
//                val face = if (placingBlock!!.dx != 0) {
//                    if (placingBlock!!.dx > 0) EnumBlockFacing.EAST else EnumBlockFacing.WEST
//                } else if (placingBlock!!.dy != 0) {
//                    if (placingBlock!!.dy > 0) EnumBlockFacing.UP else EnumBlockFacing.DOWN
//                } else {
//                    if (placingBlock!!.dz > 0) EnumBlockFacing.SOUTH else EnumBlockFacing.NORTH
//                }
//                settings.bot.controller.useOnBlock(placingBlock!!.x + placingBlock!!.dx - face.offset.x, placingBlock!!.y + placingBlock!!.dy - face.offset.y,
//                    placingBlock!!.z + placingBlock!!.dz - face.offset.z, face)
//                settings.bot.protocol.swingItem()
//                settings.bot.player.sneaking = false
//                if (settings.losWhenPlacingBlocks && placingBlock!!.returnPos != null) returningPos = placingBlock!!.returnPos!!.copy()
//                placing = false
//                placingBlock = null
//            }
//            return
//        }

        var dx = nextPoint.postX - mc.thePlayer.posX
        val dy = nextPoint.postY - mc.thePlayer.posY
        var dz = nextPoint.postZ - mc.thePlayer.posZ
        if (abs(dx) <= 0.35 && abs(dz) <= 0.35 && abs(dy) < 1) {
            // arrived at next point
            lastNodeTime = System.currentTimeMillis()
            if (stopPathing) {
                stop()
                return
            }
            path.remove(nextPoint)
            if (path.isEmpty()) { // done
                // If the block the bot is standing on is not a full block only checking for the floored position can fail as
                // the distance to the goal can get greater then 0 when the vector is floored.
                if (!dynamicGoal && stateGoal != null && (stateGoal!!.isEnd(mc.thePlayer.position) || stateGoal!!.isEnd(mc.thePlayer.position.down(1)))) {
                    stateGoal = null
                }
                fullStop()
                return
            }
            // not done yet
            nextPoint = path.first()
            if (nextPoint.toPlace.isNotEmpty() || nextPoint.toBreak.isNotEmpty()) {
                fullStop()
                return
            }
            dx = nextPoint.postX - mc.thePlayer.posX
            dz = nextPoint.postZ - mc.thePlayer.posZ
        }

        mc.thePlayer.rotationYaw = MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(-dx, dz)).toFloat())
        mc.gameSettings.keyBindForward.pressed = true
        mc.gameSettings.keyBindJump.pressed = false

        if (mc.thePlayer.isInWater) {
            displayAlert("W")
            mc.gameSettings.keyBindJump.pressed = true
            mc.gameSettings.keyBindSprint.pressed = false
        } else if (settings.allowSprinting && PathfinderSimulator.canStraightLine(path, true)) {
            displayAlert("SL")
            mc.gameSettings.keyBindJump.pressed = false
            mc.gameSettings.keyBindSprint.pressed = true
        } else if (settings.allowSprinting && PathfinderSimulator.canSprintJump(path)) {
            displayAlert("SJ")
            mc.gameSettings.keyBindJump.pressed = true
            mc.gameSettings.keyBindSprint.pressed = true
        } else if (PathfinderSimulator.canStraightLine(path)) {
            displayAlert("SL")
            mc.gameSettings.keyBindJump.pressed = false
            mc.gameSettings.keyBindSprint.pressed = false
        } else if (PathfinderSimulator.canWalkJump(path)) {
            displayAlert("WJ")
            mc.gameSettings.keyBindJump.pressed = true
            mc.gameSettings.keyBindSprint.pressed = false
        } else {
            displayAlert("L")
            mc.gameSettings.keyBindForward.pressed = false
            mc.gameSettings.keyBindSprint.pressed = false
        }

        // check for futility
        if (System.currentTimeMillis() - lastNodeTime > 5000) {
            // should never take this long to go to the next node
            resetPath()
        }
    }

    override fun listen() = true
}