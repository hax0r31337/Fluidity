package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.event.Render2DEvent;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSpectator.class)
public class MixinGuiSpectator {
    @Inject(method = "renderTooltip", at = @At("RETURN"))
    private void renderTooltip(ScaledResolution sr, float partialTicks, CallbackInfo callbackInfo) {
        Fluidity.eventManager.call(new Render2DEvent(sr, partialTicks));
    }
}
