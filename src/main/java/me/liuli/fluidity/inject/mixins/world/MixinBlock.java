package me.liuli.fluidity.inject.mixins.world;

import me.liuli.fluidity.module.modules.misc.BetterButton;
import me.liuli.fluidity.module.modules.world.FastMine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class MixinBlock {

    @Shadow protected double minX;

    @Shadow protected double minY;

    @Shadow protected double minZ;

    @Shadow protected double maxX;

    @Shadow protected double maxY;

    @Shadow protected double maxZ;

    @Inject(method = "setBlockBounds", at = @At("RETURN"), cancellable = true)
    public void setBlockBounds(float p_setBlockBounds_1_, float p_setBlockBounds_2_, float p_setBlockBounds_3_, float p_setBlockBounds_4_, float p_setBlockBounds_5_, float p_setBlockBounds_6_, CallbackInfo ci) {
        if (BetterButton.INSTANCE.getState() && ((Object)this instanceof BlockButton || (Object)this instanceof BlockLever)) {
            this.minX = 0.0;
            this.minY = 0.0;
            this.minZ = 0.0;
            this.maxX = 1.0;
            this.maxY = 1.0;
            this.maxZ = 1.0;
            ci.cancel();
        }
    }


    @Inject(method = "getPlayerRelativeBlockHardness", at = @At("RETURN"), cancellable = true)
    public void getPlayerRelativeBlockHardness(EntityPlayer p_getPlayerRelativeBlockHardness_1_, World p_getPlayerRelativeBlockHardness_2_, BlockPos p_getPlayerRelativeBlockHardness_3_, CallbackInfoReturnable<Float> cir) {
        if (FastMine.INSTANCE.getState()) {
            cir.setReturnValue(FastMine.INSTANCE.getMultiplier().get() * cir.getReturnValue());
        }
    }
}
