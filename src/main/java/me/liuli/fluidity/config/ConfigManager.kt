package me.liuli.fluidity.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.Listener
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.util.client.logError
import me.liuli.fluidity.util.client.logInfo
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.other.getObjectInstance
import me.liuli.fluidity.util.other.resolvePackage
import me.liuli.fluidity.util.timing.TheTimer
import java.io.*
import java.nio.charset.StandardCharsets

class ConfigManager : Listener {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val rootPath = File(mc.mcDataDir, Fluidity.NAME)
    val configPath = File(rootPath, "configs")
    val configSetFile = File(rootPath, "config.json")

    private val sections = mutableListOf<ConfigSection>()
    private val timer = TheTimer()

    var nowConfig = "default"
    var configFile = File(configPath, "$nowConfig.json")

    init {
        resolvePackage("${this.javaClass.`package`.name}.sections", ConfigSection::class.java)
            .forEach {
                try {
                    sections.add(it.newInstance())
                } catch (e: IllegalAccessException) {
                    // this module is a kotlin object
                    sections.add(getObjectInstance(it))
                } catch (e: Throwable) {
                    logError("Failed to load config section: ${it.name} (${e.javaClass.name}: ${e.message})")
                }
            }

        // 初始化文件夹
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }

        if (!configPath.exists()) {
            configPath.mkdirs()
        }
    }

    fun load(name: String) {
        if (nowConfig != name) {
            save() // 保存老配置
        }

        nowConfig = name
        configFile = File(configPath, "$nowConfig.json")

        val json = if (configFile.exists()) {
            JsonParser().parse(BufferedReader(FileReader(configFile))).asJsonObject
        } else {
            JsonObject() // 这样方便一点,虽然效率会低
        }

        for (section in sections) {
            section.load(if (json.has(section.sectionName)) { json.getAsJsonObject(section.sectionName) } else { null })
        }

        if (!configFile.exists()) {
            save()
        }

        saveConfigSet()

        logInfo("Config $nowConfig.json loaded.")
    }

    fun reload() {
        load(nowConfig)
    }

    fun save() {
        val config = JsonObject()

        for (section in sections) {
            config.add(section.sectionName, section.save())
        }

        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(configFile), StandardCharsets.UTF_8))
        writer.write(gson.toJson(config))
        writer.close()

        saveConfigSet()

        logInfo("Config $nowConfig.json saved.")
    }

    fun loadDefault() {
        val configSet = if (configSetFile.exists()) { JsonParser().parse(BufferedReader(FileReader(configSetFile))).asJsonObject } else { JsonObject() }

        antiForge = if (configSet.has("anti-forge")) {
            configSet.get("anti-forge").asBoolean
        } else {
            false
        }
        load(if (configSet.has("file")) {
            configSet.get("file").asString
        } else {
            "default"
        })
    }

    fun saveConfigSet() {
        val configSet = JsonObject()

        configSet.addProperty("file", nowConfig)
        configSet.addProperty("anti-forge", antiForge)

        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(configSetFile), StandardCharsets.UTF_8))
        writer.write(gson.toJson(configSet))
        writer.close()
    }

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        if (timer.hasTimePassed(60 * 1000L)) { // save it every minute
            logInfo("Auto-saving $nowConfig.json")
            timer.reset()
            save()
        }
    }

    override fun listen() = true

    companion object {
        var antiForge = false
    }
}