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
import me.liuli.fluidity.util.move.Vec3d
import me.liuli.fluidity.util.move.distanceXZ
import me.liuli.fluidity.util.move.floorPosition
import me.liuli.fluidity.util.move.lookAt
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import me.liuli.fluidity.util.render.glColor
import me.liuli.fluidity.util.render.stripColor
import me.liuli.fluidity.util.world.*
import net.minecraft.block.Block
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

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
    private var selectedEntity: Vec3d? = null
    private val lines = mutableListOf<Pair<Vec3, Vec3>>()

    override fun onDisable() {
        nametags.clear()
        lines.clear()
        selectedEntity = null
    }

    fun getName(entityId: Int): String {
        return nametags[entityId] ?: ""
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        val lastItem = mc.thePlayer.inventoryContainer.getSlot(44).stack
        if (lastItem != null && stripColor(lastItem.displayName).contains("Map", true)) {
//            displayAlert("IN DUNGEON")
        } else {
            nametags.clear()
            return
        }

        if (mc.thePlayer.ticksExisted % 10 != 0) return

        val armorStands = mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.hasCustomName() }
        nametags.keys.map { it }.forEach {
            if (mc.theWorld.getEntityByID(it) == null) {
                nametags.remove(it)
            }
        }
        var hasBlaze = false
        var hasCreeper: EntityCreeper? = null
        mc.theWorld.loadedEntityList.filter { it is EntityLivingBase && it !is EntityArmorStand && it != mc.thePlayer }.forEach { entity ->
            if (higherOrLowerValue.get() && entity is EntityBlaze) {
                hasBlaze = true
            } else if (creeperBeamValue.get() && entity is EntityCreeper) {
                hasCreeper = entity
            }
            val nearest = armorStands.minByOrNull { distanceXZ(entity.posX - it.posX, entity.posZ - it.posZ) } ?: return@forEach
            if (distanceXZ(entity.posX - nearest.posX, entity.posZ - nearest.posZ) > 3) return@forEach
            nametags[entity.entityId] = nearest.name
        }
        if (hasBlaze) {
            val regex = Regex("\\[Lv\\w{2}] Blaze \\w{2,4}/\\w{2,4}❤")
            val blazes = armorStands.filter { regex.matches(stripColor(it.name)) }
            val targetId = Block.getIdFromBlock(Blocks.iron_bars)
            val theChest = mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest }.firstOrNull {
                val upBlock = it.pos.up().getBlock()?.let { Block.getIdFromBlock(it) } ?: -1
                val downBlock = it.pos.down().getBlock()?.let { Block.getIdFromBlock(it) } ?: -1
                upBlock == targetId
            }
            if (theChest == null) {
                selectedEntity = null
            } else {
                val isLower = theChest.pos.up().getBlock()?.let { Block.getIdFromBlock(it) == targetId } ?: false
                val blaze = if(isLower) {
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

                if (blaze != null) {
                    selectedEntity = Vec3d(blaze.posX, blaze.posY - 1.7, blaze.posZ)
                } else {
                    selectedEntity = null
                }
            }
        } else if (hasCreeper != null && mc.thePlayer.getDistanceSqToEntity(hasCreeper) < 225) {
            val floorPos = hasCreeper!!.floorPosition
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
                        if (Block.getIdFromBlock(block) == targetId) dots.add(Vec3(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5))
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
            lines.clear()
            selectedEntity = null
        }
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
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            val axisAlignedBB = selectedEntity!!.let {
                AxisAlignedBB(it.x - 0.5 - renderPosX, it.y - renderPosY, it.z - 0.5 - renderPosZ,
                    it.x + 0.5 - renderPosX, it.y + 1.8 - renderPosY, it.z + 0.5 - renderPosZ)
            }
            drawAxisAlignedBB(axisAlignedBB, higherOrLowerColorValue.get(), 0f, 0, higherOrLowerColorValue.get() shr 24 and 255)
        }
        if (lines.isNotEmpty()) {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            glColor(creeperBeamColorValue.get())
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
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
            }
        }
    }
}