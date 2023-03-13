/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks.impl.gui

import me.liuli.fluidity.inject.hooks.Hook
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.module.modules.player.InventoryHelper
import me.liuli.fluidity.util.render.glColor
import me.liuli.fluidity.util.render.quickDrawRect
import me.yuugiri.hutil.processor.hook.MethodHookParam
import net.minecraft.inventory.Slot
import org.lwjgl.opengl.GL11

class HookGuiContainer : HookProvider("net.minecraft.client.gui.inventory.GuiContainer") {

    @Hook(method = "drawSlot", type = Hook.Type("ENTER"))
    fun drawSlot(param: MethodHookParam) {
        val slot = param.args[0] as Slot

        if (slot.stack == null || !InventoryHelper.state) {
            return
        }

        var color = -1
        if (InventoryHelper.usefulItems.contains(slot)) {
            color = InventoryHelper.usefulColorValue
        } else if (InventoryHelper.garbageItems.contains(slot)) {
            color = InventoryHelper.garbageColorValue
        }
        if (color != -1) {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            glColor(color)
            quickDrawRect(slot.xDisplayPosition.toFloat(), slot.yDisplayPosition.toFloat(), (slot.xDisplayPosition + 16).toFloat(), (slot.yDisplayPosition + 16).toFloat())
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
        }
    }
}