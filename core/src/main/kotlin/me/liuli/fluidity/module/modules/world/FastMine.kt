/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue

object FastMine : Module("FastMine", "Makes you mine blocks faster", ModuleCategory.WORLD) {

    val multiplier = FloatValue("Multiplier", 1.1f, 1f, 2f)
}