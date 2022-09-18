package me.liuli.fluidity.pathfinder.path

import net.minecraft.util.Vec3i

data class PathPlaceInfo(val x: Int, val y: Int, val z: Int,
                         val dx: Int, val dy: Int, val dz: Int,
                         val returnPos: Vec3i? = null, val useOne: Boolean = false, val jump: Boolean = false)