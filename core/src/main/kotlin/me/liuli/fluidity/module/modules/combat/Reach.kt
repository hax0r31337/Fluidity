/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue

object Reach : Module("Reach", "Increase your hand length", ModuleCategory.COMBAT) {

    val combatReachValue by FloatValue("CombatReach", 3.5f, 3f, 7f)
    val buildReachValue by FloatValue("BuildReach", 5f, 4.5f, 7f)

    val reach: Double
        get() = if(state) combatReachValue.toDouble() else 3.0
}