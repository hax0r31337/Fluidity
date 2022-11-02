/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.inject.transformer

import me.yuugiri.hutil.HookUtility
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

class JvmInstrumentationTransformer(private val hook: HookUtility) : ClassFileTransformer {

    override fun transform(classLoader: ClassLoader, name: String, klass: Class<*>, protectionDomain: ProtectionDomain, byteArray: ByteArray): ByteArray {
        if (name.startsWith("kotlin") || name.startsWith("me.yuugiri") || name.startsWith("org.spongepowered")) return byteArray

        return HookUtilityCompatTransformer.hook.dealWithClassData(byteArray, name.replace('.', '/'))
    }
}