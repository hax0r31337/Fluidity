package me.liuli.fluidity.module

import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.KeyEvent
import me.liuli.fluidity.event.Listener
import me.liuli.fluidity.util.client.displayAlert
import org.lwjgl.input.Keyboard
import org.reflections.Reflections

class ModuleManager : Listener {
    val modules = mutableListOf<Module>()

    private var pendingKeyBindModule: Module? = null

    init {
        val reflections = Reflections("${this.javaClass.`package`.name}.modules")
        val subTypes: Set<Class<out Module>> = reflections.getSubTypesOf(Module::class.java)
        for (theClass in subTypes) {
            try {
                registerModule(theClass.newInstance())
            } catch (e: Exception) {
                e.printStackTrace()
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

    @EventMethod
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