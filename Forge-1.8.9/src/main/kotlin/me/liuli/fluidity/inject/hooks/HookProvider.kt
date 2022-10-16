/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.hooks

import me.liuli.fluidity.inject.processor.MethodNodeProcessor
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.*
import me.yuugiri.hutil.processor.hook.point.*
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Method
import java.lang.reflect.Modifier

annotation class Hook(val method: String = "", val desc: String = "*", val type: Type, val shift: EnumHookShift = EnumHookShift.BEFORE, val ordinal: Int = -1) {

    /**
     * @param type ENTER, EXIT, THROW, INVOKE, FIELD
     */
    annotation class Type(val type: String, val info: String = "", val superclass: String = "", val isInfoRegex: Boolean = false)
}

annotation class MethodProcess(val method: String = "", val desc: String = "*")

abstract class HookProvider(val className: String) {

    open fun applyHook(mhp: MethodHookProcessor, mnp: MethodNodeProcessor) {
        this.javaClass.declaredMethods.forEach { method ->
            if (method.isAnnotationPresent(Hook::class.java)) {
                val annotation = method.getDeclaredAnnotation(Hook::class.java)!!
                mhp.addHookInfo(HookInfo(HookTargetImpl(className.replace('.', '/'),
                    annotation.method.ifEmpty { method.name }, annotation.desc),
                    annotation.type.toHookPoint(), annotation.shift, annotation.ordinal, genFunctionCallback(method)))
            } else if (method.isAnnotationPresent(MethodProcess::class.java)) {
                val annotation = method.getDeclaredAnnotation(MethodProcess::class.java)!!
                mnp.processorList.add(
                    MethodNodeProcessor.ProcessorInfo(HookTargetImpl(className.replace('.', '/'),
                        annotation.method.ifEmpty { method.name }, annotation.desc), getMethodProcessCallback(method)))
            }
        }
    }

    private fun getMethodProcessCallback(method: Method): (AbstractObfuscationMap?, MethodNode) -> Boolean {
        method.isAccessible = true
        val isStatic = Modifier.isStatic(method.modifiers)
        return if (method.parameterCount == 1) {
            if (isStatic) ({ a, b -> method.invoke(null, b); true }) else ({ a, b -> method.invoke(this, b); true })
        } else if (method.parameterCount == 2) {
            if (isStatic) ({ a, b -> method.invoke(null, a, b); true }) else ({ a, b -> method.invoke(this, a, b); true })
        } else throw IllegalArgumentException("Unsupported parameter count: $method")
    }

    private fun genFunctionCallback(method: Method): (MethodHookParam) -> Unit {
        method.isAccessible = true
        val isStatic = Modifier.isStatic(method.modifiers)
        return if (method.parameterCount == 0) {
            if (isStatic) ({ try { method.invoke(null) } catch (t: Throwable) { t.printStackTrace() } })
            else ({ try { method.invoke(this) } catch (t: Throwable) { t.printStackTrace() } })
        } else if (method.parameterCount == 1) {
            if (isStatic) ({ try { method.invoke(null, it) } catch (t: Throwable) { t.printStackTrace() } })
            else ({ try { method.invoke(this, it) } catch (t: Throwable) { t.printStackTrace() } })
        } else throw IllegalArgumentException("Unsupported parameter count: $method")
    }

    private fun Hook.Type.toHookPoint(): IHookPoint {
        return when(this.type) {
            "ENTER" -> HookPointEnter()
            "EXIT" -> HookPointExit()
            "THROW" -> HookPointThrow()
            "INVOKE" -> HookPointInvoke(this.getHookMatcher(), this.superclass)
            "FIELD" -> HookPointField(this.getHookMatcher(), this.superclass)
            else -> throw IllegalArgumentException("Illegal hook point type: ${this.type}")
        }
    }

    private fun Hook.Type.getHookMatcher(): IHookPointMatcher {
        return if (this.info.isEmpty()) {
            throw IllegalArgumentException("Illegal info for invoke hook point")
        } else if (this.info == "*") {
            HookPointMatcherAll()
        } else if (this.isInfoRegex) {
            HookPointMatcherRegex(Regex(this.info))
        } else {
            HookPointMatcherRaw(this.info)
        }
    }
}