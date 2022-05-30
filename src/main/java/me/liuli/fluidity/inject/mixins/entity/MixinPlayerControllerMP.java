package me.liuli.fluidity.inject.mixins.entity;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.event.AttackEvent;
import me.liuli.fluidity.event.ClickBlockEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"))
    private void attackEntity(EntityPlayer entityPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
        if (targetEntity == null)
            return;

        Fluidity.eventManager.callEvent(new AttackEvent(targetEntity));
    }

    @Inject(method = "onPlayerRightClick", at = @At(value = "HEAD"))
    public void onPlayerRightClick(EntityPlayerSP p_onPlayerRightClick_1_, WorldClient p_onPlayerRightClick_2_, ItemStack p_onPlayerRightClick_3_, BlockPos p_onPlayerRightClick_4_, EnumFacing p_onPlayerRightClick_5_, Vec3 p_onPlayerRightClick_6_, CallbackInfoReturnable<Boolean> cb) {
        Fluidity.eventManager.callEvent(new ClickBlockEvent(ClickBlockEvent.Type.RIGHT, p_onPlayerRightClick_4_, p_onPlayerRightClick_5_));
    }
}
