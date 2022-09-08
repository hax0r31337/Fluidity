package me.liuli.fluidity.inject.mixins.net;

import me.liuli.fluidity.config.ConfigManager;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NetworkDispatcher.class, remap = false)
public class MixinNetworkDispatcher {

    @Inject(method = "handleVanilla", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void handleVanilla(Packet<?> packet, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigManager.INSTANCE.getAntiForge()) {
            cir.setReturnValue(false);
        }
    }
}
