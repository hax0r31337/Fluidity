package me.liuli.fluidity.command.commands

import me.liuli.fluidity.command.Command
import me.liuli.fluidity.util.mc

class SayCommand : Command("say", "Allows you to say something without change the prefix") {
    override fun exec(args: Array<String>) {
        if (args.isNotEmpty()) {
            mc.thePlayer.sendChatMessage(args.joinToString(" "))
            chat("Your message was successfully sent to the chat.")
            return
        }
        chatSyntax("<message>")
    }
}