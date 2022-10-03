import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import utils.toBytes
import utils.toClassNode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

open class TaskStripDebug : DefaultTask() {

    @TaskAction
    fun execute() {
        val rootDir = File(project.projectDir, "build/libs")
        rootDir.listFiles()?.forEach {
            if (it.name.endsWith(".jar")) {
                println("Patching ${it.absolutePath}...")
                patchJar(it)
            }
        }
    }
}

fun patchJar(file: File) {
    val jis = ZipInputStream(ByteArrayInputStream(file.readBytes()))
    val jos = ZipOutputStream(FileOutputStream(file))
    jos.setLevel(9)
    val buffer = ByteArray(1024)
    while (true) {
        val entry = jis.nextEntry ?: break
        if (entry.isDirectory) continue
        var body: ByteArray
        run {
            val bos = ByteArrayOutputStream()
            var n: Int
            while (jis.read(buffer).also { n = it } != -1) {
                bos.write(buffer, 0, n)
            }
            bos.close()
            body = bos.toByteArray()
        }
        if (entry.name.endsWith(".class")) {
            body = patchClass(body)
        }
        jos.putNextEntry(JarEntry(entry.name))
        jos.write(body)
        jos.closeEntry()
    }
    jos.close()
}

private fun patchClass(data: ByteArray): ByteArray {
    val klass = toClassNode(data)

    klass.methods.forEach { patchMethod(klass, it) }
    klass.sourceDebug = null
    klass.sourceFile = null

    klass.visibleAnnotations?.filterNotNull()?.forEach {
        if (it.desc.equals("Lkotlin/Metadata;")) {
            klass.visibleAnnotations.remove(it)
        }
    }

    return toBytes(klass)
}

private fun patchMethod(klass: ClassNode, method: MethodNode) {
    val inst = method.instructions.toArray()
    method.instructions.clear()
    inst.forEach {
        if (it is LineNumberNode) {
            return@forEach
        }
        method.instructions.add(it)
    }
//        val names = mutableListOf<Char>()
//        fun genChar(): Char {
//            val c = (0x4E00..0x9FFF).random().toChar()
//            return if (names.contains(c)) {
//                genChar()
//            } else {
//                names.add(c)
//                c
//            }
//        }
    method.localVariables?.forEach {
        it.name = "\n"
    }
    method.parameters?.forEach {
        it.name = "\n"
    }
}