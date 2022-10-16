/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.command.commands

import me.liuli.fluidity.command.Command
import me.liuli.fluidity.pathfinder.Pathfinder
import me.liuli.fluidity.pathfinder.goals.GoalBlock
import me.liuli.fluidity.util.client.displayAlert

class RouteCommand : Command("route", "Controls pathfinder") {
    override fun exec(args: Array<String>) {
        if (args.isNotEmpty()) {
            when(args[0]) {
                "to" -> {
                    Pathfinder.setGoal(GoalBlock(args[1].toInt(), args[2].toInt(), args[3].toInt()))
                    displayAlert("routing...")
                }
                "stop" -> {
                    Pathfinder.stateGoal = null
                    Pathfinder.resetPath(true)
                    displayAlert("stopped")
                }
                else -> chatSyntax("<to/stop>")
            }
            return
        }
        chatSyntax("<to/stop>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        return if (args.size == 1) {
            arrayOf("to", "stop").filter { it.startsWith(args[0]) }
        } else {
            emptyList()
        }
    }
}