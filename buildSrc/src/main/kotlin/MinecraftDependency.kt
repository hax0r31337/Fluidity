import minecraft.MinecraftVersion
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import remap.ErroringRemappingAccessMap
import remap.Remapper
import utils.cacheDir
import utils.minecraftJar
import utils.resourceCached
import java.io.File

fun minecraftDep(project: Project, vararg accessTransformer: String): String {
    val minecraftJar = minecraftJar(project)
    if (!minecraftJar.exists()) {
        val cacheDir = cacheDir(project)
        val version = MinecraftVersion(resourceCached(cacheDir, "versions/1.8.9.json", "https://launchermeta.mojang.com/v1/packages/d546f1707a3f2b7d034eece5ea2e311eda875787/1.8.9.json"))
        val srgMapping = resourceCached(cacheDir, "1.8.9.srg", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/joined.srg")
        val fieldsCsvMapping = resourceCached(cacheDir, "fields-22.csv", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/fields.csv")
        val methodsCsvMapping = resourceCached(cacheDir, "methods-22.csv", "https://ayanoyuugiri.github.io/resources/srg/1.8.9/methods.csv")
        val accessMap = ErroringRemappingAccessMap(arrayOf(fieldsCsvMapping, methodsCsvMapping))
        accessTransformer.forEach { accessMap.loadAccessTransformer(File(project.rootDir, it)) }
        Remapper()
            .applyWarppedSrg(srgMapping.reader(Charsets.UTF_8), fieldsCsvMapping.reader(Charsets.UTF_8), methodsCsvMapping.reader(Charsets.UTF_8))
            .withAccessMap(accessMap)
            .remapJars(version.getJars(cacheDir), minecraftJar)
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