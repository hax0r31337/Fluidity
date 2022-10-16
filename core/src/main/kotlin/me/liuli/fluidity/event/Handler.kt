/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.event

interface Handler {

    val target: Class<out Event>

    fun invoke(event: Event)
}

class HandlerFunction<T : Event>(private val func: (T) -> Unit, override val target: Class<T>) : Handler {

    override fun invoke(event: Event) {
        func(event as T)
    }
}