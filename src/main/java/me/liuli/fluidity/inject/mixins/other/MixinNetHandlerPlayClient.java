package me.liuli.fluidity.inject.mixins.other;

import me.liuli.fluidity.module.modules.misc.NoRotateSet;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Redirect(method = "handlePlayerPosLook", at = @At(value="INVOKE", target="Lnet/minecraft/entity/player/EntityPlayer;setPositionAndRotation(DDDFF)V"))
    public void handlePlayerPosLook(EntityPlayer player, double x, double y, double z, float yaw, float pitch) {
        if (NoRotateSet.INSTANCE.getState()) {
            player.setPosition(x, y, z);
        } else {
            player.setPositionAndRotation(x, y, z, yaw, pitch);
        }
    }
}
