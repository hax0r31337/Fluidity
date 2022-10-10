package me.liuli.fluidity.inject.inject.hooks

import me.yuugiri.hutil.processor.hook.*
import me.yuugiri.hutil.processor.hook.point.*
import java.lang.reflect.Method
import java.lang.reflect.Modifier

annotation class Hook(val method: String = "", val desc: String = "*", val type: Type, val shift: EnumHookShift = EnumHookShift.BEFORE, val ordinal: Int = -1) {

    /**
     * @param type ENTER, EXIT, THROW, INVOKE, FIELD
     */
    annotation class Type(val type: String, val info: String = "", val isInfoRegex: Boolean = false)
}

abstract class AbstractHookProvider(val className: String) {

    open fun applyHook(mhp: MethodHookProcessor) {
        this.javaClass.declaredMethods.forEach { method ->
            if (!method.isAnnotationPresent(Hook::class.java)) return@forEach
            val annotation = method.getDeclaredAnnotation(Hook::class.java)!!
            mhp.addHookInfo(HookInfo(HookTargetImpl(className.replace('.', '/'),
                annotation.method.ifEmpty { method.name }, annotation.desc),
                annotation.type.toHookPoint(), annotation.shift, annotation.ordinal, genFunctionCallback(method)))
        }
    }

    private fun genFunctionCallback(method: Method): (MethodHookParam) -> Unit {
        method.isAccessible = true
        val isStatic = Modifier.isStatic(method.modifiers)
        return if (method.parameterCount == 0) {
            if (isStatic) ({ method.invoke(null) }) else ({ method.invoke(this) })
        } else if (method.parameterCount == 1) {
            if (isStatic) ({ method.invoke(null, it) }) else ({ method.invoke(this, it) })
        } else throw IllegalArgumentException("Unsupported parameter count: $method")
    }

    private fun Hook.Type.toHookPoint(): IHookPoint {
        return when(this.type) {
            "ENTER" -> HookPointEnter()
            "EXIT" -> HookPointExit()
            "THROW" -> HookPointThrow()
            "INVOKE" -> HookPointInvoke(this.getHookMatcher())
            "FIELD" -> HookPointField(this.getHookMatcher())
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