package me.liuli.fluidity.inject.mixins.render;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.event.TextEvent;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {
    @ModifyVariable(method = "renderString", at = @At("HEAD"), ordinal = 0)
    private String renderString(final String string) {
        if (string == null)
            return string;

        final TextEvent textEvent = new TextEvent(string);
        Fluidity.eventManager.callEvent(textEvent);
        return textEvent.getText();
    }

    @ModifyVariable(method = "getStringWidth", at = @At("HEAD"), ordinal = 0)
    private String getStringWidth(final String string) {
        if (string == null)
            return string;

        final TextEvent textEvent = new TextEvent(string);
        Fluidity.eventManager.callEvent(textEvent);
        return textEvent.getText();
    }
}
