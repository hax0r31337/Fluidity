package me.liuli.fluidity.inject;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.module.modules.combat.Reach;
import me.liuli.fluidity.util.client.ClientUtilsKt;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

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
    private static final EnumFacing[] facings = EnumFacing.values();
    private static final EnumChatFormatting[] chatFormatting = EnumChatFormatting.values();
    private static final EnumParticleTypes[] particleTypes = EnumParticleTypes.values();
    private static final EnumWorldBlockLayer[] worldBlockLayers = EnumWorldBlockLayer.values();

    public static EnumFacing[] facings() {
        return facings;
    }

    public static EnumChatFormatting[] chatFormatting() {
        return chatFormatting;
    }

    public static EnumParticleTypes[] particleTypes() {
        return particleTypes;
    }

    public static EnumWorldBlockLayer[] worldBlockLayers() {
        return worldBlockLayers;
    }

    public static void dummy() {}

    public static void setTitle(String newTitle) {
        System.out.println("attempt set title: " + newTitle);
        ClientUtilsKt.setTitle(Fluidity.INSTANCE.getHasLoaded() ? "HaveFun" : "InitializeGame");
    }
}
