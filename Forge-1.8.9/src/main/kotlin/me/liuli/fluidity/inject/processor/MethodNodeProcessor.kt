package me.liuli.fluidity.inject.processor

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import me.yuugiri.hutil.processor.hook.AbstractHookTarget
import me.yuugiri.hutil.processor.hook.HookTargetImpl
import me.yuugiri.hutil.util.methods_
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.util.concurrent.atomic.AtomicBoolean

class MethodNodeProcessor : IClassProcessor {

    val processorList = mutableListOf<ProcessorInfo>()

    override fun selectClass(name: String) = processorList.any { it.target.classMatches(name) }

    override fun processClass(obfuscationMap: AbstractObfuscationMap?, map: AbstractObfuscationMap.ClassObfuscationRecord, klass: ClassNode): Boolean {
        var name = map.name
        val selectedRecords = run {
            processorList.filter { it.target.classMatches(name) }.let {
                it.ifEmpty {
                    name = map.obfuscatedName
                    processorList.filter { it.target.classMatches(name) }
                }
            }
        }.also { if (it.isEmpty()) return false }.map { it to AtomicBoolean(false) }

        var changed = false

        klass.methods_.forEach { method ->
            val obf = AbstractObfuscationMap.methodObfuscationRecord(obfuscationMap, klass.name, method)
            var processors = selectedRecords.filter { it.first.target.methodMatches(obf.name, obf.description) }
            if (processors.isEmpty() && obf.name != method.name) {
                processors = selectedRecords.filter { it.first.target.methodMatches(method.name, method.desc) }
            }
            processors.forEach { p ->
                p.second.set(true)
                changed = p.first.callback(obfuscationMap, method) || changed
            }
        }

        if (selectedRecords.any { !it.second.get() }) {
            throw IllegalStateException("some processors not applied (class=${klass.name}, proc=${selectedRecords.count { !it.second.get() }})")
        }

        return changed
    }

    class ProcessorInfo(val target: AbstractHookTarget, val callback: (obfuscationMap: AbstractObfuscationMap?, method: MethodNode) -> Boolean)
}