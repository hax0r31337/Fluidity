package remap

import org.objectweb.asm.tree.ClassNode
import utils.toClassNode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream

class ClassStateManager() {

    private val classes = mutableMapOf<String, ClassNode>()
    private val superclassCache = mutableMapOf<String, List<String>>()

    constructor(jarIn: File) : this() {
        val jis = ZipInputStream(ByteArrayInputStream(jarIn.readBytes()))

        val buffer = ByteArray(1024)
        while (true) {
            val entry = jis.nextEntry ?: break
            if (entry.isDirectory || !entry.name.endsWith(".class")) continue
            val bos = ByteArrayOutputStream()
            var n: Int
            while (jis.read(buffer).also { n = it } != -1) {
                bos.write(buffer, 0, n)
            }
            bos.close()
            addClass(bos.toByteArray())
        }

        jis.close()
    }

    fun addClass(classNode: ClassNode) {
        classes[classNode.name] = classNode
    }

    fun addClass(byteArray: ByteArray) {
        addClass(toClassNode(byteArray))
    }

    fun searchSuperClasses(name: String): List<String> {
        superclassCache[name]?.let {
            return it
        }
        val list = mutableListOf<String>()

        classes.values.forEach {
            if (it.superName == name) {
                list.add(it.name)
            }
        }

        list.filter { it != "java/lang/Object" }.forEach {
            list.addAll(searchSuperClasses(it))
        }

        return list.also {
            superclassCache[name] = it
        }
    }

    fun clearCache() {
        superclassCache.clear()
    }

    fun clear() {
        clearCache()
        classes.clear()
    }
}