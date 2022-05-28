package me.liuli.fluidity.command.commands

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.command.Command

class ConfigCommand : Command("config", "Manage configs of the client") {
    private val rootSyntax = "<load/save/reload>"

    override fun exec(args: Array<String>) {
        if (args.isEmpty()) {
            chatSyntax(rootSyntax)
            return
        }

        when (args[0].lowercase()) {
            "load" -> {
                if (args.size> 1) {
                    Fluidity.configManager.load(args[1])
                } else {
                    chatSyntax("load <config name>")
                }
            }

            "save" -> {
                Fluidity.configManager.save()
                chat("Config §l${Fluidity.configManager.nowConfig}§r saved")
            }

            "reload" -> {
                Fluidity.configManager.reload()
                chat("Config §l${Fluidity.configManager.nowConfig}§r reloaded")
            }

            else -> chatSyntax(rootSyntax)
        }
    }
}