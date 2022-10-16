/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.command.commands

import me.liuli.fluidity.command.Command
import me.liuli.fluidity.module.ModuleManager

class ToggleCommand : Command("toggle", "Allow you toggle modules without open ClickGui", arrayOf("t")) {
    override fun exec(args: Array<String>) {
        if (args.isNotEmpty()) {
            args.forEach {
                if (it.isBlank()) {
                    return@forEach
                }

                val module = ModuleManager.getModule(it)
                if (module == null) {
                    chat("Module \"$it\" not found.")
                } else {
                    module.toggle()
                    chat("Toggled module \"${module.name}\" ${if (module.state){"§aON"}else {"§cOFF"}}")
                }
            }
            return
        }
        chatSyntax("<module>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val moduleName = args[0]

        return ModuleManager.modules
            .map { it.name }
            .filter { it.startsWith(moduleName, true) }
            .toList()
    }
}