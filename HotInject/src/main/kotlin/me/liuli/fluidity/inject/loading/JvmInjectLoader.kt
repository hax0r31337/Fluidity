/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.loading

import com.sun.tools.attach.VirtualMachine
import com.sun.tools.attach.VirtualMachineDescriptor
import me.liuli.fluidity.Fluidity
import me.liuli.fluidity.inject.hooks.HookProvider
import me.liuli.fluidity.inject.processor.MethodNodeProcessor
import me.liuli.fluidity.inject.transformer.HookUtilityCompatTransformer
import me.liuli.fluidity.inject.transformer.JvmInstrumentationTransformer
import me.liuli.fluidity.util.other.asmName
import me.liuli.fluidity.util.other.resolveInstances
import me.yuugiri.hutil.HookUtility
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.obfuscation.SeargeObfuscationMap
import me.yuugiri.hutil.processor.AccessProcessor
import me.yuugiri.hutil.processor.hook.MethodHookProcessor
import java.io.File
import java.lang.instrument.Instrumentation
import java.net.URL
import java.net.URLClassLoader
import java.util.zip.GZIPInputStream
import javax.swing.*

fun agentmain(args: String, inst: Instrumentation) {
    // attach jar into classpath
    val cl = try {
        Class.forName("ave")
    } catch (e: Throwable) {
        Class.forName("net.minecraft.client.Minecraft")
    }.classLoader as URLClassLoader
    val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
    method.isAccessible = true
    method.invoke(cl, File(args).toURI().toURL())

    // hook classes
    try {
        val hook = loadHookUtility()

        inst.addTransformer(JvmInstrumentationTransformer(hook))

        inst.allLoadedClasses.forEach { klass ->
            val name = klass.asmName
            val classObf = AbstractObfuscationMap.classObfuscationRecord(hook.obfuscationMap, name)
            if(hook.processorList.any { !(!it.selectClass(classObf.name) && (name == classObf.name || !it.selectClass(name))) }) {
                println(name)
                inst.retransformClasses(klass)
            }
        }

        Fluidity.init()
        Fluidity.load()
    } catch (t: Throwable) {
        t.printStackTrace()
        println(t)
    }
}

fun main(args: Array<String>) {
    // load ToolsJar if not loaded
    try {
        Class.forName("com.sun.tools.attach.VirtualMachine")
    } catch (e: Throwable) {
        val classLoaderExt = HookProvider::class.java.classLoader as URLClassLoader
        val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
        method.isAccessible = true
        method.invoke(classLoaderExt, (JvmInjectLoaderUtils.getToolsJar() ?: throw Exception("No Tools.Jar has found :(")).toURI().toURL())
    }
    val selfJar = File(HookProvider::class.java.protectionDomain.codeSource.location.toURI())
    if (!selfJar.exists() || !selfJar.isFile) {
        System.err.println("[ERROR] Please run this as a Jar File.")
        return
    }

    val descriptor = JvmInjectLoaderUtils.selectJvm()
    if (descriptor == null) {
        System.err.println("Operation cancelled.")
        return
    }

    val vm = VirtualMachine.attach(descriptor)
    vm.loadAgent(selfJar.canonicalPath, selfJar.canonicalPath)
    vm.detach()

    println("Injected!")
}

private fun loadHookUtility(): HookUtility {
    val hook = HookUtility()
    val time = System.nanoTime()

    // load obfuscation mapEnumFacing
    hook.obfuscationMap = SeargeObfuscationMap(GZIPInputStream(JvmInstrumentationTransformer::class.java.getResourceAsStream("/1.8.9-mcp.srg.gz")).bufferedReader(Charsets.UTF_8))

    // load access transformer
    val accessRecords: Int
    hook.processorList.add(AccessProcessor.fromFMLAccessTransformer(JvmInstrumentationTransformer::class.java.getResourceAsStream("/fluidity_at.cfg")!!.bufferedReader(Charsets.UTF_8)).also { accessRecords = it.records.size })

    // load hooks
    val mnp = MethodNodeProcessor()
    var hookApplied = 0
    resolveInstances("${HookProvider::class.java.`package`.name}.impl", HookProvider::class.java)
        .forEach {
            it.applyHook(MethodHookProcessor, mnp)
            hookApplied++
        }
    hook.processorList.add(MethodHookProcessor)
    if (mnp.processorList.isNotEmpty()) hook.processorList.add(mnp)

    println("HookUtility initialized with [${hookApplied} hooks, ${mnp.processorList.size} method processors, $accessRecords access records] in ${((System.nanoTime() - time) / 1_000_00) / 10f} ms")

    return hook
}

object JvmInjectLoaderUtils {

    /**
     * pop a JOptionPane to select a target JVM.
     */
    fun selectJvm(): VirtualMachineDescriptor? {
        val panel = JPanel()
        panel.add(JLabel("Select target JVM:"))
        val model = DefaultComboBoxModel<String>()
        val list = VirtualMachine.list()
        list.sortedBy { if (it.displayName().contains("minecraft", true)) 0 else 1 }
            .map { model.addElement("${it.id()} - ${it.displayName().split(" ")[0]}") }
        val comboBox = JComboBox(model)
        panel.add(comboBox)

        when (JOptionPane.showConfirmDialog(null, panel, "Fluidity", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            JOptionPane.OK_OPTION -> return list.find { it.id() == comboBox.selectedItem.toString().split(" - ")[0] }
        }

        return null
    }

    /**
     * get tools jar by java.home
     */
    fun getToolsJar(): File? {
        if (System.getProperty("fluidity.toolsJar") != null) {
            return File(System.getProperty("fluidity.toolsJar"))
        }

        val javaHome = File(System.getProperty("java.home"))
        val toolsJar = File(javaHome, "lib/tools.jar")

        // if java.home is jdk
        if (toolsJar.exists()) {
            return toolsJar
        }

        // if java.home is jre
        if(javaHome.name.equals("jre", ignoreCase = true)) {
            File(javaHome.parentFile, "lib/tools.jar").also {
                if(it.exists())
                    return it
            }
        }

        return null
    }
}