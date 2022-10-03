package utils

import java.io.Reader

fun readCsvMapping(mapping: Reader): Map<String, String> {
    val export = mutableMapOf<String, String>()
    mapping.readLines().forEach {
        val args = it.split(",")
        if (args[0] == "searge" && args.size >= 2) return@forEach
        export[args[0]] = args[1]
    }
    return export
}