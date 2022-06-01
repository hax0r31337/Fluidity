package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.event.GuiKeyEvent;
import me.liuli.fluidity.event.RenderScreenEvent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Shadow
    protected List<GuiButton> buttonList;
    @Shadow
    protected FontRenderer fontRendererObj;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
        Fluidity.eventManager.call(new RenderScreenEvent(mouseX, mouseY, partialTicks));
    }

    @Inject(method = "keyTyped", at = @At("RETURN"))
    private void keyTyped(char typedChar, int keyCode, CallbackInfo callbackInfo) {
        Fluidity.eventManager.call(new GuiKeyEvent(typedChar, keyCode));
    }
}
