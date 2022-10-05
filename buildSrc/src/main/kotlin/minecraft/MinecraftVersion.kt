package minecraft

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import utils.resourceCached
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class MinecraftVersion(versionJson: File) {

    val json = JsonParser().parse(versionJson.reader(Charsets.UTF_8)).asJsonObject

    fun getClientUrl() = json.getAsJsonObject("downloads").getAsJsonObject("client").get("url").asString

    fun getAssetsUrl() = json.getAsJsonObject("assetIndex").get("url").asString

    fun getAssetsVersion() = json.get("assets").asString

    fun getVersion() = json.get("id").asString

    fun getMainClass() = json.get("mainClass").asString

    fun getArguments(replacements: Map<String, String>) = json.get("minecraftArguments").asString.let {
        var rep = it
        replacements.forEach { (key, value) ->
            rep = rep.replace("\${$key}", value)
        }
        rep
    }

    fun getLibraries(platform: String = MinecraftVersion.platform): List<Library> {
        val librariesArray = json.getAsJsonArray("libraries")
        val librariesOut = mutableListOf<Library>()

        librariesArray.forEach {
            val lib = it.asJsonObject
            if (lib.has("rules")) {
                var allow = false
                lib.getAsJsonArray("rules").forEach {
                    val rule = it.asJsonObject
                    if (!rule.has("os") || rule.getAsJsonObject("os").get("name").asString == platform) {
                        allow = rule.get("action").asString == "allow"
                    }
                }
                if (!allow) return@forEach
            }
            val files = mutableListOf<HostedFile>()
            val downloads = lib.getAsJsonObject("downloads")
            if (downloads.has("artifact")) {
                files.add(HostedFile(downloads.getAsJsonObject("artifact")))
            }
            if (downloads.has("classifiers")) {
                val classifiers = downloads.getAsJsonObject("classifiers")
                if (classifiers.has("natives-$platform")) {
                    files.add(HostedFile(classifiers.getAsJsonObject("natives-$platform")))
                }
            }
            if (files.isEmpty()) return@forEach
            if (lib.has("extract")) {
                val extract = lib.getAsJsonObject("extract")
                if (extract.has("exclude")) {
                    librariesOut.add(Library(files, true, extract.getAsJsonArray("exclude").map { it.asString }))
                    return@forEach
                }
            }
            librariesOut.add(Library(files))
        }

        return librariesOut
    }

    fun getJars(cacheDir: File): List<File> {
        val jars = mutableListOf<File>()

        jars.add(resourceCached(cacheDir, "versions/1.8.9.jar", this.getClientUrl()))
        this.getLibraries().forEach { lib ->
            lib.files.forEach {
                jars.add(resourceCached(cacheDir, "libraries/${it.path}", it.url))
            }
        }

        return jars
    }

    fun extractJars(cacheDir: File, extractDir: File) {
        this.getLibraries().filter { it.needExtract && it.extractExcludes.isNotEmpty() }.forEach { lib ->
            lib.files.forEach {
                extractJar(resourceCached(cacheDir, "libraries/${it.path}", it.url), extractDir, lib.extractExcludes)
            }
        }
    }

    private fun extractJar(jar: File, extractDir: File, excludeRules: List<String>) {
        val jis = ZipInputStream(FileInputStream(jar))
        val buffer = ByteArray(1024)
        while (true) {
            val entry = jis.nextEntry ?: break
            if (entry.isDirectory || excludeRules.any { entry.name.startsWith(it) }) continue
            val fos = FileOutputStream(File(extractDir, entry.name).also { it.parentFile.mkdirs() })
            var n: Int
            while (jis.read(buffer).also { n = it } != -1) {
                fos.write(buffer, 0, n)
            }
            fos.close()
        }
        jis.close()
    }

    data class HostedFile(val path: String, val sha1: String, val size: Int, val url: String) {
        constructor(obj: JsonObject) : this(obj.get("path").asString, obj.get("sha1").asString, obj.get("size").asInt, obj.get("url").asString)
    }

    class Library(val files: List<HostedFile>, val needExtract: Boolean = false, val extractExcludes: List<String> = emptyList())

    companion object {
        val platform = System.getProperty("os.name").let {
            if (it.contains("win", true)) {
                "windows"
            } else if (it.contains("mac", true) || it.contains("darwin", true)) {
                "osx"
            } else {
                "linux"
            }
        }
    }
}