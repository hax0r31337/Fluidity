package me.liuli.fluidity.util.world

import com.mojang.authlib.GameProfile
import me.liuli.fluidity.util.move.floorPosition
import net.minecraft.block.BlockIce
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockPackedIce
import net.minecraft.block.BlockSlime
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Blocks
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import java.util.UUID
import kotlin.math.*

class EntitySimulatable(world: World) : EntityOtherPlayerMP(world, GameProfile(UUID.randomUUID(), "")) {

    constructor(thePlayer: EntityPlayerSP) : this(thePlayer.worldObj) {
        this.copyLocationAndAnglesFrom(thePlayer)
        this.speedOnGround = thePlayer.speedOnGround
        this.motionX = thePlayer.motionX
        this.motionY = thePlayer.motionY
        this.motionZ = thePlayer.motionZ
    }

    init {
        noClip = false
    }

    var jump = false

    override fun onUpdate() {
//        applyWaterFlow() TODO: water velocity

        // Reset velocity component if it falls under the threshold
        if (abs(motionX) < .005f) motionX = .0
        if (abs(motionY) < .005f) motionY = .0
        if (abs(motionZ) < .005f) motionZ = .0

        if (jump) {
            if (jumpTicks > 0) jumpTicks--
            if (isInWater || isInLava) {
                motionY += 0.04
            } else if(onGround && jumpTicks == 0) {
                motionY = 0.42
                val jumpBoost = getActivePotionEffect(Potion.jump)?.amplifier ?: 0
                if (jumpBoost > 0) {
                    motionY += 0.1 * jumpBoost
                }
                if (isSprinting) {
                    val yaw = Math.toRadians(rotationYaw.toDouble())
                    motionX -= sin(yaw).toFloat() * 0.2
                    motionZ += cos(yaw).toFloat() * 0.2
                }
                jumpTicks = 10
            }
        } else {
            jumpTicks = 0
        }

        moveStrafing *= 0.98f
        moveForward *= 0.98f

        if (isSneaking) {
            moveStrafing *= 0.3f
            moveForward *= 0.3f
        }

        if (isInWater || isInLava) {
            isSprinting = false
            moveInWater()
        } else {
            moveInAir()
        }
    }

    private fun moveInWater() {
//        val lastY = posY
        val inertia = if(isInWater) 0.8 else 0.5
        var horizontalInertia = inertia
        var acceleration = 0.02

        if (isInWater) {
            var strider = EnchantmentHelper.getDepthStriderModifier(this).toDouble()
            if (onGround) {
                strider *= 0.5
            }
            if (strider > 0) {
                horizontalInertia += (0.546 - horizontalInertia) * strider / 3
                acceleration *= (0.7 - acceleration) * strider / 3
            }
        }

        applyHeading(acceleration)
        movePlayer()

        motionY *= inertia
        motionY -= 0.02
        motionX *= horizontalInertia
        motionZ *= horizontalInertia

//        if (isCollidedHorizontally) {
//            val f = width / 2.0
//            val bb = AxisAlignedBB(posX + motionX - f, lastY + motionY + 0.6, posZ + motionZ - f,
//                posX + motionX + f, lastY + motionY + 0.6 + height, posZ + motionZ + f)
//            if(!world.getSurroundingBBs(bb).any { it.intersects(bb) } && getWaterInBB(bb).isNotEmpty()) {
//                motionY = 0.3
//            }
//        }
    }

    private fun moveInAir() {
        var acceleration = 0.02
        var inertia = 0.91
        val blockUnder = worldObj.getBlockState(floorPosition.down())?.block ?: Blocks.air

        if (onGround) {
            inertia *= when(blockUnder) {
                is BlockSlime -> 0.8f
                is BlockIce, is BlockPackedIce -> 0.98f
                else -> 0.6f
            }
            acceleration = (speedOnGround * 0.1627714 / inertia.pow(3)).coerceAtLeast(.0) // acceleration should not be negative
        }

        applyHeading(acceleration)

        if ((worldObj.getBlockState(floorPosition)?.block ?: Blocks.air) is BlockLadder) {
            motionX = motionX.coerceIn(-0.15, 0.15)
            motionZ = motionZ.coerceIn(-0.15, 0.15)
            motionY = motionY.coerceAtLeast(if (isSneaking) .0 else -0.15)
        }

        movePlayer()

        // refresh isOnClimbableBlock cuz position changed
        if (((worldObj.getBlockState(floorPosition)?.block ?: Blocks.air) is BlockLadder)
            && (isCollidedHorizontally || jump)) {
            motionY = 0.2
        }

        // apply friction and gravity
        motionY -= 0.08
        motionX *= inertia
        motionZ *= inertia
        motionY *= 0.98
    }

    private fun movePlayer() {
//        if(isInWeb) {
//            motionX *= 0.25
//            motionY *= 0.25
//            motionZ *= 0.25
//        }
        this.inWater = false
        moveEntity(motionX, motionY, motionZ)
//        if (isInWeb) {
//            motion.set(.0, .0, .0)
//        }
    }

    private fun applyHeading(multiplier: Double) {
        var speed = sqrt(moveStrafing * moveStrafing + moveForward * moveForward).toDouble()
        if (speed < 0.01) return
        speed = multiplier / speed.coerceAtLeast(1.0)

        val strafe = moveStrafing * speed
        val forward = moveForward * speed

        val yaw = Math.toRadians(rotationYaw.toDouble())
        val sinYaw = sin(yaw).toFloat()
        val cosYaw = cos(yaw).toFloat()

        motionX += strafe * cosYaw - forward * sinYaw
        motionZ += forward * cosYaw + strafe * sinYaw
    }
}