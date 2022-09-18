package me.liuli.fluidity.inject.mixins.net;

import io.netty.channel.ChannelHandlerContext;
import me.liuli.fluidity.config.ConfigManager;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.fml.common.network.handshake.HandshakeMessageHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandshakeMessageHandler.class, remap = false)
public class MixinHandshakeMessageHandler {

    @Inject(method = "channelRead0", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void handleVanilla(ChannelHandlerContext ctx, FMLHandshakeMessage msg, CallbackInfo ci) {
        if (ConfigManager.INSTANCE.getAntiForge()) {
            ci.cancel();
        }
    }
}
