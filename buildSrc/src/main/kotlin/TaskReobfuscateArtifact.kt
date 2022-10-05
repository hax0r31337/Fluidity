import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import remap.ClassStateManager
import utils.cacheDir
import utils.minecraftJar
import utils.readCsvMapping
import utils.resourceCached
import java.io.File


fun generateReobfuscateMapping(project: Project): Pair<Map<String, String>, Map<String, String>> {
    val cacheDir = cacheDir(project)
    val resultFields = mutableMapOf<String, String>()
    val resultMethods = mutableMapOf<String, String>()
    val mapping = File(cacheDir, "1.8.9-remap.dat")
    if (mapping.exists()) {
        mapping.readLines(Charsets.UTF_8).forEach {
            val args = it.split(" ")
            if (args.size != 3) return@forEach
            when (args[0]) {
                "F" -> resultFields[args[1]] = args[2]
                "M" -> resultMethods[args[1]] = args[2]
            }
        }
        return resultFields to resultMethods
    }

    println("Generating re-obfuscate mapping cache...")

    val srgMapping = resourceCached(cacheDir, "1.8.9.srg", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/joined.srg")
    val fieldsCsvMapping = resourceCached(cacheDir, "fields-22.csv", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/fields.csv")
    val methodsCsvMapping = resourceCached(cacheDir, "methods-22.csv", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/methods.csv")

    val fields = readCsvMapping(fieldsCsvMapping.reader(Charsets.UTF_8))
    val methods = readCsvMapping(methodsCsvMapping.reader(Charsets.UTF_8))
    val csm = ClassStateManager(minecraftJar(project).also {
        if (!it.exists()) minecraftDep(project)
    })
    srgMapping.readLines(Charsets.UTF_8).forEach {
        val args = it.split(" ")
        when (args[0]) {
            "FD:" -> {
                val fd = args[2]
                val klass = fd.substring(0, fd.lastIndexOf('/'))
                val field = fd.substring(fd.lastIndexOf('/')+1)
                csm.searchSuperClasses(klass).forEach { klass ->
                    resultFields["$klass/${fields[field]}"] = field
                }
            }
            "MD:" -> {
                val md = args[3]
                val klass = md.substring(0, md.lastIndexOf('/'))
                val method = md.substring(md.lastIndexOf('/')+1)
                csm.searchSuperClasses(klass).forEach { klass ->
                    resultMethods["$klass/${methods[method]}${args[4]}"] = method
                }
            }
        }
    }

    csm.clear()

    val sb = StringBuilder()
    resultFields.forEach { (s, s2) ->
        sb.append("F ").append(s).append(' ').append(s2).append('\n')
    }
    resultMethods.forEach { (s, s2) ->
        sb.append("M ").append(s).append(' ').append(s2).append('\n')
    }
    mapping.writeText(sb.toString(), Charsets.UTF_8)

    return resultFields to resultMethods
}

fun reobfuscateClass(klass: ClassNode, map: Pair<Map<String, String>, Map<String, String>>) {
    klass.methods.forEach {
        reobfuscateMethod(it, map)
    }
}

private fun reobfuscateMethod(method: MethodNode, map: Pair<Map<String, String>, Map<String, String>>) {
    method.instructions.forEach { insn ->
        if (insn is MethodInsnNode) {
            val id = "${insn.owner}/${insn.name}${insn.desc}"
            map.second[id]?.let {
                insn.name = it
            }
        } else if (insn is FieldInsnNode) {
            val id = "${insn.owner}/${insn.name}"
            map.first[id]?.let {
                insn.name = it
            }
        }
    }
}


open class TaskReobfuscateArtifact : TaskClassPatching() {

    @get:Internal
    val mapping by lazy { generateReobfuscateMapping(project) }

    override val patcher: (ClassNode) -> Unit = { reobfuscateClass(it, mapping) }
}