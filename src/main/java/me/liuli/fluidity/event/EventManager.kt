package me.liuli.fluidity.event

import me.liuli.fluidity.Fluidity

class EventManager {
    private val handlers = mutableMapOf<Class<out Event>, MutableList<Handler>>()

    /**
     * time used in processing event in last second
     */
    var timeCost = 0L
        private set

    private var timeCostThis = 0L
    private var lastSyncTime = System.currentTimeMillis()

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

    fun <T : Event> registerFunction(func: (T) -> Unit, target: Class<T>) {
        registerHandler(HandlerFunction(func, target))
    }

    fun call(event: Event) {
        val start = System.nanoTime()
        for (handler in (handlers[event.javaClass] ?: return)) {
            handler.invoke(event)
        }
        if (Fluidity.DEBUG_MODE) {
            timeCostThis += System.nanoTime() - start
            if (System.currentTimeMillis() - lastSyncTime > 1000) {
                timeCost = timeCostThis
                timeCostThis = 0
                lastSyncTime = System.currentTimeMillis()
            }
        }
    }
}