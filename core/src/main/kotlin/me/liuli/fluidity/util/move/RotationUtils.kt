package me.liuli.fluidity.util.move

import me.liuli.fluidity.util.mc
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt


val EntityPlayerSP.serverRotationYaw: Float
    get() = if (!silentRotationYaw.isNaN()) silentRotationYaw else this.rotationYaw

val EntityPlayerSP.serverRotationPitch: Float
    get() = if (!silentRotationPitch.isNaN()) silentRotationPitch else this.rotationPitch

var silentRotationYaw = Float.NaN
var silentRotationPitch = Float.NaN
var lastReportedYaw = 0f
var lastReportedPitch = 0f

/**
 * handle calls from hook
 */
object RotationUtils {

    fun flushLastReported() {
        lastReportedYaw = mc.thePlayer.serverRotationYaw
        lastReportedPitch = mc.thePlayer.serverRotationPitch
        silentRotationYaw = Float.NaN
        silentRotationPitch = Float.NaN
    }

    fun applyVisualYawUpdate() {
        if (!silentRotationYaw.isNaN()) {
            mc.thePlayer.rotationYawHead = silentRotationYaw
            mc.thePlayer.renderYawOffset = silentRotationYaw
        }
    }

    @JvmStatic
    fun rotationYaw(): Float {
        return mc.thePlayer.serverRotationYaw
    }

    @JvmStatic
    fun rotationPitch(): Float {
        return mc.thePlayer.serverRotationPitch
    }
}

fun setServerRotation(yaw: Float, pitch: Float) {
    // fix GCD sensitivity to bypass some anti-cheat measures
    fixSensitivity(yaw, pitch).also {
        silentRotationYaw = it.first
        silentRotationPitch = it.second
    }
}

fun setClientRotation(yaw: Float, pitch: Float) {
    fixSensitivity(yaw, pitch).also {
        mc.thePlayer.rotationYaw = it.first
        mc.thePlayer.rotationPitch = it.second
    }
}

/**
 * Calculate difference between the client rotation and your entity
 *
 * @param entity your entity
 * @return difference between rotation
 */
fun getRotationDifference(entity: Entity): Double {
    val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
    return getRotationDifference(rotation.first, rotation.second, mc.thePlayer.serverRotationYaw, mc.thePlayer.serverRotationPitch)
}

/**
 * Calculate difference between two rotations
 *
 * @param a rotation
 * @param b rotation
 * @return difference between rotation
 */
fun getRotationDifference(aYaw: Float, aPitch: Float, bYaw: Float, bPitch: Float): Double {
    return hypot(getAngleDifference(aYaw, bYaw).toDouble(), (aPitch - bPitch).toDouble())
}

/**
 * Get the center of a box
 *
 * @param bb your box
 * @return center of box
 */
fun getCenter(bb: AxisAlignedBB): Vec3 {
    return Vec3(
        bb.minX + (bb.maxX - bb.minX) * 0.5,
        bb.minY + (bb.maxY - bb.minY) * 0.5,
        bb.minZ + (bb.maxZ - bb.minZ) * 0.5
    )
}

/**
 * Translate vec to rotation
 *
 * @param vec     target vec
 * @param predict predict new location of your body
 * @return rotation
 */
fun toRotation(vec: Vec3, predict: Boolean): Pair<Float, Float> {
    val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)
    if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
    val diffX = vec.xCoord - eyesPos.xCoord
    val diffY = vec.yCoord - eyesPos.yCoord
    val diffZ = vec.zCoord - eyesPos.zCoord
    return Pair(
        MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
        MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
    )
}

/**
 * Calculate difference between two angle points
 *
 * @param a angle point
 * @param b angle point
 * @return difference between angle points
 */
fun getAngleDifference(a: Float, b: Float): Float {
    return ((a - b) % 360f + 540f) % 360f - 180f
}

/**
 * Limit your rotation using a turn speed
 *
 * @param currentRotation your current rotation
 * @param targetRotation your goal rotation
 * @param turnSpeed your turn speed
 * @return limited rotation
 */
fun limitAngleChange(aYaw: Float, aPitch: Float, bYaw: Float, bPitch: Float, turnSpeed: Float): Pair<Float, Float> {
    val yawDifference = getAngleDifference(bYaw, aYaw)
    val pitchDifference = getAngleDifference(bPitch, aPitch)
    return Pair(
        aYaw + if (yawDifference > turnSpeed) turnSpeed else yawDifference.coerceAtLeast(-turnSpeed),
        aPitch + if (pitchDifference > turnSpeed) turnSpeed else pitchDifference.coerceAtLeast(-turnSpeed)
    )
}

fun fixSensitivity(yaw: Float, pitch: Float, sensitivity: Float = mc.gameSettings.mouseSensitivity): Pair<Float, Float> {
    val f = sensitivity * 0.6F + 0.2F
    val gcd = f * f * f * 1.2F

    // fix yaw
    var deltaYaw = yaw - lastReportedYaw
    deltaYaw -= deltaYaw % gcd

    // fix pitch
    var deltaPitch = pitch - lastReportedPitch
    deltaPitch -= deltaPitch % gcd

    return Pair(lastReportedYaw + deltaYaw, lastReportedPitch + deltaPitch)
}

fun jitterRotation(jitter: Float, originalYaw: Float = mc.thePlayer.serverRotationYaw, originalPitch: Float = mc.thePlayer.serverRotationPitch): Pair<Float, Float> {
    val yaw = originalYaw + (Math.random() - 0.5) * jitter
    val pitch = originalPitch + (Math.random() - 0.5) * jitter
    return Pair(yaw.toFloat(), pitch.toFloat().coerceIn(-90f, 90f))
}

fun getViewVector(yaw: Float, pitch: Float): Vec3d {
    val csPitch = Math.cos(pitch.toDouble())
    val snPitch = Math.sin(pitch.toDouble())
    val csYaw = Math.cos(yaw.toDouble())
    val snYaw = Math.sin(yaw.toDouble())
    return Vec3d(-snYaw * csPitch, snPitch, -csYaw * csPitch)
}

fun lookAt(x: Double, y: Double, z: Double) {
    val rotation = toRotation(Vec3(x, y, z), true)
    setClientRotation(rotation.first, rotation.second)
}

/**
 * Face target with bow
 *
 * @param target your enemy
 * @param silent client side rotations
 * @param predict predict new enemy position
 * @param predictSize predict size of predict
 */
fun faceBow(targetX: Double, targetY: Double, targetZ: Double): Pair<Float, Float> {
    val posX = targetX - mc.thePlayer.posX
    val posY = targetY - mc.thePlayer.posY
    val posZ = targetZ - mc.thePlayer.posZ
    val posSqrt = sqrt(posX * posX + posZ * posZ)
    var velocity = mc.thePlayer.itemInUseDuration / 20f
    velocity = (velocity * velocity + velocity * 2) / 3
    if (velocity > 1) velocity = 1f
    velocity = velocity * velocity
    return  Pair(
        (atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90,
        -Math.toDegrees(atan((velocity - sqrt(velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity)))) / (0.006f * posSqrt))).toFloat().coerceIn(-90f, 90f)
    )
}