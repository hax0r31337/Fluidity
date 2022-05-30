package me.liuli.fluidity.command.commands

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.command.Command
import me.liuli.fluidity.module.ModuleCommand
import me.liuli.fluidity.util.client.displayChatMessage

class HelpCommand : Command("help", "Show the list of commands") {
    override fun exec(args: Array<String>) {
        val commands = Fluidity.commandManager.commands.filter {
            val command = it.value
            command != this && command !is ModuleCommand
        }
        chat("${Fluidity.NAME} Client Commands(${commands.size}):")
        commands.forEach {
            val command = it.value
            displayChatMessage(" ${Fluidity.commandManager.prefix}${command.command} - ${command.description}")
        }
    }
}