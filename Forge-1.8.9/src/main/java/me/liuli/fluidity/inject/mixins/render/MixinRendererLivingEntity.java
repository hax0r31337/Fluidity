package me.liuli.fluidity.inject.mixins.render;

import me.liuli.fluidity.module.modules.client.Targets;
import me.liuli.fluidity.module.modules.render.ESP;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity {

    @Inject(method = "canRenderName", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void canRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (ESP.INSTANCE.getState() && ESP.INSTANCE.getNameValue().get() && Targets.INSTANCE.isTarget(entity, ESP.INSTANCE.getOnlyShowAttackableValue().get()))
            callbackInfoReturnable.setReturnValue(false);
    }
}
