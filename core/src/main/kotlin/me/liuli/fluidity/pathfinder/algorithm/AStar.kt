/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.pathfinder.algorithm

import me.liuli.fluidity.pathfinder.PathfinderController
import me.liuli.fluidity.pathfinder.goals.IGoal
import me.liuli.fluidity.pathfinder.path.PathMove
import net.minecraft.util.Vec3i

class AStar(start: PathMove, private val settings: PathfinderController, val goal: IGoal) {

    private val closedDataSet = hashSetOf<Int>()
    private val openHeap = BinaryHeapOpenSet()
    private val openDataMap = hashMapOf<Int, PathNode>()
    val visitedChunks = hashSetOf<Long>()
    private val maxCost: Int
    private var computedTime = 0

    private var bestNode: PathNode

    init {
        val startNode = PathNode(start, .0, goal.heuristic(start))
        openHeap.push(startNode)
        openDataMap[startNode.data.hashCode()] = startNode
        bestNode = startNode

        maxCost = if (settings.searchRadius < 0) -1 else startNode.h.toInt() + settings.searchRadius
    }

    fun compute(): Result {
        val computeStartTime = System.currentTimeMillis()
        while (!openHeap.isEmpty()) {
            if (System.currentTimeMillis() - computeStartTime > settings.searchTickTimeout) {
                computedTime += (System.currentTimeMillis() - computeStartTime).toInt()
                return makeResult(ResultStatus.PARTIAL, this.bestNode)
            }
            if (System.currentTimeMillis() - computeStartTime > settings.searchTimeout - computedTime) {
                computedTime += (System.currentTimeMillis() - computeStartTime).toInt()
                return makeResult(ResultStatus.TIMEOUT, this.bestNode)
            }
            val node = this.openHeap.pop()
            if (this.goal.isEnd(Vec3i(node.data.x, node.data.y, node.data.z))) {
                computedTime += (System.currentTimeMillis() - computeStartTime).toInt()
                return makeResult(ResultStatus.SUCCESS, node)
            }
            // not done yet
            this.openDataMap.remove(node.data.hashCode())
            this.closedDataSet.add(node.data.hashCode())
            this.visitedChunks.add(getChunkCode(node.data.x, node.data.z))

            val neighbors = this.settings.getNeighbors(node.data)
            for (neighborData in neighbors) {
                if (this.closedDataSet.contains(neighborData.hashCode())) {
                    continue // skip closed neighbors
                }
                val gFromThisNode = node.g + neighborData.cost
                var neighborNode = this.openDataMap[neighborData.hashCode()]
                var update = false

                val heuristic = this.goal.heuristic(neighborData)
                if (this.maxCost > 0 && gFromThisNode + heuristic > this.maxCost) continue

                if (neighborNode == null) {
                    // add neighbor to the open set
                    neighborNode = PathNode()
                    // properties will be set later
                    this.openDataMap[neighborData.hashCode()] = neighborNode
                } else {
                    if (neighborNode.g < gFromThisNode) {
                        // skip this one because another route is faster
                        continue
                    }
                    update = true
                }
                // found a new or better route.
                // update this neighbor with this node as its new parent
                neighborNode.set(neighborData, gFromThisNode, heuristic, node)
                if (neighborNode.h < this.bestNode.h) this.bestNode = neighborNode
                if (update) {
                    this.openHeap.update(neighborNode)
                } else {
                    this.openHeap.push(neighborNode)
                }
            }
        }
        // all the neighbors of every accessible node have been exhausted
        computedTime += (System.currentTimeMillis() - computeStartTime).toInt()
        return makeResult(ResultStatus.NO_PATH, this.bestNode)
    }

    fun visitedChunk(x: Int, z: Int): Boolean {
        return visitedChunks.contains(getChunkCode(x, z))
    }

    private fun getChunkCode(x: Int, z: Int): Long {
        return (x.toLong() shr 4 shl 32) or (z.toLong() shr 4)
    }

    private fun makeResult(status: ResultStatus, node: PathNode): Result {
        val path = mutableListOf<PathMove>()
        var iterNode: PathNode? = node
        while(iterNode?.parent != null) {
            path.add(iterNode.data)
            iterNode = iterNode.parent
        }
        path.reverse()

        return Result(status, node.g, computedTime, this.closedDataSet.size,
            this.closedDataSet.size + this.openHeap.size(), path, this)
    }

    data class Result(val status: ResultStatus, val cost: Double, val timeCost: Int,
                      val visitedNodes: Int, val generatedNodes: Int, val path: MutableList<PathMove>, val context: AStar)

    enum class ResultStatus {
        PARTIAL,
        TIMEOUT,
        NO_PATH,
        SUCCESS
    }
}