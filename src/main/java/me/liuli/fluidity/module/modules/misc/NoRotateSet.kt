package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory

object NoRotateSet : Module("NoRotateSet", "Prevents the server from rotating your head", ModuleCategory.MISC) {

    var yaw = 0f
    var pitch = 0f

    fun cacheServerRotation(yaw: Float, pitch: Float) {
        this.yaw = yaw
        this.pitch = pitch
    }

}