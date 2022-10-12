package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.*
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class NoSlow : Module("NoSlow", "Make you not slow down during item use", ModuleCategory.MOVEMENT) {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "Hypixel", "Matrix"), "Vanilla")
    private val blockMultiplier = FloatValue("BlockMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeMultiplier = FloatValue("ConsumeMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowMultiplier = FloatValue("BowMultiplier", 1.0F, 0.2F, 1.0F)

    private var needNoSlow = false
    private val packetBuf = mutableListOf<Packet<*>>()
    private var releasingPacket = false

    override fun onDisable() {
        needNoSlow = false
        releasingPacket = false
    }

    @Listen
    fun onPreMotion(event: PreMotionEvent) {
        processMotion(true)
    }

    @Listen
    fun onPostMotion(event: PostMotionEvent) {
        processMotion(false)
    }

    private fun processMotion(pre: Boolean) {
        if (!mc.thePlayer.isUsingItem || mc.thePlayer.heldItem?.item !is ItemSword) {
            needNoSlow = false
            return
        }
        needNoSlow = true
        when(modeValue.get()) {
            "Hypixel" -> {
                if (mc.thePlayer.ticksExisted % 10 == 0) {
                    if (pre) {
                        mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                    } else {
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
                    }
                }
            }
            "Matrix" -> {
                if (packetBuf.size >= 3 && pre) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                    releaseBuf()
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
                }
            }
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (modeValue.get() == "Matrix" && !releasingPacket) {
            if (needNoSlow) {
                if (packet is C03PacketPlayer || packet is C02PacketUseEntity || packet is C0APacketAnimation) {
                    packetBuf.add(packet)
                    event.cancel()
                }
            } else if (packetBuf.isNotEmpty()) {
                releaseBuf()
            }
        }
    }

    private fun releaseBuf() {
        releasingPacket = true
        packetBuf.forEach {
            mc.netHandler.addToSendQueue(it)
        }
        packetBuf.clear()
        releasingPacket = false
    }

    @Listen
    fun onSlowDown(event: SlowDownEvent) {
        val item = mc.thePlayer.heldItem?.item

        event.percentage = when (item) {
            is ItemFood, is ItemPotion, is ItemBucketMilk -> this.consumeMultiplier.get()
            is ItemSword -> this.blockMultiplier.get()
            is ItemBow -> this.bowMultiplier.get()
            else -> 0.2F
        }
    }
}