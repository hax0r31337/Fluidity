package me.liuli.fluidity.inject.mixins.entity;

import com.mojang.authlib.GameProfile;
import me.liuli.fluidity.module.modules.movement.KeepSprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    public abstract GameProfile getGameProfile();

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    protected abstract String getSwimSound();

    @Shadow
    public abstract FoodStats getFoodStats();

    @Shadow
    protected int flyToggleTimer;

    @Shadow
    public PlayerCapabilities capabilities;

    @Shadow
    public abstract int getItemInUseDuration();

    @Shadow
    public abstract ItemStack getItemInUse();

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void onAttackTargetEntityWithCurrentItem(CallbackInfo callbackInfo) {
        if (KeepSprint.INSTANCE.getState()) {
            System.out.println("setSprinting(false)");
            final float multiplier = 0.6f + 0.4f * KeepSprint.INSTANCE.getMultiplierValue().get();
            this.motionX = this.motionX / 0.6 * multiplier;
            this.motionZ = this.motionZ / 0.6 * multiplier;
            if (KeepSprint.INSTANCE.getNoBreakSprintValue().get()) {
                this.setSprinting(true);
            }
        }
    }

}
