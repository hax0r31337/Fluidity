/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.util.client.ClientUtilsKt;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;

/**
 * values() will cause performance issues, so we store them in a static array.
 * We use ASM to replace values() with our own array. [net.ccbluex.liquidbounce.injection.transformers.OptimizeTransformer]
 * https://stackoverflow.com/questions/2446135/is-there-a-performance-hit-when-using-enum-values-vs-string-arrays
 *
 * in my tests, this is 10 times faster than using values()
 * I access them 1145141919 times and save EnumFacing.name into a local variable in my test
 * EnumFacings.values() cost 122 ms
 * StaticStorage.facings() cost 15 ms
 *
 * @author liulihaocai
 */
public class StaticStorage {
    public static final EnumFacing[] facings = EnumFacing.values();
    public static final EnumChatFormatting[] chatFormatting = EnumChatFormatting.values();
    public static final EnumParticleTypes[] particleTypes = EnumParticleTypes.values();
    public static final EnumWorldBlockLayer[] worldBlockLayers = EnumWorldBlockLayer.values();

    public static void setTitle(String newTitle) {
        ClientUtilsKt.setTitle(Fluidity.INSTANCE.getHasLoaded() ? "HaveFun" : "InitializeGame");
    }
}
