/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.util.move

data class Vec2i(@JvmField var row: Int, @JvmField var col: Int) {

    constructor() : this(0, 0)
}