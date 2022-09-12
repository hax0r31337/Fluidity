package me.liuli.fluidity.inject.mixins.net;

import me.liuli.fluidity.module.modules.misc.NoRotateSet;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Shadow @Final private NetworkManager netManager;

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

    @Inject(method = "handlePlayerPosLook", at = @At("HEAD"), cancellable = true)
    public void handlePlayerPosLook(S08PacketPlayerPosLook pk, CallbackInfo ci) {
        double x = pk.getX();
        double y = pk.getY();
        double z = pk.getZ();
        if (x >= Integer.MAX_VALUE || x <= Integer.MIN_VALUE ||
                y >= Integer.MAX_VALUE || y <= Integer.MIN_VALUE ||
                z >= Integer.MAX_VALUE || z <= Integer.MIN_VALUE) {
            System.out.println("X=" +x + ",Y=" +y+",Z="+z);
            this.netManager.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, pk.yaw, pk.pitch, false));
            ci.cancel();
        }
    }
}
