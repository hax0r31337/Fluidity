package me.liuli.fluidity.util.world

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.lastReportedPitch
import me.liuli.fluidity.util.move.lastReportedYaw
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EntitySelectors
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(1F)
    val pos = getNearestPointBB(eyes, entity.entityBoundingBox)
    val xDist = abs(pos.xCoord - eyes.xCoord)
    val yDist = abs(pos.yCoord - eyes.yCoord)
    val zDist = abs(pos.zCoord - eyes.zCoord)
    return sqrt(xDist.pow(2) + yDist.pow(2) + zDist.pow(2))
}

private fun getNearestPointBB(eye: Vec3, box: AxisAlignedBB): Vec3 {
    val origin = doubleArrayOf(eye.xCoord, eye.yCoord, eye.zCoord)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        if (origin[i] > destMaxs[i]) origin[i] = destMaxs[i] else if (origin[i] < destMins[i]) origin[i] = destMins[i]
    }
    return Vec3(origin[0], origin[1], origin[2])
}


fun rayTraceEntity(range: Double, entity: Entity = mc.thePlayer, yaw: Float = lastReportedYaw, pitch: Float = lastReportedPitch, entityFilter: (Entity) -> Boolean = { true }): Entity? {
    mc.theWorld ?: return null

    var blockReachDistance = range
    val eyePosition = entity.getPositionEyes(1f)
    val yawCos = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
    val yawSin = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
    val pitchCos = -MathHelper.cos(-pitch * 0.017453292f)
    val pitchSin = MathHelper.sin(-pitch * 0.017453292f)
    val entityLook = Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
    val vector = eyePosition.addVector(
        entityLook.xCoord * blockReachDistance,
        entityLook.yCoord * blockReachDistance,
        entityLook.zCoord * blockReachDistance
    )
    val entityList = mc.theWorld.getEntitiesInAABBexcluding(
        entity,
        entity.entityBoundingBox.addCoord(
            entityLook.xCoord * blockReachDistance,
            entityLook.yCoord * blockReachDistance,
            entityLook.zCoord * blockReachDistance
        ).expand(1.0, 1.0, 1.0),
        Predicates.and(EntitySelectors.NOT_SPECTATING,
            Predicate { it?.canBeCollidedWith() ?: false })
    )
    var pointedEntity: Entity? = null
    for (entity in entityList) {
        if (!entityFilter(entity)) continue
        val collisionBorderSize = entity.collisionBorderSize
        val axisAlignedBB = entity.entityBoundingBox.expand(
            collisionBorderSize.toDouble(),
            collisionBorderSize.toDouble(),
            collisionBorderSize.toDouble()
        )
        val movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vector)
        if (axisAlignedBB.isVecInside(eyePosition)) {
            if (blockReachDistance >= 0.0) {
                pointedEntity = entity
                blockReachDistance = 0.0
            }
        } else if (movingObjectPosition != null) {
            val eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec)
            if (eyeDistance < blockReachDistance || blockReachDistance == 0.0) {
                if (entity === entity.ridingEntity && !entity.canRiderInteract()) {
                    if (blockReachDistance == 0.0) pointedEntity = entity
                } else {
                    pointedEntity = entity
                    blockReachDistance = eyeDistance
                }
            }
        }
    }
    return pointedEntity
}