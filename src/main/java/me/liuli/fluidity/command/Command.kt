package me.liuli.fluidity.command

import me.liuli.fluidity.util.client.displayAlert

abstract class Command(val command: String, val description: String, val subCommand: Array<String> = emptyArray()) {
    abstract fun exec(args: Array<String>)

    open fun tabComplete(args: Array<String>): List<String> {
        return emptyList()
    }

    protected fun chat(msg: String) = displayAlert(msg)

    protected fun chatSyntax(syntax: String) = displayAlert("Syntax: ${CommandManager.prefix}$command $syntax")
}