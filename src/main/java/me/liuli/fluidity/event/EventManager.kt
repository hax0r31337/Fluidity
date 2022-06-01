package me.liuli.fluidity.event

import java.lang.reflect.Method

class EventManager {
    private val methods = mutableListOf<ListenerMethod>()

    fun registerListener(listener: Listener) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventMethod::class.java)) {
                methods.add(ListenerMethod(method, listener))
            }
        }
    }

    fun call(event: Event) {
        for (lm in methods) {
            if (lm.listener.listen() && lm.isMatchEvent(event)) {
                try {
                    lm.method.invoke(lm.listener, event)
                } catch (t: Throwable) {
                    Exception("An error occurred while handling the event: ", t).printStackTrace()
                }
            }
        }
    }
}

class ListenerMethod(val method: Method, val listener: Listener) {
    init {
        if (!method.isAccessible) {
            method.isAccessible = true
        }
    }

    fun isMatchEvent(event: Event): Boolean {
        return method.parameterTypes[0] == event.javaClass
    }
}