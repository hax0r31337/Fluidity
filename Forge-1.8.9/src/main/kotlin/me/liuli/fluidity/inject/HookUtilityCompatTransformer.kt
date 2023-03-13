/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject

import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.processor.MethodNodeProcessor
import me.liuli.fluidity.inject.processor.OptimizeProcessor
import me.liuli.fluidity.util.other.resolveInstances
import me.yuugiri.hutil.HookUtility
import me.yuugiri.hutil.obfuscation.SeargeObfuscationMap
import me.yuugiri.hutil.processor.AccessProcessor
import me.yuugiri.hutil.processor.hook.MethodHookProcessor
import net.minecraft.launchwrapper.IClassTransformer
import java.util.zip.GZIPInputStream

class HookUtilityCompatTransformer : IClassTransformer {

    init {
        initHooks() // initialize hook utility before got pushed into transformer list
    }

    override fun transform(name: String?, transformedName: String?, data: ByteArray?): ByteArray? {
        name ?: return data
        data ?: return null
        if (name.startsWith("kotlin") || name.startsWith("me.yuugiri") || name.startsWith("org.spongepowered")) return data

        return hook.dealWithClassData(data, (transformedName ?: name).replace('.', '/'))/*.also {
            File("dump/$name.class").writeBytes(it)
        }*/
    }

    companion object {
        val hook = HookUtility()
        private var hookInitialized = false

        private fun initHooks() {
            if (hookInitialized) return
            val time = System.nanoTime()

            // load obfuscation mapEnumFacing
            hook.obfuscationMap = SeargeObfuscationMap(GZIPInputStream(HookUtilityCompatTransformer::class.java.getResourceAsStream("/1.8.9-mcp.srg.gz")).bufferedReader(Charsets.UTF_8))

            // load access transformer
            val accessRecords: Int
            hook.processorList.add(AccessProcessor.fromFMLAccessTransformer(HookUtilityCompatTransformer::class.java.getResourceAsStream("/fluidity_at.cfg")!!.bufferedReader(Charsets.UTF_8)).also { accessRecords = it.records.size })

            // load custom transformers
            hook.processorList.add(OptimizeProcessor())

            // load hooks
            val mnp = MethodNodeProcessor()
            var hookApplied = 0
            resolveInstances("${HookProvider::class.java.`package`.name}.impl", HookProvider::class.java)
                .forEach {
                    it.applyHook(MethodHookProcessor, mnp)
                    hookApplied++
                }
            hook.processorList.add(MethodHookProcessor)
            if (mnp.processorList.isNotEmpty()) hook.processorList.add(mnp)

            hookInitialized = true
            println("HookUtility initialized with [${hookApplied} hooks, ${mnp.processorList.size} method processors, $accessRecords access records] in ${((System.nanoTime() - time) / 1_000_00) / 10f} ms")
        }
    }
}