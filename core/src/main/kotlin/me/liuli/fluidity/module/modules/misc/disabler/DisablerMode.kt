/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.misc.disabler

import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.event.WorldEvent

abstract class DisablerMode(val name: String) {

    open fun onEnable() {}
    open fun onDisable() {}

    open fun onUpdate(event: UpdateEvent) {}
    open fun onWorld(event: WorldEvent) {}
    open fun onPacket(event: PacketEvent) {}
}