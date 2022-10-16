/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

class FMLLoadingPlugin : IFMLLoadingPlugin {

    override fun getASMTransformerClass(): Array<String> {
        return arrayOf(HookUtilityCompatTransformer::class.java.name)
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(map: Map<String, Any>) {}

    override fun getAccessTransformerClass(): String? {
        return null
    }
}