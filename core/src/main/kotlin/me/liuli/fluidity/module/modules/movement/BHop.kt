/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.strafe

class BHop : Module("BunnyHop", "speed up movement", ModuleCategory.MOVEMENT) {

    private val speedValue by FloatValue("Speed", 0.4f, 0.1f, 3f)

    @Listen
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.strafe(speedValue)
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }
    }
}