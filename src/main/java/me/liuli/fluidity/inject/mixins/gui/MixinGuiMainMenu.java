package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.Fluidity;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends MixinGuiScreen {
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
        final String str = Fluidity.COLORED_NAME+"§f v"+ Fluidity.VERSION;
        // render client brand
        this.fontRendererObj.drawString(str, this.width - this.fontRendererObj.getStringWidth(str) - 2, 2, 16777215, false);
    }
}
