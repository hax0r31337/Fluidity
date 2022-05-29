package me.liuli.fluidity.module.modules.render

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue

object NoFOV : Module("NoFOV", "Disables FOV changes caused by speed effect, etc", ModuleCategory.RENDER) {

    val fovValue = FloatValue("FOV", 1f, 0f, 1.5f)
}