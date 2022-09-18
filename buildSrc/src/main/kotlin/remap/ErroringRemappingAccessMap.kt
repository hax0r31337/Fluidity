package remap

import com.google.common.base.Joiner
import net.md_5.specialsource.AccessMap
import java.io.BufferedReader
import java.io.File


class ErroringRemappingAccessMap(renameCsvs: Array<File>) : AccessMap() {
    private val renames = mutableMapOf<String, String>()
    val brokenLines = mutableMapOf<String, String>()

    init {
        for (f in renameCsvs) {
            f.readLines(Charsets.UTF_8).forEach { line ->
                val pts = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if ("searge" != pts[0]) {
                    renames[pts[0]] = pts[1]
                }
            }
        }
    }

    override fun loadAccessTransformer(file: File) {
        val reader = BufferedReader(file.reader(Charsets.UTF_8))
        loadAccessTransformer(reader)
        reader.close()
    }

    override fun addAccessChange(symbolString: String, accessString: String) {
        val pts = symbolString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (pts.size >= 2) {
            val idx = pts[1].indexOf('(')
            var start = pts[1]
            var end = ""
            if (idx != -1) {
                start = pts[1].substring(0, idx)
                end = pts[1].substring(idx)
            }
            val rename = renames[start]
            if (rename != null) {
                pts[1] = rename + end
            }
        }
        val joinedString = Joiner.on('.').join(pts)
        super.addAccessChange(joinedString, accessString)
        // convert  package.class  to  package/class
        brokenLines[joinedString.replace('.', '/')] = symbolString
    }

    override fun accessApplied(key: String, oldAccess: Int, newAccess: Int) {
        brokenLines.remove(key.replace(" ", ""))
    }
}