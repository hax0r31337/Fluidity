package me.liuli.fluidity.config.sections

import com.google.gson.JsonObject
import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.config.ConfigSection

class ValueSection : ConfigSection("value") {
    override fun load(obj: JsonObject?) {
        val json = obj ?: JsonObject()

        Fluidity.moduleManager.modules.forEach { module ->
            val moduleJson = if (json.has(module.name)) { json.getAsJsonObject(module.name) } else { JsonObject() }

            module.getValues().forEach { value ->
                if (moduleJson.has(value.name)) {
                    value.fromJson(moduleJson.get(value.name))
                } else {
                    value.reset()
                }
            }
        }
    }

    override fun save(): JsonObject {
        val json = JsonObject()

        Fluidity.moduleManager.modules.forEach { module ->
            val values = module.getValues()
            if (values.isNotEmpty()) {
                val moduleJson = JsonObject()

                values.forEach { value ->
                    moduleJson.add(value.name, value.toJson())
                }

                json.add(module.name, moduleJson)
            }
        }

        return json
    }
}