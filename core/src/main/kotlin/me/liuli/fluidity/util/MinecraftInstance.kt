package me.liuli.fluidity.util

import net.minecraft.client.Minecraft

inline val mc: Minecraft
    get() = Minecraft.getMinecraft()