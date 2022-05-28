package me.liuli.fluidity.command.commands

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.command.Command

class PrefixCommand : Command("prefix", "Change the command prefix") {
    override fun exec(args: Array<String>) {
        if (args.size == 1) {
            Fluidity.commandManager.prefix = args[0]
            chat("Command prefix successfully changed to '${args[0]}'")
        } else {
            chatSyntax("<prefix>")
        }
    }
}