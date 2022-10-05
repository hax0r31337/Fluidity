package minecraft

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import utils.resourceCached
import java.io.File

class MinecraftAssetsDownloader(val assetsVersion: String, val jsonUrl: String, val assetsDir: File) {

    val assetsObjects: JsonObject

    init {
        val json = JsonParser().parse(resourceCached(assetsDir, "indexes/$assetsVersion.json", jsonUrl).reader(Charsets.UTF_8)).asJsonObject
        assetsObjects = json.getAsJsonObject("objects")
    }

    fun getAssets(): List<File> {
        return assetsObjects.entrySet().map {
            val name = it.value.asJsonObject.get("hash").asString.let { "${it.substring(0, 2)}/$it" }
            resourceCached(assetsDir, "objects/$name", "https://resources.download.minecraft.net/$name")
        }
    }
}