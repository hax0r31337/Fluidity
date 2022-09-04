package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.Vec2i
import me.liuli.fluidity.util.move.Vec3d
import me.liuli.fluidity.util.move.distanceXZ
import me.liuli.fluidity.util.move.floorPosition
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import me.liuli.fluidity.util.render.glColor
import me.liuli.fluidity.util.render.stripColor
import me.liuli.fluidity.util.skyblock.IceFillSolver
import me.liuli.fluidity.util.skyblock.IcePathSolver
import me.liuli.fluidity.util.skyblock.TicTacToeSolver
import me.liuli.fluidity.util.world.getBlock
import me.liuli.fluidity.util.world.renderPosX
import me.liuli.fluidity.util.world.renderPosY
import me.liuli.fluidity.util.world.renderPosZ
import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional
import net.minecraft.block.BlockStone
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.monster.EntitySilverfish
import net.minecraft.init.Blocks
import net.minecraft.item.ItemMap
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.concurrent.thread
import kotlin.experimental.and
import kotlin.math.abs

object DungeonAssist : Module("DungeonAssist", "An smart assistant helps you play Hypixel SkyBlock Dungeon", ModuleCategory.MISC) {

    private val espValue = BoolValue("ESP", true)
    private val espColorValue = ColorValue("ESPColor", Color(0xff, 0xff, 0xff, 0x55).rgb)
    private val creeperBeamValue = BoolValue("CreeperBeam", true)
    private val creeperBeamColorValue = ColorValue("CreeperBeamColor", Color(0x32, 0xcd, 0x32, 0x55).rgb)
    private val higherOrLowerValue = BoolValue("HigherOrLower", true)
    private val higherOrLowerColorValue = ColorValue("HigherOrLowerColor", Color(0xeb, 0xb0, 0x35, 0x55).rgb)

    private val nametags = mutableMapOf<Int, String>()
    private val threeWeirdosConditions = arrayOf("My chest doesn't have the reward. We are all telling the truth.",
        "At least one of them is lying, and the reward is not in ",
        "The reward is not in my chest!",
        "My chest has the reward and I'm telling the truth!",
        "The reward isn't in any of our chests.",
        "Both of them are telling the truth. Also, ")
    private val triviaQuizSolutions = mutableMapOf<String, Array<String>>().also {
        it["What SkyBlock year is it?"] = arrayOf("Year ${(System.currentTimeMillis() / 1000L - 1560276000).toInt() / 446400 + 1}")
        it["What is the status of The Watcher?"] = arrayOf("Stalker")
        it["What is the status of Bonzo?"] = arrayOf("New Necromancer")
        it["What is the status of Scarf?"] = arrayOf("Apprentice Necromancer")
        it["What is the status of The Professor?"] = arrayOf("Professor")
        it["What is the status of Thorn?"] = arrayOf("Shaman Necromancer")
        it["What is the status of Livid?"] = arrayOf("Master Necromancer")
        it["What is the status of Sadan?"] = arrayOf("Necromancer Lord")
        it["What is the status of Maxor?"] = arrayOf("Young Wither")
        it["What is the status of Goldor?"] = arrayOf("Wither Soldier")
        it["What is the status of Storm?"] = arrayOf("Elementalist")
        it["What is the status of Necron?"] = arrayOf("Wither Lord")
        it["What is the status of Maxor, Storm, Goldor and Necron?"] = arrayOf("Wither Lord")
        it["How many total Fairy Souls are there?"] = arrayOf("238 Fairy Souls")
        it["How many Fairy Souls are there in Spider's Den?"] = arrayOf("19 Fairy Souls")
        it["How many Fairy Souls are there in The End?"] = arrayOf("12 Fairy Souls")
        it["How many Fairy Souls are there in The Farming Islands?"] = arrayOf("20 Fairy Souls")
        it["How many Fairy Souls are there in Crimson Isle?"] = arrayOf("29 Fairy Souls")
        it["How many Fairy Souls are there in The Park?"] = arrayOf("11 Fairy Souls")
        it["How many Fairy Souls are there in Jerry's Workshop?"] = arrayOf("5 Fairy Souls")
        it["How many Fairy Souls are there in Hub?"] = arrayOf("79 Fairy Souls")
        it["How many Fairy Souls are there in The Hub?"] = arrayOf("79 Fairy Souls")
        it["How many Fairy Souls are there in Deep Caverns?"] = arrayOf("21 Fairy Souls")
        it["How many Fairy Souls are there in Gold Mine?"] = arrayOf("12 Fairy Souls")
        it["How many Fairy Souls are there in Dungeon Hub?"] = arrayOf("7 Fairy Souls")
        it["Which brother is on the Spider's Den?"] = arrayOf("Rick")
        it["What is the name of Rick's brother?"] = arrayOf("Pat")
        it["What is the name of the Painter in the Hub?"] = arrayOf("Marco")
        it["What is the name of the person that upgrades pets?"] = arrayOf("Kat")
        it["What is the name of the lady of the Nether?"] = arrayOf("Elle")
        it["Which villager in the Village gives you a Rogue Sword?"] = arrayOf("Jamie")
        it["How many unique minions are there?"] = arrayOf("55 Minions")
        it["Which of these enemies does not spawn in the Spider's Den?"] = arrayOf("Zombie Spider", "Cave Spider", "Wither Skeleton", "Dashing Spooder", "Broodfather", "Night Spider")
        it["Which of these monsters only spawns at night?"] = arrayOf("Zombie Villager", "Ghast")
        it["Which of these is not a dragon in The End?"] = arrayOf("Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon", "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon")
    }
    private var selectedEntity: AxisAlignedBB? = null
    private val lines = mutableListOf<Pair<Vec3, Vec3>>()
    private var triviaQuestion = ""
    var inDungeon = false
        private set

