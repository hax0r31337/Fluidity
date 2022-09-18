package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue

object HitBox : Module("HitBox", "Makes hitboxes of targets bigger", ModuleCategory.COMBAT) {

    val sizeValue = FloatValue("Size", 0.4F, 0F, 1F)
}