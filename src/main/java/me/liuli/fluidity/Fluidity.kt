package me.liuli.fluidity

import me.liuli.fluidity.command.CommandManager
import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.event.EventManager
import me.liuli.fluidity.gui.compose.ComposeManager
import me.liuli.fluidity.gui.compose.DependencyDownloader
import me.liuli.fluidity.module.ModuleManager
import me.liuli.fluidity.pathfinder.Pathfinder
import me.liuli.fluidity.util.client.logInfo
import me.liuli.fluidity.util.client.setTitle
import java.util.*

object Fluidity {

    private val gitInfo = Properties().also {
        val inputStream = Fluidity::class.java.classLoader.getResourceAsStream("git.properties")
        if(inputStream != null) {
            it.load(inputStream)
        } else {
            throw RuntimeException("git.properties not found")
        }
    }

    const val NAME = "Fluidity"
    const val COLORED_NAME = "§3F§bluidity"
    @JvmField
    val VERSION = gitInfo["git.commit.id.abbrev"]?.let { "git-$it" } ?: "unknown"

    lateinit var eventManager: EventManager

    var hasLoaded = false
        private set

    fun init() {
        logInfo("Initialize $NAME $VERSION")
        eventManager = EventManager()
        DependencyDownloader.asyncLoad()
    }

    fun load() {
        logInfo("Loading $NAME $VERSION")
        setTitle("LoadClient")

        DependencyDownloader.awaitLoad()

        eventManager.registerListener(ConfigManager)
        CommandManager
        eventManager.registerListener(Pathfinder)
        eventManager.registerListener(ModuleManager)

        ConfigManager.loadDefault()

        hasLoaded = true
        setTitle("HaveFun")
    }

    fun shutdown() {
        ConfigManager.save()
    }
}