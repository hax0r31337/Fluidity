/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.value

import com.google.gson.JsonElement
import me.liuli.fluidity.util.client.logWarn
import kotlin.reflect.KProperty

abstract class Value<T>(val name: String, protected var value: T) {
    val defaultValue = value

    open fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
        } catch (e: Exception) {
            logWarn("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    open fun get() = value

    open fun changeValue(value: T) {
        this.value = value
    }

    open fun reset() {
        value = defaultValue
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}
    operator fun getValue(obj: Any, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(obj: Any, property: KProperty<*>, valueIn: T) {
        value = valueIn
    }
}