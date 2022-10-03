package utils

import org.gradle.api.Project
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

private val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:100.0) Gecko/20100101 Firefox/100.0"

fun resourceCached(cacheDir: File, path: String, url: String): File {
    val file = File(cacheDir, path)
    if (!file.exists()) {
        file.parentFile.mkdirs()
        println("Downloading resource ${file.absolutePath} from ${url}")
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 2000
        conn.readTimeout = 10000
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.instanceFollowRedirects = true
        conn.doOutput = true

        Files.copy(conn.inputStream, file.toPath())
    }

    return file
}

fun minecraftJar(project: Project): File {
    return File(project.rootDir, ".gradle/fluidity/repo/minecraftbin-1.8.9-${project.rootProject.name}.jar")
}

fun cacheDir(project: Project): File {
    return File(project.gradle.gradleUserHomeDir, "caches/fluidity/")
}