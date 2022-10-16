/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.config.sections

import com.google.gson.JsonObject
import me.liuli.fluidity.command.CommandManager
import me.liuli.fluidity.config.ConfigSection

class CommandSection : ConfigSection("command") {
    override fun load(json: JsonObject?) {
        if (json == null || !json.has("prefix")) {
            CommandManager.prefix = CommandManager.defaultPrefix
        } else {
            CommandManager.prefix = json.get("prefix").asString
        }
    }

    override fun save(): JsonObject {
        val json = JsonObject()

        json.addProperty("prefix", CommandManager.prefix)

        return json
    }
}