/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.event

class EventManager {
    private val handlers = mutableMapOf<Class<out Event>, MutableList<Handler>>()

    fun registerListener(listener: Listener) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(Listen::class.java)) {
                registerHandler(HandlerMethod(method, listener))
            }
        }
    }

    fun registerHandler(handler: Handler) {
        (handlers[handler.target] ?: mutableListOf<Handler>().also { handlers[handler.target] = it })
            .add(handler)
    }

    fun <T : Event> registerFunction(target: Class<T>, func: (T) -> Unit) {
        registerHandler(HandlerFunction(func, target))
    }

    fun emit(event: Event) {
        for (handler in (handlers[event.javaClass] ?: return)) {
            handler.invoke(event)
        }
    }
}