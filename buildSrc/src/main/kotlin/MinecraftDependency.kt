import com.beust.klaxon.Klaxon
import com.beust.klaxon.PathMatcher
import com.google.common.collect.ImmutableMap
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import remap.ErroringRemappingAccessMap
import remap.Remapper
import utils.resourceCached
import java.io.File

private fun minecraftJar(project: Project): File {
    return File(project.rootDir, ".gradle/fluidity/repo/minecraftbin-1.8.9-${project.rootProject.name}.jar")
}

private fun cacheDir(project: Project): File {
    return File(project.gradle.gradleUserHomeDir, "caches/fluidity/")
}

fun minecraftDep(project: Project, vararg accessTransformer: String): String {
    val minecraftJar = minecraftJar(project)
    if (!minecraftJar.exists()) {
        val cacheDir = cacheDir(project)
        val version = resourceCached(cacheDir, "versions/version.json", "https://launchermeta.mojang.com/v1/packages/d546f1707a3f2b7d034eece5ea2e311eda875787/1.8.9.json")
        val os = "natives-"+System.getProperty("os.name").let {
            if (it.contains("win", true)) {
                "windows"
            } else if (it.contains("mac", true) || it.contains("darwin", true)) {
                "osx"
            } else {
                "linux"
            }
        }
        val jars = mutableListOf<File>()
        val pathMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = true

            override fun onMatch(path: String, value: Any) {
                if (value is String && (path == "\$.downloads.client.url" || (path.startsWith("\$.libraries[") && path.endsWith(".url")))) {
                    if (path.contains(".classifiers.") && !path.contains(os)) {
                        return
                    }
                    val name = value.toString().let { it.substring(it.lastIndexOf("/") + 1) }
                    jars.add(resourceCached(cacheDir, "189libs/$name", value.toString()))
                }
            }
        }
        Klaxon()
            .pathMatcher(pathMatcher)
            .parseJsonObject(version.reader(Charsets.UTF_8))
        val srgMapping = resourceCached(cacheDir, "1.8.9.srg", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/joined.srg")
        val fieldsCsvMapping = resourceCached(cacheDir, "fields-22.csv", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/fields.csv")
        val methodsCsvMapping = resourceCached(cacheDir, "methods-22.csv", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/methods.csv")
        val accessMap = ErroringRemappingAccessMap(arrayOf(fieldsCsvMapping, methodsCsvMapping))
        accessTransformer.forEach { accessMap.loadAccessTransformer(File(project.rootDir, it)) }
        Remapper()
            .applyWarppedSrg(srgMapping.reader(Charsets.UTF_8), fieldsCsvMapping.reader(Charsets.UTF_8), methodsCsvMapping.reader(Charsets.UTF_8))
            .withAccessMap(accessMap)
            .remapJars(jars, minecraftJar)
    }
    project.rootProject.allprojects {
        repositories.flatDir {
            name = "ReplicaMcRepo"
            dir(File(project.rootDir, ".gradle/fluidity/repo"))
        }
    }
    return "me.yuugiri:minecraftbin:1.8.9-${project.rootProject.name}"
}

/**
 * used to refresh access transformer
 */
open class TaskRemoveMinecraftJarCache : DefaultTask() {

    @TaskAction
    fun execute() {
        val minecraftJar = minecraftJar(project)
        if (minecraftJar.exists()) minecraftJar.delete()
    }
}