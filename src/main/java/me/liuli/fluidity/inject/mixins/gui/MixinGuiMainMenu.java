package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.Fluidity;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends MixinGuiScreen {
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
        // render client brand
        this.fontRendererObj.drawString(Fluidity.getColoredName()+"§f v"+ Fluidity.getVersion(), 2, this.height - (10 + 4 * (this.fontRendererObj.FONT_HEIGHT + 1)), 16777215, false);
    }
}
