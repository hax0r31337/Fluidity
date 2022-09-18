package me.liuli.fluidity.util.timing

open class TheTimer {
    protected var time = System.currentTimeMillis()

    fun reset() {
        time = System.currentTimeMillis()
    }

    fun getTimePassed(): Long {
        return System.currentTimeMillis() - time
    }

    fun hasTimePassed(time: Int) = hasTimePassed(time.toLong())

    fun hasTimePassed(time: Long): Boolean {
        return getTimePassed() >= time
    }
}