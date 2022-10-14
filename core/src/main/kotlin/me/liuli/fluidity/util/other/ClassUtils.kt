package me.liuli.fluidity.util.other

import com.google.common.collect.ImmutableSet
import com.google.common.reflect.ClassPath
import com.google.gson.JsonParser
import java.lang.reflect.Modifier

val jsonParser = JsonParser()

private val allClassesMap = mutableMapOf<ClassLoader, ImmutableSet<ClassPath.ClassInfo>>()

/**
 * scan classes with specified superclass like what Reflections do
 */
fun <T : Any> resolvePackage(packagePath: String, klass: Class<T>): List<Class<out T>> {
    val cl = klass.classLoader

    // use a list to cache classes
    val list = mutableListOf<Class<out T>>()

    (allClassesMap[cl] ?: run {
        val all = ClassPath.from(cl).allClasses
        allClassesMap[cl] = all
        all
    }).filter { it.packageName.startsWith(packagePath) }
      .map { cl.loadClass(it.name) }
      .forEach { resolved ->
          // check if class is assignable from target class
          if(klass.isAssignableFrom(resolved) && !resolved.isInterface && !Modifier.isAbstract(resolved.modifiers)) {
              // add to list
              list.add(resolved as Class<out T>)
          }
      }

    return list
}

fun <T : Any> resolveInstances(packagePath: String, klass: Class<T>): List<T> {
    return resolvePackage(packagePath, klass).map {
        try {
            it.newInstance()
        } catch (e: IllegalAccessException) {
            getObjectInstance(it)
        }
    }
}

fun <T> getObjectInstance(clazz: Class<T>): T {
    clazz.declaredFields.forEach {
        if (it.name.equals("INSTANCE")) {
            return it.get(null) as T
        }
    }
    throw IllegalAccessException("This class not a kotlin object")
}

val Class<*>.asmName: String
    get() = this.name.replace('.', '/')