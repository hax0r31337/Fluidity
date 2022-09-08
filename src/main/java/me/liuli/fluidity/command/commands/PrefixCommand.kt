package me.liuli.fluidity.command.commands

import me.liuli.fluidity.command.Command
import me.liuli.fluidity.command.CommandManager

class PrefixCommand : Command("prefix", "Change the command prefix") {
    override fun exec(args: Array<String>) {
        if (args.size == 1) {
            CommandManager.prefix = args[0]
            chat("Command prefix successfully changed to '${args[0]}'")
        } else {
            chatSyntax("<prefix>")
        }
    }
}