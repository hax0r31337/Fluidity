package me.liuli.fluidity.util.timing

class TheTimer {
    private var time = System.currentTimeMillis()

    fun reset() {
        time = System.currentTimeMillis()
    }

    fun getTimePassed(): Long {
        return System.currentTimeMillis() - time
    }

    fun hasTimePassed(time: Long): Boolean {
        return getTimePassed() >= time
    }
}