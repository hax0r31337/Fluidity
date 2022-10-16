/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc

object NoJumpDelay : Module("NoJumpDelay", "Removes delay between jumps", ModuleCategory.MISC) {

    @Listen
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.jumpTicks = 0
    }
}