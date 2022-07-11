package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.event.AttackEvent
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.event.WorldEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.ColorValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.other.jsonParser
import me.liuli.fluidity.util.render.drawAxisAlignedBB
import me.liuli.fluidity.util.world.renderPosX
import me.liuli.fluidity.util.world.renderPosY
import me.liuli.fluidity.util.world.renderPosZ
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemSkull
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import java.util.*

class SoulESP : Module("SoulESP", "See Hypixel SkyBlock Fairy Souls through wall", ModuleCategory.RENDER) {

    private val modeValue = ListValue("Mode", arrayOf("All", "FairySoul", "Gift"), "All")
    private val colorValue = ColorValue("Color", 0xFF55FF)
    private val clickedColorValue = ColorValue("ClickedColor", 0x00AAAA)
    private val boxAlphaValue = IntValue("BoxAlpha", 50, 0, 255)
    private val outlineAlphaValue = IntValue("OutlineAlpha", 255, 0, 255)
    private val outlineThicknessValue = FloatValue("OutlineThickness", 1f, 1f, 10f)

    private val parsed = mutableMapOf<Int, String>()
    private val clicked = mutableListOf<Int>()

    override fun onDisable() {
        parsed.clear()
        clicked.clear()
    }

    @Listen
    fun onWorld(event: WorldEvent) {
        parsed.clear()
        clicked.clear()
    }

    @Listen
    fun onAttack(event: AttackEvent) {
        if (!parsed.containsKey(event.targetEntity.entityId))
            return

        clicked.add(event.targetEntity.entityId)
    }

    @Listen
    fun onRender3D(event: Render3DEvent) {
        mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
            .forEach { entity ->
                // get helmet
                val helmet = entity.getCurrentArmor(3)

                if (helmet?.item !is ItemSkull) {
                    return@forEach // continue
                }

                val url = if (parsed.containsKey(entity.entityId)) {
                    parsed[entity.entityId]
                } else {
                    val data = helmet.tagCompound.getCompoundTag("SkullOwner").getCompoundTag("Properties")
                        .getTagList("textures", NBTTagCompound.NBT_TYPES.indexOf("COMPOUND"))
                        .getCompoundTagAt(0).getString("Value")
                    val jsonObject = jsonParser.parse(String(Base64.getDecoder().decode(data))).asJsonObject
                    jsonObject
                        .getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .get("url").asString.also { parsed[entity.entityId] = it }
                }

                if(when(modeValue.get()) {
                    "All" -> false
                    "FairySoul" -> url != SKULL_FAIRY_SOUL
                    "Gift" -> url != SKULL_GIFT
                    else -> true
                }) {
                    return@forEach
                }

                val entityBox = entity.entityBoundingBox
                val x = entity.renderPosX
                val y = entity.renderPosY
                val z = entity.renderPosZ
                val axisAlignedBB = AxisAlignedBB(
                    entityBox.minX - entity.posX + x - 0.2,
                    entityBox.maxY - entity.posY + y - 0.7,
                    entityBox.minZ - entity.posZ + z - 0.2,
                    entityBox.maxX - entity.posX + x + 0.2,
                    entityBox.maxY - entity.posY + y,
                    entityBox.maxZ - entity.posZ + z + 0.2
                )
                drawAxisAlignedBB(axisAlignedBB, if (clicked.contains(entity.entityId)) clickedColorValue.get() else colorValue.get(), outlineThicknessValue.get(), outlineAlphaValue.get(), boxAlphaValue.get())
            }
    }

    companion object {
        private const val SKULL_FAIRY_SOUL = "http://textures.minecraft.net/texture/b96923ad247310007f6ae5d326d847ad53864cf16c3565a181dc8e6b20be2387"
        private const val SKULL_GIFT = "http://textures.minecraft.net/texture/10f5398510b1a05afc5b201ead8bfc583e57d7202f5193b0b761fcbd0ae2"
    }
}