package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.other.nextFloat
import me.liuli.fluidity.util.timing.ClickTimer
import net.minecraft.client.settings.KeyBinding
import kotlin.random.Random

class AutoClicker : Module("AutoClicker", "Constantly clicks when holding down a mouse button.", category = ModuleCategory.COMBAT) {

    private val minCpsValue = IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue = IntValue("MaxCPS", 12, 1, 20)

    private val rightValue = BoolValue("Right", true)
    private val leftValue = BoolValue("Left", true)
    private val jitterValue = FloatValue("Jitter", 0.0f, 0.0f, 5.0f)

    private val leftClickTimer = ClickTimer()
    private val rightClickTimer = ClickTimer()

    override fun onEnable() {
        leftClickTimer.update(minCpsValue.get(), maxCpsValue.get())
        rightClickTimer.update(minCpsValue.get(), maxCpsValue.get())
    }

    @EventMethod
    fun onRender(event: Render3DEvent) {
        // Left click
        if (mc.gameSettings.keyBindAttack.isKeyDown && leftValue.get() &&
            leftClickTimer.canClick() && mc.playerController.curBlockDamageMP == 0F) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Minecraft Click Handling

            leftClickTimer.update(minCpsValue.get(), maxCpsValue.get())
        }

        // Right click
        if (mc.gameSettings.keyBindUseItem.isKeyDown && !mc.thePlayer!!.isUsingItem && rightValue.get() && rightClickTimer.canClick()) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode) // Minecraft Click Handling

            rightClickTimer.update(minCpsValue.get(), maxCpsValue.get())
        }
    }

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (jitterValue.get() != 0f && (leftValue.get() && mc.gameSettings.keyBindAttack.isKeyDown && mc.playerController.curBlockDamageMP == 0F
                    || rightValue.get() && mc.gameSettings.keyBindUseItem.isKeyDown && !thePlayer.isUsingItem)) {
            if (Random.nextBoolean()) thePlayer.rotationYaw += nextFloat(-jitterValue.get(), jitterValue.get())

            if (Random.nextBoolean()) {
                thePlayer.rotationPitch += nextFloat(-jitterValue.get(), jitterValue.get())

                // Make sure pitch is not going into unlegit values
                if (thePlayer.rotationPitch > 90)
                    thePlayer.rotationPitch = 90F
                else if (thePlayer.rotationPitch < -90)
                    thePlayer.rotationPitch = -90F
            }
        }
    }
}