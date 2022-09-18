package me.liuli.fluidity.event

open class Event

open class EventCancellable : Event() {
    var cancelled = false

    fun cancel() {
        cancelled = true
    }
}