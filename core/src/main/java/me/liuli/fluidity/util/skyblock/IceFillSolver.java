/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.util.skyblock;

import me.liuli.fluidity.util.move.Vec2i;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://github.com/bowser0000/SkyblockMod/blob/master/src/main/java/me/Danker/utils/IceWalkUtils.java">original code</a>
 */
public class IceFillSolver {

//    public static List<Vec2i> solve(boolean[][] board) {
//        Vec2i startPos = new Vec2i(board.length - 1, board[0].length / 2);
//        Vec2i endPos = new Vec2i(0, board[0].length / 2);
//        List<Vec2i> route = new ArrayList<>();
//        route.add(startPos);
//        return findSolution(board, startPos, endPos, route);
//    }

    public static List<Vec2i> findSolution(boolean[][] board, Vec2i startPos, Vec2i endPos, List<Vec2i> route) {
        for (IcePathSolver.Direction direction : IcePathSolver.directions) {
            Vec2i nextPoint = move(board, startPos, direction);
            if (nextPoint == null || route.contains(nextPoint)) continue;
            List<Vec2i> newRoute = new ArrayList<>(route);
            newRoute.add(nextPoint);
            if (nextPoint.equals(endPos) && isComplete(board, newRoute)) return newRoute;
            List<Vec2i> solution = findSolution(board, nextPoint, endPos, newRoute);
            if (solution == null) continue;
            return solution;
        }
        return null;
    }

    public static Vec2i move(boolean[][] board, Vec2i pos, IcePathSolver.Direction direction) {
        switch (direction) {
            case UP:
                if (pos.row != 0 && !board[pos.row - 1][pos.col]) {
                    return new Vec2i(pos.row - 1, pos.col);
                }
                break;
            case DOWN:
                if (pos.row != board.length - 1 && !board[pos.row + 1][pos.col]) {
                    return new Vec2i(pos.row + 1, pos.col);
                }
                break;
            case LEFT:
                if (pos.col != 0 && !board[pos.row][pos.col - 1]) {
                    return new Vec2i(pos.row, pos.col - 1);
                }
                break;
            case RIGHT:
                if (pos.col != board[0].length - 1 && !board[pos.row][pos.col + 1]) {
                    return new Vec2i(pos.row, pos.col + 1);
                }
                break;
        }
        return null;
    }

    public static boolean isComplete(boolean[][] board, List<Vec2i> route) {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[0].length; column++) {
                if (!board[row][column] && !route.contains(new Vec2i(row, column))) return false;
            }
        }
        return true;
    }
}