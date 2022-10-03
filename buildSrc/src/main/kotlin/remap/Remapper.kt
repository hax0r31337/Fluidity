package remap

import net.md_5.specialsource.*
import utils.readCsvMapping
import java.io.BufferedReader
import java.io.File
import java.io.Reader

class Remapper {

    /**
     * original, mapped
     */
    val classMap = mutableMapOf<String, String>()
    val fieldMap = mutableListOf<FieldRecord>()
    val methodMap = mutableListOf<MethodRecord>()
    private var accessMap: AccessMap? = null

    fun applySrg(mapping: Reader): Remapper {
        mapping.readLines().forEach {
            val args = it.split(" ")
            when(args[0]) {
                "CL:" -> { // class
                    classMap[args[1]] = args[2]
                }
                "FD:" -> {
                    val name = args[1]
                    val srg = args[2]
                    fieldMap.add(FieldRecord(name.substring(0, name.lastIndexOf('/')), srg.substring(0, srg.lastIndexOf('/')),
                        name.substring(name.lastIndexOf('/')+1), srg.substring(srg.lastIndexOf('/')+1)))
                }
                "MD:" -> {
                    val name = args[1]
                    val srg = args[3]
                    methodMap.add(MethodRecord(name.substring(0, name.lastIndexOf('/')), srg.substring(0, srg.lastIndexOf('/')),
                        name.substring(name.lastIndexOf('/')+1), srg.substring(srg.lastIndexOf('/')+1), args[2], args[4]))
                }
            }
        }
        return this
    }

    fun applyWarppedSrg(mapping: Reader, fieldsMapping: Reader, methodsMapping: Reader): Remapper {
        val fields = readCsvMapping(fieldsMapping)
        val methods = readCsvMapping(methodsMapping)
        mapping.readLines().forEach {
            val args = it.split(" ")
            when(args[0]) {
                "CL:" -> { // class
                    classMap[args[1]] = args[2]
                }
                "FD:" -> {
                    val name = args[1]
                    val srg = args[2]
                    fieldMap.add(FieldRecord(name.substring(0, name.lastIndexOf('/')), srg.substring(0, srg.lastIndexOf('/')),
                        name.substring(name.lastIndexOf('/')+1), srg.substring(srg.lastIndexOf('/')+1).let { fields[it] ?: it }))
                }
                "MD:" -> {
                    val name = args[1]
                    val srg = args[3]
                    methodMap.add(MethodRecord(name.substring(0, name.lastIndexOf('/')), srg.substring(0, srg.lastIndexOf('/')),
                        name.substring(name.lastIndexOf('/')+1), srg.substring(srg.lastIndexOf('/')+1).let { methods[it] ?: it }, args[2], args[4]))
                }
            }
        }
        return this
    }

    fun remapJars(jars: List<File>, output: File): Remapper {
        val mapping = JarMapping().also {
            val sb = StringBuilder()
            classMap.forEach {
                sb.append("CL: ${it.key} ${it.value}\n")
            }
            fieldMap.forEach {
                sb.append(it.toString())
                sb.append("\n")
            }
            methodMap.forEach {
                sb.append(it.toString())
                sb.append("\n")
            }
            sb.setLength(sb.length - 1)
            it.loadMappings(BufferedReader(sb.toString().reader()), null, null, false)
        }
        val remapper = JarRemapper(RemapperProcessor(null, mapping, null), mapping, accessMap?.let { RemapperProcessor(null, null, it) })
        val jar = Jar.init(jars)
        remapper.remapJar(jar, output)
        return this
    }

    fun withAccessMap(accessMap: AccessMap): Remapper {
        this.accessMap = accessMap
        return this
    }

    data class FieldRecord(var klass: String, var mappedClass: String, var name: String, var mappedName: String) {
        override fun toString() = "FD: ${klass}/${name} ${mappedClass}/${mappedName}"
    }

    data class MethodRecord(var klass: String, var mappedClass: String, var name: String, var mappedName: String, var desc: String, var mappedDesc: String) {
        override fun toString() = "MD: ${klass}/${name} ${desc} ${mappedClass}/${mappedName} ${mappedDesc}"
    }
}