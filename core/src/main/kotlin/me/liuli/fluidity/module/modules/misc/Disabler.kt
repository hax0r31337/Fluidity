/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.event.WorldEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.misc.disabler.DisablerMode
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.other.resolveInstances

class Disabler : Module("Disabler", "Disable anticheats through exploit", ModuleCategory.MISC) {

    private val modeList = resolveInstances("${this.javaClass.`package`.name}.disabler", DisablerMode::class.java)
        .sortedBy { it.name }

    private val modeValue by ListValue("Mode", modeList.map { it.name }.toTypedArray(), modeList.first().name)

    private val mode: DisablerMode
        get() = modeList.find { it.name == modeValue }!!

    override fun onEnable() {
        mode.onEnable()
    }

    override fun onDisable() {
        mode.onDisable()
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate(event)
    }

    @Listen
    fun onWorld(event: WorldEvent) {
        mode.onWorld(event)
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }
}