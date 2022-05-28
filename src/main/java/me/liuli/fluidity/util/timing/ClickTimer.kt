package me.liuli.fluidity.util.timing

class ClickTimer {

    private var lastClickTime: Long = 0
    private var delay: Long = 0

    fun canClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - lastClickTime >= delay
    }

    fun update(minCPS: Int, maxCPS: Int) {
        delay = ((Math.random() * (1000 / minCPS - 1000 / maxCPS + 1)) + 1000 / maxCPS).toLong()
        lastClickTime = System.currentTimeMillis()
    }
}