package me.liuli.fluidity

import me.liuli.fluidity.command.CommandManager
import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.event.EventManager
import me.liuli.fluidity.module.ModuleManager
import me.liuli.fluidity.util.client.logError
import me.liuli.fluidity.util.client.logInfo
import me.liuli.fluidity.util.client.setTitle
import me.liuli.fluidity.util.other.DiscordRPC
import kotlin.concurrent.thread

object Fluidity {
    @JvmStatic
    val name = "Fluidity"
    @JvmStatic
    val coloredName = "§3F§bluidity"
    @JvmStatic
    val version = "1.0.0"
    @JvmStatic
    val author = "Liulihaocai"

    lateinit var eventManager: EventManager
    lateinit var configManager: ConfigManager
    lateinit var commandManager: CommandManager
    lateinit var moduleManager: ModuleManager

    fun init() {
        logInfo("Initialize $name v$version")
        eventManager = EventManager()
    }

    fun load() {
        logInfo("Loading $name v$version")
        setTitle("Loading Client...")

        configManager = ConfigManager()
        eventManager.registerListener(configManager)

        commandManager = CommandManager()

        moduleManager = ModuleManager()
        eventManager.registerListener(moduleManager)

        configManager.loadDefault()

        thread {
            try {
                DiscordRPC.run()
            } catch (e: Throwable) {
                logError("Failed to load DiscordRPC.", e)
            }
        }

        setTitle(null)
    }

    fun shutdown() {
        configManager.save()
    }
}