    override fun onDisable() {
        nametags.clear()
        lines.clear()
        selectedEntity = null
        inDungeon = false
        triviaQuestion = ""
    }

    fun getName(entityId: Int): String {
        return nametags[entityId] ?: ""
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        val lastItem = mc.thePlayer.inventoryContainer.getSlot(44).stack
        if (lastItem != null && stripColor(lastItem.displayName).contains("Map", true)) {
            inDungeon = true
        } else {
            onDisable()
            return
        }

        if (mc.thePlayer.ticksExisted % 20 != 0) return

        val armorStands = mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.hasCustomName() }
        nametags.keys.map { it }.forEach {
            if (mc.theWorld.getEntityByID(it) == null) {
                nametags.remove(it)
            }
        }
        var hasBlaze = false
        var hasCreeper: EntityCreeper? = null
        var hasSilverfish: EntitySilverfish? = null
        mc.theWorld.loadedEntityList.filter { it is EntityLivingBase && it !is EntityArmorStand && it != mc.thePlayer }.forEach { entity ->
            if (higherOrLowerValue.get() && entity is EntityBlaze) {
                hasBlaze = true
            } else if (creeperBeamValue.get() && entity is EntityCreeper) {
                hasCreeper = entity
            } else if (entity is EntitySilverfish && entity.getDistanceSqToEntity(mc.thePlayer) < 900) {
                hasSilverfish = entity
            }
            val nearest = armorStands.minByOrNull { distanceXZ(entity.posX - it.posX, entity.posZ - it.posZ) } ?: return@forEach
            if (distanceXZ(entity.posX - nearest.posX, entity.posZ - nearest.posZ) > 3) return@forEach
            nametags[entity.entityId] = nearest.name
        }

