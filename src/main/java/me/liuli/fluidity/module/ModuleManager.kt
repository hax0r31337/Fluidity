package me.liuli.fluidity.module

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.KeyEvent
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Listener
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.client.logError
import me.liuli.fluidity.util.other.getObjectInstance
import me.liuli.fluidity.util.other.resolvePackage
import org.lwjgl.input.Keyboard

class ModuleManager : Listener {
    val modules = mutableListOf<Module>()

    private var pendingKeyBindModule: Module? = null

    init {
        resolvePackage("${this.javaClass.`package`.name}.modules", Module::class.java)
            .forEach {
                try {
                    registerModule(it.newInstance())
                } catch (e: IllegalAccessException) {
                    // this module is a kotlin object
                    registerModule(getObjectInstance(it))
                } catch (e: Throwable) {
                    logError("Failed to load module: ${it.name} (${e.javaClass.name}: ${e.message})")
                }
            }
    }

    fun registerModule(module: Module) {
        modules.add(module)
        Fluidity.eventManager.registerListener(module)

        // module command
        val values = module.getValues()
        if (module.command && values.isNotEmpty()) {
            Fluidity.commandManager.registerCommand(ModuleCommand(module, values))
        }
    }

    fun getModule(name: String): Module? {
        modules.forEach {
            if (it.name.equals(name, ignoreCase = true)) {
                return it
            }
        }
        return null
    }

    @Listen
    private fun onKey(event: KeyEvent) {
        if (pendingKeyBindModule != null) {
            pendingKeyBindModule!!.keyBind = event.key
            displayAlert("Bound module §l${pendingKeyBindModule!!.name}§r to key §l${Keyboard.getKeyName(event.key)}§r.")
            pendingKeyBindModule = null
            return
        }

        modules.filter { it.keyBind == event.key }.forEach { it.toggle() }
    }

    fun pendKeyBind(module: Module) {
        pendingKeyBindModule = module
        displayAlert("Press ANY key to set §l${module.name}§r key bind.")
    }

    override fun listen() = true
}