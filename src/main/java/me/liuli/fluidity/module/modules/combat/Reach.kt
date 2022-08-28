package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import kotlin.math.max

object Reach : Module("Reach", "Increase your hand length", ModuleCategory.COMBAT) {

    val combatReachValue = FloatValue("CombatReach", 3.5f, 3f, 7f)
    val buildReachValue = FloatValue("BuildReach", 5f, 4.5f, 7f)

    val reach: Double
        get() = if(state) combatReachValue.get().toDouble() else 3.0

    val maxRange: Float
        get() = max(combatReachValue.get(), buildReachValue.get())
}