package me.liuli.fluidity.inject.mixins.other;

import me.liuli.fluidity.module.modules.world.FastMine;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "getPlayerRelativeBlockHardness", at = @At("RETURN"), cancellable = true)
    public void getPlayerRelativeBlockHardness(EntityPlayer p_getPlayerRelativeBlockHardness_1_, World p_getPlayerRelativeBlockHardness_2_, BlockPos p_getPlayerRelativeBlockHardness_3_, CallbackInfoReturnable<Float> cir) {
        if (FastMine.INSTANCE.getState()) {
            cir.setReturnValue(FastMine.INSTANCE.getMultiplier().get() * cir.getReturnValue());
        }
    }
}
