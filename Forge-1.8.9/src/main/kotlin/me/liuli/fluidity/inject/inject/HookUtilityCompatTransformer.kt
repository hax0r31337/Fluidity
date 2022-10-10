package me.liuli.fluidity.inject.inject

import me.yuugiri.hutil.HookUtility
import me.yuugiri.hutil.obfuscation.SeargeObfuscationMap
import me.yuugiri.hutil.processor.hook.MethodHookProcessor
import me.liuli.fluidity.inject.inject.hooks.AbstractHookProvider
import me.liuli.fluidity.inject.inject.processor.OptimizeProcessor
import me.liuli.fluidity.util.other.resolveInstances
import me.yuugiri.hutil.processor.AccessProcessor
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.util.zip.GZIPInputStream

class HookUtilityCompatTransformer : IClassTransformer {

    init {
        initHooks() // initialize hook utility before got pushed into transformer list
    }

    override fun transform(name: String, transformedName: String?, data: ByteArray): ByteArray {
        if (name.startsWith("kotlin")) return data

        // read into ClassNode
        val classReader = ClassReader(data)
        val classNode = ClassNode()
        classReader.accept(classNode, 0)

        if (!hook.dealWithClassNode(classNode)) {
            return data // save performance when no changes has apply to the class
        }

        // write back to bytecode form
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        classNode.accept(classWriter)
        return classWriter.toByteArray()
    }

    companion object {
        val hook = HookUtility()
        private var hookInitialized = false

        private fun initHooks() {
            if (hookInitialized) return
            val time = System.nanoTime()

            // load obfuscation map
            hook.obfuscationMap = SeargeObfuscationMap(GZIPInputStream(HookUtilityCompatTransformer::class.java.getResourceAsStream("/1.8.9-mcp.srg.gz")).bufferedReader(Charsets.UTF_8))

            // load access transformer
            val accessRecords: Int
            hook.processorList.add(AccessProcessor.fromFMLAccessTransformer(HookUtilityCompatTransformer::class.java.getResourceAsStream("/fluidity_at.cfg")!!.bufferedReader(Charsets.UTF_8)).also { accessRecords = it.records.size })

            // load custom transformers
            hook.processorList.add(OptimizeProcessor())

            // load hooks
            var hookApplied = 0
            resolveInstances("${AbstractHookProvider::class.java.`package`.name}.impl", AbstractHookProvider::class.java)
                .forEach {
                    it.applyHook(MethodHookProcessor)
                    hookApplied++
                }
            hook.processorList.add(MethodHookProcessor)

            hookInitialized = true
            println("HookUtility initialized with [${hookApplied} hooks, $accessRecords access records] in ${((System.nanoTime() - time) / 1_000_00) / 10f} ms")
        }
    }
}