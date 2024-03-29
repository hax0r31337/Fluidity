/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.util.mc

class GameSpeed : Module("GameSpeed", "Change Minecraft tick speed", ModuleCategory.WORLD) {

    private val speedValue by FloatValue("Speed", 1.5f, 0.1f, 5f)

    @Listen
    fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = speedValue
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}