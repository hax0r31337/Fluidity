/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.util.timing

class ClickTimer : TheTimer() {

    private var delay: Long = 0

    fun canClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - time >= delay
    }

    fun update(minCPS: Int, maxCPS: Int) {
        delay = ((Math.random() * (1000 / minCPS - 1000 / maxCPS + 1)) + 1000 / maxCPS).toLong()
        time = System.currentTimeMillis()
    }
}