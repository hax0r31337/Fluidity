package me.liuli.fluidity.inject.inject

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