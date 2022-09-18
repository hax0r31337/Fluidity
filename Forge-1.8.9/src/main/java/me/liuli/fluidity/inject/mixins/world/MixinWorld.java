package me.liuli.fluidity.inject.mixins.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow public static double MAX_ENTITY_RADIUS;

    @Shadow protected abstract boolean isChunkLoaded(int p_isChunkLoaded_1_, int p_isChunkLoaded_2_, boolean p_isChunkLoaded_3_);

    @Shadow public abstract Chunk getChunkFromChunkCoords(int p_getChunkFromChunkCoords_1_, int p_getChunkFromChunkCoords_2_);

    @Overwrite
    public List<Entity> getEntitiesInAABBexcluding(Entity p_getEntitiesInAABBexcluding_1_, AxisAlignedBB p_getEntitiesInAABBexcluding_2_, Predicate<? super Entity> p_getEntitiesInAABBexcluding_3_) {
        List<Entity> list = Lists.newArrayList();
        int i = MathHelper.floor_double((p_getEntitiesInAABBexcluding_2_.minX - MAX_ENTITY_RADIUS) / 16.0);
        int j = MathHelper.floor_double((p_getEntitiesInAABBexcluding_2_.maxX + MAX_ENTITY_RADIUS) / 16.0);
        int k = MathHelper.floor_double((p_getEntitiesInAABBexcluding_2_.minZ - MAX_ENTITY_RADIUS) / 16.0);
        int l = MathHelper.floor_double((p_getEntitiesInAABBexcluding_2_.maxZ + MAX_ENTITY_RADIUS) / 16.0);

        if (i == Integer.MAX_VALUE || i == Integer.MIN_VALUE
                || j == Integer.MAX_VALUE || j == Integer.MIN_VALUE
                || k == Integer.MAX_VALUE || k == Integer.MIN_VALUE
                || l == Integer.MAX_VALUE || l == Integer.MIN_VALUE) return list;

        for(int i1 = i; i1 <= j; ++i1) {
            for(int j1 = k; j1 <= l; ++j1) {
                if (this.isChunkLoaded(i1, j1, true)) {
                    this.getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(p_getEntitiesInAABBexcluding_1_, p_getEntitiesInAABBexcluding_2_, list, p_getEntitiesInAABBexcluding_3_);
                }
            }
        }

        return list;
    }
}
