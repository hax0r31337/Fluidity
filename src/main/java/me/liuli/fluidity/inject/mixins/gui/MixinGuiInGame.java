package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.event.Render2DEvent;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiInGame {
    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void renderTooltip(ScaledResolution sr, float partialTicks, CallbackInfo callbackInfo) {
        Fluidity.eventManager.callEvent(new Render2DEvent(sr, partialTicks));
    }
}
