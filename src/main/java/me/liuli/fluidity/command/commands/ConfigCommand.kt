package me.liuli.fluidity.command.commands

import me.liuli.fluidity.command.Command
import me.liuli.fluidity.config.ConfigManager

class ConfigCommand : Command("config", "Manage configs of the client") {
    private val rootSyntax = "<load/save/list/reload>"

    override fun exec(args: Array<String>) {
        if (args.isEmpty()) {
            chatSyntax(rootSyntax)
            return
        }

        when (args[0].lowercase()) {
            "load" -> {
                if (args.size> 1) {
                    ConfigManager.load(args[1])
                } else {
                    chatSyntax("load <config name>")
                }
            }

            "save" -> {
                ConfigManager.save()
                chat("Config §l${ConfigManager.nowConfig}§r saved")
            }

            "list" -> {
                chat("Configs:")
                ConfigManager.configPath.listFiles()
                    .filter { it.isFile }
                    .map { f ->
                        f.name.let { if (it.endsWith(".json")) it.substring(0, it.length - 5) else it }
                    }
                    .forEach {
                        chat("  " + if (it == ConfigManager.nowConfig) "§3§l$it§r" else "§c$it")
                    }
            }

            "reload" -> {
                ConfigManager.reload()
                chat("Config §l${ConfigManager.nowConfig}§r reloaded")
            }

            else -> chatSyntax(rootSyntax)
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("load", "save", "list", "reload").filter { it.startsWith(args[0], true) }
            2 -> when (args[0].lowercase()) {
                "load" -> {
                    (ConfigManager.configPath.listFiles() ?: return emptyList())
                        .filter { it.isFile }
                        .map { f ->
                            f.name.let { if (it.endsWith(".json")) it.substring(0, it.length - 5) else it }
                        }
                        .filter { it.startsWith(args[1], true) }
                }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}