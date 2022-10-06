import asm.ClassDump
import minecraft.MinecraftAssetsDownloader
import minecraft.MinecraftVersion
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import utils.cacheDir
import utils.minecraftJar
import utils.resourceCached
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


open class TaskGenIntelliJRuns : DefaultTask() {

    @get:Input
    open var vmParams = ""

    @get:Input
    open var programParams = ""

    @TaskAction
    fun execute() {
        val cacheDir = cacheDir(project)
        val nativesDir = File(cacheDir, "natives/1.8.9")
        val assetsDir = File(cacheDir, "assets")

        // get IntelliJ run config file
        val file = run {
            val root = project.rootDir.canonicalFile.parentFile
            var iter = project.projectDir.canonicalFile
            var f: File? = null
            while (iter != root) {
                f = File(iter, ".idea/workspace.xml")
                if (!f.exists()) {
                    // find iws file
                    iter.listFiles()?.also { f = null }?.forEach {
                        if (it.isFile && it.name.endsWith(".iws")) {
                            f = it
                        }
                    }
                }

                iter = iter.parentFile
            }
            f ?: throw IllegalStateException("no run configuration found")
        }

        println("run configuration detected: ${file.absolutePath}")

        val version = MinecraftVersion(resourceCached(cacheDir, "versions/1.8.9-forge1.8.9-11.15.1.2318-1.8.9.json", "https://ayanoyuugiri.github.io/resources/minecraft/1.8.9-forge1.8.9-11.15.1.2318-1.8.9.json"))
        version.getJars(cacheDir) // download libraries
        version.extractJars(cacheDir, nativesDir) // extract natives
        val assets = MinecraftAssetsDownloader(version.getAssetsVersion(), version.getAssetsUrl(), assetsDir)
        assets.getAssets() // download assets

        // prepare for hot reobfuscate
        val agentJar = File(cacheDir, "agent/agent.jar").also {
            if (!it.exists()) {
                it.parentFile.mkdirs()
                genAgentJar(it)
            }
        }
        val mainJar = File(cacheDir, "agent/main.jar").also {
            if (!it.exists()) {
                genMainJar(it, version.getMainClass(), agentJar)
            }
        }


//         inject run configuration into config file
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val document = docBuilder.parse(file)

        val gameDir = File(project.rootDir, "run").apply { mkdirs() }
        val args = version.getArguments(mapOf("auth_player_name" to "FluidityUser",
            "auth_uuid" to UUID.randomUUID().toString(),
            "auth_access_token" to "NULL",
            "version_name" to version.getVersion(),
            "game_directory" to gameDir.canonicalPath,
            "assets_root" to assetsDir.canonicalPath,
            "assets_index_name" to version.getAssetsVersion(),
            "user_properties" to "{}",
            "user_type" to "LEGACY"))
        document.injectRunConfiguration("RunFluidity (${getProjectName()})", "me.yuugiri.agent.AgentLoader", "-Djava.library.path=\"${nativesDir.canonicalPath}\" $vmParams",
            "$args $programParams", gameDir, version.getJars(cacheDir).toMutableList().also { it.add(mainJar) }, listOf(minecraftJar(project)))

        // write the content into xml file
        val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val source = DOMSource(document)
        val result = StreamResult(file)
        transformer.transform(source, result)
    }

    private fun genMainJar(jarFile: File, targetMain: String, agentJar: File) {
        val zos = ZipOutputStream(FileOutputStream(jarFile))

        zos.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
        zos.write("Manifest-Version: 1.0".toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        zos.putNextEntry(ZipEntry("me/yuugiri/agent/AgentLoader.class"))
        zos.write(ClassDump.dump(targetMain.replace('.', '/'), agentJar.canonicalPath))
        zos.closeEntry()

        zos.close()
    }

    private fun genAgentJar(jarFile: File) {
        val zos = ZipOutputStream(FileOutputStream(jarFile))

        zos.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
        zos.write("""
Manifest-Version: 1.0
Agent-Class: me.yuugiri.agent.AgentTransformer
        """.trimIndent().toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        // generate remap mapping
        val cacheDir = cacheDir(project)
        val mapping = File(cacheDir, "1.8.9-remap.dat")
        if (!mapping.exists()) generateReobfuscateMapping(project)

        zos.putNextEntry(ZipEntry("me/yuugiri/agent/AgentTransformer.class"))
        zos.write(ClassDump.dumpTransformer(mapping.canonicalPath))
        zos.closeEntry()

        zos.close()
    }

    private fun Document.injectRunConfiguration(configName: String, mainClassName: String, vmParams: String, programParams: String, runDir: File,
                                       classpathIncludes: List<File> = emptyList(), classpathExcludes: List<File> = emptyList()) {
        val runMngr = run {
            val list = this.getElementsByTagName("component")
            for (i in 0 until list.length) {
                val element = list.item(i) as Element
                if (element.getAttribute("name") == "RunManager") {
                    return@run element
                }
            }

            val project = this.getElementsByTagName("project").item(0)
            return@run project.add("component", mapOf("name" to "RunManager"))
        }
        runMngr.setAttribute("selected", "Application.$configName")
        val name = getProjectName()
        val config = runMngr.add("configuration", mapOf("name" to configName,
            "type" to "Application", "factoryName" to "Application", "default" to "false"))

        config.add("extension", mapOf("name" to "coverage", "enabled" to "false",
            "sample_coverage" to "true", "runner" to "idea"))
        config.add("module", mapOf("name" to "$name.main"))
        config.add("RunnerSettings", mapOf("RunnerId" to "Run"))
        config.add("ConfigurationWrapper", mapOf("RunnerId" to "Run"))
        config.add("option", mapOf("name" to "MAIN_CLASS_NAME", "value" to mainClassName))
        config.add("option", mapOf("name" to "VM_PARAMETERS", "value" to vmParams))
        config.add("option", mapOf("name" to "PROGRAM_PARAMETERS", "value" to programParams))
        config.add("option", mapOf("name" to "WORKING_DIRECTORY", "value" to runDir.canonicalPath))
        config.add("option", mapOf("name" to "ALTERNATIVE_JRE_PATH_ENABLED", "value" to "false"))
        config.add("option", mapOf("name" to "ALTERNATIVE_JRE_PATH", "value" to ""))
        config.add("option", mapOf("name" to "ENABLE_SWING_INSPECTOR", "value" to "false"))
        config.add("option", mapOf("name" to "PASS_PARENT_ENVS", "value" to "true"))

        if (classpathIncludes.isNotEmpty() || classpathExcludes.isNotEmpty()) {
            val classpathMods = config.add("classpathModifications", emptyMap())
            classpathIncludes.forEach {
                classpathMods.add("entry", mapOf("path" to it.canonicalPath))
            }
            classpathExcludes.forEach {
                classpathMods.add("entry", mapOf("exclude" to "true", "path" to it.canonicalPath))
            }
        }
    }

    private fun Node.add(name: String, values: Map<String, String>): Element {
        val doc = this.ownerDocument ?: this as Document
        val ele = doc.createElement(name)
        values.forEach { (key, value) ->
            ele.setAttribute(key, value)
        }
        this.appendChild(ele)
        return ele
    }

    private fun getProjectName(project: Project = getProject()): String {
        val parent = project.parent
        return if (project == project.rootProject || parent == null) {
            project.name
        } else {
            "${getProjectName(parent)}.${project.name}"
        }
    }
}