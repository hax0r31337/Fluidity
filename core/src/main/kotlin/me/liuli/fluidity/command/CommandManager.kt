package me.liuli.fluidity.command

import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.client.logError
import me.liuli.fluidity.util.other.getObjectInstance
import me.liuli.fluidity.util.other.resolvePackage

object CommandManager {
    val defaultPrefix = "."

    var prefix = defaultPrefix
    val commands = HashMap<String, Command>()
    var latestAutoComplete: Array<String> = emptyArray()

    init {
        resolvePackage("${this.javaClass.`package`.name}.commands", Command::class.java)
            .forEach {
                try {
                    registerCommand(it.newInstance())
                } catch (e: IllegalAccessException) {
                    // this module is a kotlin object
                    registerCommand(getObjectInstance(it))
                } catch (e: Throwable) {
                    logError("Failed to load command: ${it.name} (${e.javaClass.name}: ${e.message})")
                }
            }
    }

    fun registerCommand(command: Command) {
        commands[command.command.lowercase()] = command
        command.subCommand.forEach {
            commands[it.lowercase()] = command
        }
    }

    fun getCommand(name: String): Command? {
        return commands[name.lowercase()]
    }

    fun handleCommand(msg: String) {
        val input = if (msg.startsWith(prefix)) { msg.substring(1) } else { msg }
        val args = input.split(" ").toTypedArray()
        val command = getCommand(args[0])
        if (command == null) {
            displayAlert("Command not found. Type ${prefix}help to view all commands.")
            return
        }

        try {
            command.exec(args.copyOfRange(1, args.size))
        } catch (e: Exception) {
            e.printStackTrace()
            displayAlert("An error occurred while executing the command($e)")
        }
    }

    fun autoComplete(input: String): Boolean {
        this.latestAutoComplete = this.getCompletions(input) ?: emptyArray()
        return input.startsWith(this.prefix) && this.latestAutoComplete.isNotEmpty()
    }

    private fun getCompletions(input: String): Array<String>? {
        if (input.isNotEmpty() && input[0] == this.prefix[0]) {
            val args = input.split(" ")

            return if (args.size > 1) {
                val command = getCommand(args[0].substring(1))
                val tabCompletions = command?.tabComplete(args.drop(1).toTypedArray())

                tabCompletions?.toTypedArray()
            } else {
                commands.map { ".${it.key}" }.filter { it.lowercase().startsWith(args[0].lowercase()) }.toTypedArray()
            }
        }
        return null
    }
}