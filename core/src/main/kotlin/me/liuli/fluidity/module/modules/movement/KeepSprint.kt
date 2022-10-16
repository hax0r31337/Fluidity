/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue

object KeepSprint : Module("KeepSprint", "Make attack not able to disturb sprint.", ModuleCategory.MOVEMENT) {

    val multiplierValue = FloatValue("Multiplier", 1f, 0f, 1f)
    val noBreakSprintValue = BoolValue("NoBreakSprint", false)

}