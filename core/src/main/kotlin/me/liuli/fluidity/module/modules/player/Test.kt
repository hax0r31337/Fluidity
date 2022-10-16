/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.other.randomString
import net.minecraft.util.Session
import java.util.*

class Test : Module("Test", "Debug only", ModuleCategory.PLAYER) {

    override fun onEnable() {
        mc.session = Session("Fluidity_${randomString(7)}", UUID.randomUUID().toString(), "-", "legacy")
        state = false
    }
}