package me.liuli.fluidity.event

import java.lang.reflect.Method
import java.lang.Class

class EventManager {
    private val handlers = mutableMapOf<Class<out Event>, MutableList<Handler>>()

    fun registerListener(listener: Listener) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventMethod::class.java)) {
                registerHandler(HandlerMethod(method, listener))
            }
        }
    }

    fun registerHandler(handler: Handler) {
        (handlers[handler.target] ?: mutableListOf<Handler>().also { handlers[handler.target] = it })
            .add(handler)
    }

    fun <T : Event> registerFunction(func: (T) -> Unit, target: Class<T>) {
        registerHandler(HandlerFunction(func, target))
    }

    fun call(event: Event) {
        for (handler in (handlers[event.javaClass] ?: return)) {
            handler.invoke(event)
        }
    }
}