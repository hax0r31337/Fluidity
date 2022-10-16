/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.event

open class Event

open class EventCancellable : Event() {
    var cancelled = false

    fun cancel() {
        cancelled = true
    }
}