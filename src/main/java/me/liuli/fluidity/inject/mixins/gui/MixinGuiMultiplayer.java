package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.config.ConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends MixinGuiScreen {

    private GuiButton button;


    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        button = new GuiButton(996, 8, 8, 98, 20,
                "AntiForge: " + (ConfigManager.Companion.getAntiForge() ? "§aON" : "§cOFF"));
        buttonList.add(button);
    }


    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        if (button.id == 996) {
            ConfigManager.Companion.setAntiForge(!ConfigManager.Companion.getAntiForge());
            button.displayString = "AntiForge: " + (ConfigManager.Companion.getAntiForge() ? "§aON" : "§cOFF");
            callbackInfo.cancel();
        }
    }
}
