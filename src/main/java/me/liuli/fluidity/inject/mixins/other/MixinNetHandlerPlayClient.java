package me.liuli.fluidity.inject.mixins.other;

import me.liuli.fluidity.module.modules.misc.NoRotateSet;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Redirect(method = "handlePlayerPosLook", at = @At(value="INVOKE", target="Lnet/minecraft/entity/player/EntityPlayer;setPositionAndRotation(DDDFF)V"))
    public void handlePlayerPosLook(EntityPlayer player, double x, double y, double z, float yaw, float pitch) {
        if (NoRotateSet.INSTANCE.getState()) {
            NoRotateSet.INSTANCE.cacheServerRotation(yaw, pitch);
            player.setPosition(x, y, z);
        } else {
            player.setPositionAndRotation(x, y, z, yaw, pitch);
        }
    }

    @ModifyArgs(method = "handlePlayerPosLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void handlePlayerPosLook(Args args) {
        if (NoRotateSet.INSTANCE.getState()) {
            C03PacketPlayer.C06PacketPlayerPosLook packet = args.get(0);
            packet.yaw = NoRotateSet.INSTANCE.getYaw();
            packet.pitch = NoRotateSet.INSTANCE.getPitch();
        }
    }
}
