/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.util

import net.minecraft.client.Minecraft

inline val mc: Minecraft
    get() = Minecraft.getMinecraft()