/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.render

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.hooks.MethodProcess
import me.liuli.fluidity.module.modules.combat.Reach
import me.liuli.fluidity.module.modules.combat.Reach.reach
import me.liuli.fluidity.module.modules.render.CameraClip
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.other.asmName
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.MethodHookParam
import me.yuugiri.hutil.util.forEach
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntityAnimal
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

object HookEntityRenderer : HookProvider("net.minecraft.client.renderer.EntityRenderer") {

    @Hook(method = "renderWorldPass", type = Hook.Type("FIELD", "net/minecraft/client/renderer/EntityRenderer;renderHand"))
    fun renderWorldPass(param: MethodHookParam) {
        Fluidity.eventManager.call(Render3DEvent(param.args[1] as Float))
    }

    @MethodProcess(method = "getMouseOver")
    fun getMouseOver(obfuscationMap: AbstractObfuscationMap?, method: MethodNode) {
        var hasHooked = false
        method.instructions.forEach { node ->
            if (node !is MethodInsnNode || !node.desc.endsWith(")D")) return@forEach
            val obf = AbstractObfuscationMap.methodObfuscationRecord(obfuscationMap, node.owner, node.name, node.desc)
            if (obf.name == "distanceTo") {
                hasHooked = true
                method.instructions.insert(node, MethodInsnNode(Opcodes.INVOKESTATIC, HookEntityRenderer::class.java.asmName, "retraceEntity", "(DF)D", false))
                method.instructions.insert(node, VarInsnNode(Opcodes.FLOAD, 1))
            }
        }
        if (!hasHooked) throw UnknownError("unable to found hook point for net.minecraft.client.renderer.EntityRenderer;getMouseOver(F)V")
    }

    @JvmStatic
    fun retraceEntity(original: Double, p_getMouseOver_1_: Float): Double {
        if (Reach.state) {
            val entity = Minecraft.getMinecraft().renderViewEntity
            val movingObjectPosition = entity.rayTrace(reach, p_getMouseOver_1_)
            if (movingObjectPosition != null) return movingObjectPosition.hitVec.distanceTo(entity.getPositionEyes(p_getMouseOver_1_))
        }
        return original
    }

    @Hook(method = "orientCamera", type = Hook.Type("INVOKE", "net/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"))
    fun orientCamera(param: MethodHookParam) {
        if (CameraClip.state) {
            param.result = null
            val partialTicks = param.args[0] as Float
            val thisObject = param.thisObject as EntityRenderer

            val entity = mc.renderViewEntity
            var f = entity.eyeHeight
            if (entity is EntityLivingBase && entity.isPlayerSleeping) {
                f = (f.toDouble() + 1.0).toFloat()
                GlStateManager.translate(0f, 0.3f, 0.0f)
                if (!mc.gameSettings.debugCamEnable) {
//                    val blockpos = BlockPos(entity)
//                    val iblockstate: IBlockState = mc.theWorld.getBlockState(blockpos)
//                    ForgeHooksClient.orientBedCamera(mc.theWorld, blockpos, iblockstate, entity)
                    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0f, 0.0f, -1.0f, 0.0f)
                    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0f, 0.0f, 0.0f)
                }
            } else if (mc.gameSettings.thirdPersonView > 0) {
                val d3 = (thisObject.thirdPersonDistanceTemp + (thisObject.thirdPersonDistance - thisObject.thirdPersonDistanceTemp) * partialTicks).toDouble()
                if (mc.gameSettings.debugCamEnable) {
                    GlStateManager.translate(0.0f, 0.0f, (-d3).toFloat())
                } else {
                    val f1 = entity.rotationYaw
                    var f2 = entity.rotationPitch
                    if (mc.gameSettings.thirdPersonView == 2) f2 += 180.0f
                    if (mc.gameSettings.thirdPersonView == 2) GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f)
                    GlStateManager.rotate(entity.rotationPitch - f2, 1.0f, 0.0f, 0.0f)
                    GlStateManager.rotate(entity.rotationYaw - f1, 0.0f, 1.0f, 0.0f)
                    GlStateManager.translate(0.0f, 0.0f, (-d3).toFloat())
                    GlStateManager.rotate(f1 - entity.rotationYaw, 0.0f, 1.0f, 0.0f)
                    GlStateManager.rotate(f2 - entity.rotationPitch, 1.0f, 0.0f, 0.0f)
                }
            } else {
                GlStateManager.translate(0.0f, 0.0f, -0.1f)
            }
            if (!mc.gameSettings.debugCamEnable) {
                var yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0f
                val pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks
                val roll = 0.0f
                if (entity is EntityAnimal) {
                    yaw = entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * partialTicks + 180.0f
                }
//                val block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entity, partialTicks)
//                val event = CameraSetup(this as EntityRenderer, entity, block, partialTicks.toDouble(), yaw, pitch, roll)
//                MinecraftForge.EVENT_BUS.post(event)
                GlStateManager.rotate(roll, 0.0f, 0.0f, 1.0f)
                GlStateManager.rotate(pitch, 1.0f, 0.0f, 0.0f)
                GlStateManager.rotate(yaw, 0.0f, 1.0f, 0.0f)
            }
            GlStateManager.translate(0.0f, -f, 0.0f)
            val d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks.toDouble()
            val d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks.toDouble() + f.toDouble()
            val d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks.toDouble()
            thisObject.cloudFog = mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks)
        }
    }
}