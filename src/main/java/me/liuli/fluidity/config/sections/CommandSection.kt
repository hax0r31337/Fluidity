package me.liuli.fluidity.config.sections

import com.google.gson.JsonObject
import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.config.ConfigSection

class CommandSection : ConfigSection("command") {
    override fun load(json: JsonObject?) {
        if (json == null || !json.has("prefix")) {
            Fluidity.commandManager.prefix = Fluidity.commandManager.defaultPrefix
        } else {
            Fluidity.commandManager.prefix = json.get("prefix").asString
        }
    }

    override fun save(): JsonObject {
        val json = JsonObject()

        json.addProperty("prefix", Fluidity.commandManager.prefix)

        return json
    }
}