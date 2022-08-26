package me.liuli.fluidity.pathfinder.algorithm

import me.liuli.fluidity.pathfinder.path.PathMove


class PathNode() {

    lateinit var data: PathMove
    var g = .0
    var h = .0
    var f = .0
    var parent: PathNode? = null

    @JvmField
    var heapPosition = 0

    constructor(data: PathMove, g: Double, h: Double, parent: PathNode? = null) : this() {
        this.set(data, g, h, parent)
    }

    fun set(data: PathMove, g: Double, h: Double, parent: PathNode? = null) {
        this.data = data
        this.g = g
        this.h = h
        this.f = g + h
        this.parent = parent
    }
}