        if (hasBlaze) {
            solveBlaze(armorStands)
        } else if (!solveCreeper(hasCreeper) && !solveTicTacToe() && !solveIcePath(hasSilverfish) && !solveIceFill()) {
            lines.clear()
            selectedEntity = null
        }
    }

    private fun solveIceFill(): Boolean {
        val floorPos = mc.thePlayer.floorPosition
        var pos = floorPos.down()
        if (pos.getBlock()?.let { it == Blocks.ice || it == Blocks.packed_ice } != true) {
            return false
        }
        var pos1 = pos
        var pos2 = pos
        var pos3 = pos
        while(pos.getBlock()?.let { it == Blocks.ice || it == Blocks.packed_ice } == true) {
            pos = pos.add(1, 0, 0)
        }
        while(pos1.getBlock()?.let { it == Blocks.ice || it == Blocks.packed_ice } == true) {
            pos1 = pos1.add(-1, 0, 0)
        }
        while(pos2.getBlock()?.let { it == Blocks.ice || it == Blocks.packed_ice } == true) {
            pos2 = pos2.add(0, 0, 1)
        }
        while(pos3.getBlock()?.let { it == Blocks.ice || it == Blocks.packed_ice } == true) {
            pos3 = pos3.add(0, 0, -1)
        }
        val columns = Array(pos.x - pos1.x + 1) { BooleanArray(pos2.z - pos3.z + 1) }
        for ((var1, x) in (pos1.x..pos.x).withIndex()) {
            for ((idx, z) in (pos3.z..pos2.z).withIndex()) {
                columns[var1][idx] = BlockPos(x, pos.y + 1, z).getBlock() != Blocks.air || BlockPos(x, pos.y, z).getBlock() != Blocks.ice
            }
        }
        val route = mutableListOf<Vec2i>()
        val startPos = Vec2i(floorPos.x - pos1.x, floorPos.z - pos3.z)
        route.add(Vec2i(floorPos.x - pos1.x, floorPos.z - pos3.z))

        val chest = mc.theWorld.loadedTileEntityList.firstOrNull {
            it is TileEntityChest && it.pos.down().getBlock() == Blocks.stone && mc.theWorld.getBlockState(it.pos.down())?.getValue(BlockStone.VARIANT) == BlockStone.EnumType.ANDESITE_SMOOTH
        } ?: return false
        val face = mc.theWorld.getBlockState(chest.pos)?.let { it.getValue(BlockDirectional.FACING) } ?: return false
        var cpos = chest.pos
        var i = 0
        var endPos: Vec2i? = null
        while(i < 50) {
            cpos = cpos.add(face.directionVec)
            val v = Vec2i(cpos.x - pos1.x, cpos.z - pos3.z)
            if (v.row >= 0 && v.col >= 0 && v.row < columns.size && v.col < columns[0].size && !columns[v.row][v.col]) {
                endPos = v
                break
            }
            i++
        }
        endPos ?: return false
        columns[startPos.row][startPos.col] = false
        thread {
            val result = IceFillSolver.findSolution(columns, startPos, endPos, route) ?: return@thread
            lines.clear()
            result.forEachIndexed { i, v ->
                val nextPos = if (i + 1 == result.size) {
                    return@forEachIndexed
                } else {
                    val n = result[i+1]
                    Vec3(pos1.x + n.row + 0.5, floorPos.y + 0.1, pos3.z + n.col + 0.5)
                }
                lines.add(Pair(nextPos, Vec3(pos1.x + v.row + 0.5, floorPos.y + 0.1, pos3.z + v.col + 0.5)))
            }
        }

        return true
    }

    private fun solveWaterBoard(): Boolean {
        // TODO
//        val chest = mc.theWorld.loadedTileEntityList.firstOrNull {
//            it is TileEntityChest && it.pos.down().getBlock() == Blocks.stone && mc.theWorld.getBlockState(it.pos.down())?.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE &&
//                    (it.pos.add(1, 0, 0).getBlock() == Blocks.carpet || it.pos.add(0, 0, 1).getBlock() == Blocks.carpet)
//        } ?: return false
//        val face = mc.theWorld.getBlockState(chest.pos)?.let { it.getValue(BlockDirectional.FACING) } ?: return false
//        val wools = mutableListOf<EnumDyeColor>()
//        var pos1 = chest.pos
//        while(pos1.getBlock() != Blocks.stone_brick_stairs) {
//            pos1 = pos1.add(face.directionVec)
//            val block = mc.theWorld.getBlockState(pos1)
//            if (block?.block is BlockColored) {
//                wools.add(block.getValue(BlockColored.COLOR) ?: continue)
//            }
//        }
//        pos1 = chest.pos.add(0, 7, 0)
//        while(pos1.getBlock() != Blocks.glass) {
//            pos1 = pos1.subtract(face.directionVec)
//        }
//        // the chest must in center in the board
//        val xChange = face.directionVec.z != 0
//        var pos2 = pos1
//        while(pos2.getBlock() == Blocks.glass) {
//            pos2 = if (xChange) pos2.add(1, 0, 0) else pos2.add(0, 0, 1)
//        }
//        println(pos1)
//        println(pos2)

        return false
    }

    private fun solveIcePath(silverfish: EntitySilverfish?): Boolean {
        if (silverfish != null && silverfish.floorPosition.down().getBlock() == Blocks.packed_ice) {
            val pos = silverfish.floorPosition
            val chest = mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.pos.y == pos.y }
                .minByOrNull { it.pos.distanceSq(pos) } ?: return false
            val chestFacing =
                mc.theWorld.getBlockState(chest.pos)?.let { it.getValue(BlockDirectional.FACING) } ?: return false
            var pos1 = chest.pos
            while (pos1.getBlock() != Blocks.cobblestone) {
                pos1 = pos1.add(chestFacing.directionVec)
            }
            pos1 = pos1.add(-chestFacing.directionVec.x, 0, -chestFacing.directionVec.z)
            val xChange = chestFacing.directionVec.z != 0
            while (mc.theWorld.getBlockState(pos1)
                    .getValue(BlockStone.VARIANT) == BlockStone.EnumType.ANDESITE_SMOOTH) {
                pos1 = if (xChange) {
                    pos1.add(-1, 0, 0)
                } else {
                    pos1.add(0, 0, -1)
                }
            }
            pos1 = if (xChange) {
                pos1.add(1, 0, 0)
            } else {
                pos1.add(0, 0, 1)
            }
            val endX = if (!xChange) {
                if (abs(chestFacing.directionVec.x) == chestFacing.directionVec.x) pos1.x - 20 else pos1.x + 20
            } else {
                pos1.x + 20
            }
            val endZ = if (xChange) {
                if (abs(chestFacing.directionVec.z) == chestFacing.directionVec.z) pos1.z - 20 else pos1.z + 20
            } else {
                pos1.z + 20
            }
            val endPoints = mutableListOf<Vec2i>().also {
                val pos2 = chest.pos.add(chestFacing.directionVec.x * 2, 0, chestFacing.directionVec.z * 2)
                it.add(Vec2i(pos2.x, pos2.z))
                it.add(Vec2i(pos2.x + chestFacing.directionVec.z, pos2.z + chestFacing.directionVec.x))
                it.add(Vec2i(pos2.x - chestFacing.directionVec.z, pos2.z - chestFacing.directionVec.x))
            }
            val startX = if (endX < pos1.x) pos1.x - 1 else pos1.x + 1
            val startZ = if (endZ < pos1.z) pos1.z - 1 else pos1.z + 1
            val board = Array(19) { BooleanArray(19) }
            var boardX = 0
            var boardZ: Int
            var var1 = startX
            val fishPos = Vec2i(0, 0)
            while (var1 != endX) {
                var var2 = startZ
                boardZ = 0
                while (var2 != endZ) {
                    if (var1 == pos.x && var2 == pos.z) {
                        fishPos.row = boardX
                        fishPos.col = boardZ
                    } else if (BlockPos(var1, pos1.y, var2).getBlock() != Blocks.air) {
                        board[boardX][boardZ] = true
                    } else {
                        endPoints.forEach {
                            if (it.row == var1 && it.col == var2) {
                                it.row = boardX
                                it.col = boardZ
                            }
                        }
                    }
                    boardZ++
                    if (var2 < endZ) var2++ else var2--
                }
                boardX++
                if (var1 < endX) var1++ else var1--
            }

            val route = IcePathSolver.solve(board, fishPos, endPoints)
            val posY = chest.pos.y.toDouble() + 0.1
            lines.clear()
            route.forEachIndexed { index, point ->
                val nextPos = if (index + 1 == route.size) {
                    return@forEachIndexed
                } else {
                    val n = route[index + 1]
                    Vec3((if (endX > startX) startX + n.row else endX + (18 - n.row) + 1) + 0.5, posY, (if (endZ > startZ) startZ + n.col else endZ + (18 - n.col) + 1) + 0.5)
                }
                lines.add(Pair(Vec3((if (endX > startX) startX + point.row else endX + (18 - point.row) + 1) + 0.5, posY, (if (endZ > startZ) startZ + point.col else endZ + (18 - point.col) + 1) + 0.5), nextPos))
            }
            return true
        }
        return false
    }

    private fun solveBlaze(armorStands: List<Entity>) {
        val regex = Regex("\\[Lv\\w{2}] Blaze \\w{2,4}/\\w{2,4}❤")
        val blazes = armorStands.filter { regex.matches(stripColor(it.name)) }
        val targetId = Block.getIdFromBlock(Blocks.iron_bars)
        val theChest = mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest }.firstOrNull {
            val upBlock = it.pos.up().getBlock()?.let { Block.getIdFromBlock(it) } ?: -1
            upBlock == targetId
        }
        if (theChest == null) {
            selectedEntity = null
        } else {
            var iter = theChest.pos.up()
            while (Block.getIdFromBlock(iter.add(1, 0, 0).getBlock()) == 0) {
                iter = iter.up()
            }
            iter = iter.add(1, 0, 0)
            val blaze = if(Block.getIdFromBlock(iter.getBlock()) == Block.getIdFromBlock(Blocks.cobblestone)) {
                blazes.minByOrNull {
                    val str = stripColor(it.name)
                    str.substring(str.indexOf("/")+1, str.indexOf("❤")).toInt()
                }
            } else {
                blazes.maxByOrNull {
                    val str = stripColor(it.name)
                    str.substring(str.indexOf("/")+1, str.indexOf("❤")).toInt()
                }
            }

            selectedEntity = if (blaze != null) {
                Vec3d(blaze.posX, blaze.posY - 1.7, blaze.posZ).let {
                    AxisAlignedBB(it.x - 0.5, it.y, it.z - 0.5,
                        it.x + 0.5, it.y + 1.8, it.z + 0.5)
                }
            } else {
                null
            }
        }
    }

    private fun solveCreeper(creeper: EntityCreeper?): Boolean {
        if (creeper != null && mc.thePlayer.getDistanceSqToEntity(creeper) < 225) {
            val floorPos = creeper.floorPosition
            var offsetY = floorPos.y
            val targetId = Block.getIdFromBlock(Blocks.sea_lantern)

            while (offsetY > 0) {
                val block = BlockPos(floorPos.x, offsetY - 1, floorPos.z).getBlock() ?: break
                if (Block.getIdFromBlock(block) == 0) {
                    offsetY--
                } else {
                    break
                }
            }

            val dots = mutableListOf<Vec3>()
            for (x in 15 downTo -15) {
                for (y in 10 downTo -7) {
                    for (z in 15 downTo -15) {
                        val blockPos = BlockPos(floorPos.x + x, offsetY + y, floorPos.z + z)
                        val block = blockPos.getBlock() ?: continue
                        if (Block.getIdFromBlock(block) == targetId) dots.add(Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5))
                    }
                }
            }

            val hitBB = AxisAlignedBB(floorPos.x - 0.5, offsetY + 0.0,  floorPos.z - 0.5, floorPos.x + 1.5, offsetY + 2.0, floorPos.z + 1.5)
            lines.clear()
            for (i in 0 until dots.size) {
                if (i+1 == dots.size) break
                for (j in i+1 until dots.size) {
                    val hitInfo = hitBB.calculateIntercept(dots[i], dots[j])
                    if (hitInfo != null) {
                        lines.add(Pair(dots[i], dots[j]))
                    }
                }
            }
        } else {
            return false
        }
        return true
    }

    private fun solveTicTacToe(): Boolean {
        val itemFrames = mc.theWorld.loadedEntityList.filter { it is EntityItemFrame && it.getDistanceSqToEntity(mc.thePlayer) < 100 && it.displayedItem?.item is ItemMap }
        if (itemFrames.size != 9 && itemFrames.size % 2 == 1) {
            val game = Array(3) { charArrayOf('_', '_', '_') }
            var offset: BlockPos? = null
            var horizonOffset: BlockPos? = null
            itemFrames.forEach {
                val facing = (it as EntityItemFrame).facingDirection
                val backBlock = it.floorPosition.subtract(facing.directionVec)
                if (backBlock.getBlock() != Blocks.iron_block) return@forEach
                val upBlock = backBlock.up().getBlock()
                val downBlock = backBlock.down().getBlock()
                val col = if(upBlock == Blocks.iron_block && downBlock == Blocks.iron_block) {
                    1
                } else if (upBlock == Blocks.iron_block) {
                    2
                } else if (downBlock == Blocks.iron_block) {
                    0
                } else return@forEach
                val leftBlock = backBlock.add(facing.directionVec.z, 0, facing.directionVec.x).getBlock()
                val rightBlock = backBlock.add(-facing.directionVec.z, 0, -facing.directionVec.x).getBlock()
                val row = if(leftBlock == Blocks.iron_block && rightBlock == Blocks.iron_block) {
                    1
                } else if (leftBlock == Blocks.iron_block) {
                    2
                } else if (rightBlock == Blocks.iron_block) {
                    0
                } else return@forEach
                if (offset == null) {
                    horizonOffset = BlockPos(facing.directionVec.z, 0, facing.directionVec.x)
                    offset = backBlock.add(facing.directionVec.z * row, col, facing.directionVec.x * row)
                }

                val mapData = (it.displayedItem.item as ItemMap).getMapData(it.displayedItem, mc.theWorld)
                val status = (mapData.colors[8256] and (0xff).toByte()).let { if (it.toInt() == 0x72) 'x' else 'o' }
                game[col][row] = status
            }
            if (offset == null) {
                return false
            }
            val bestMove = TicTacToeSolver.findBestMove(game)
            selectedEntity = offset!!.let {
                val offset = it.add(horizonOffset!!.x * -bestMove.col, -bestMove.row, horizonOffset!!.z * -bestMove.col)
                AxisAlignedBB(offset.x + 0.0, offset.y + 0.0, offset.z + 0.0, offset.x + 1.0, offset.y + 1.0, offset.z + 1.0)
            }
        } else {
            return false
        }
        return true
    }

    @Listen
    fun onRender3d(event: Render3DEvent) {
        if (espValue.get()) {
            nametags.forEach { (i, s) ->
                if (!s.contains("✯")) {
                    return@forEach
                }
                val entity = mc.theWorld.getEntityByID(i) ?: return@forEach
                if (entity.isInvisible) return@forEach

                val entityBox = entity.entityBoundingBox
                val x = entity.renderPosX
                val y = entity.renderPosY
                val z = entity.renderPosZ
                val axisAlignedBB = AxisAlignedBB(
                    entityBox.minX - entity.posX + x,
                    entityBox.minY - entity.posY + y,
                    entityBox.minZ - entity.posZ + z,
                    entityBox.maxX - entity.posX + x,
                    entityBox.maxY - entity.posY + y,
                    entityBox.maxZ - entity.posZ + z
                )
                drawAxisAlignedBB(axisAlignedBB, espColorValue.get(), 0f, 0, espColorValue.get() shr 24 and 255)
            }
        }
        if (selectedEntity != null) {
            val axisAlignedBB = selectedEntity!!.offset(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)
            drawAxisAlignedBB(axisAlignedBB, higherOrLowerColorValue.get(), 0f, 0, higherOrLowerColorValue.get() shr 24 and 255)
        }
        if (lines.isNotEmpty()) {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            glColor(creeperBeamColorValue.get())
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glLineWidth(4f)
            GL11.glBegin(GL11.GL_LINES)

            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            lines.forEach {
                GL11.glVertex3d(it.first.xCoord - renderPosX, it.first.yCoord - renderPosY, it.first.zCoord - renderPosZ)
                GL11.glVertex3d(it.second.xCoord - renderPosX, it.second.yCoord - renderPosY, it.second.zCoord - renderPosZ)
            }

            GL11.glEnd()
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S02PacketChat) {
            val msg = stripColor(packet.chatComponent.unformattedText)
            if (msg.startsWith("[NPC]") && threeWeirdosConditions.any { msg.contains(it) }) {
                val name = msg.substring(6, msg.indexOf(":"))
                displayAlert("The reward is in §c$name§f's chest!")
            } else if (triviaQuestion.isNotEmpty() && (msg.contains("ⓐ") || msg.contains("ⓑ") || msg.contains("ⓒ"))) {
                val answers = triviaQuizSolutions[triviaQuestion] ?: throw NullPointerException("Solution not found")
                if (!answers.any { msg.contains(it) }) {
                    event.cancel()
                }
            } else {
                triviaQuizSolutions.keys.forEach {
                    if (msg.contains(it)) {
                        triviaQuestion = it
                        return
                    }
                }
            }
        }
    }
}