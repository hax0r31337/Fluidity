package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.module.modules.player.InventoryHelper;
import me.liuli.fluidity.util.render.RenderUtilsKt;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends MixinGuiScreen {

    @Shadow
    private boolean isMouseOverSlot(Slot p_isMouseOverSlot_1_, int p_isMouseOverSlot_2_, int p_isMouseOverSlot_3_) {
        return false;
    }

    @Shadow
    public Container inventorySlots;

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void drawSlot(Slot slot, CallbackInfo ci) {
        if (slot.getStack() == null || !InventoryHelper.INSTANCE.getState()) {
            return;
        }

        int color = -1;
        if (InventoryHelper.INSTANCE.getUsefulItems().contains(slot)) {
            color = InventoryHelper.INSTANCE.getUsefulColorValue().get();
        } else if (InventoryHelper.INSTANCE.getGarbageItems().contains(slot)) {
            color = InventoryHelper.INSTANCE.getGarbageColorValue().get();
        }
        if (color != -1) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            RenderUtilsKt.glColor(color);
            RenderUtilsKt.quickDrawRect(slot.xDisplayPosition, slot.yDisplayPosition, slot.xDisplayPosition + 16, slot.yDisplayPosition + 16);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }
}
