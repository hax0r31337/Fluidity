/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PreMotionEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.util.mc

class ViewBobbing : Module("ViewBobbing", "Makes you custom view bobbing effect", ModuleCategory.RENDER) {

    private val value = FloatValue("Value", 0.1f, 0f, 0.5f)
    private val offgroundValue = BoolValue("OffGround", false)

    @Listen
    fun onPreMotion(event: PreMotionEvent) {
        if (offgroundValue.get() || mc.thePlayer.onGround) {
            mc.thePlayer.cameraYaw = value.get()
            mc.thePlayer.prevCameraYaw = value.get()
        }
    }
}