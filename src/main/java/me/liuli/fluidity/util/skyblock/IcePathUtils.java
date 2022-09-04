package me.liuli.fluidity.util.skyblock;

import me.liuli.fluidity.util.move.Vec2i;

import java.util.*;

/**
 * <a href="https://github.com/bowser0000/SkyblockMod/blob/master/src/main/java/me/Danker/utils/SilverfishUtils.java">original code</a>
 */
public class IcePathUtils {

    public static final Direction[] directions = Direction.values();

    // bfs
    public static List<Vec2i> solve(boolean[][] board, Vec2i startPos, List<Vec2i> endColumns) {
        LinkedList<Vec2i> queue = new LinkedList<>();
        Map<Vec2i, Vec2i> visited = new HashMap<>();
        queue.add(startPos);
        visited.put(startPos, null);
        while (!queue.isEmpty()) {
            if (queue.size() > 1000000) break;
            Vec2i position = queue.pollFirst();
            for (Direction direction : directions) {
                Vec2i pushedPoint = push(board, position, direction);
                if (visited.containsKey(pushedPoint)) continue;
                queue.add(pushedPoint);
                visited.put(pushedPoint, position);
                for (Vec2i endColumn : endColumns) {
                    if (pushedPoint != null && pushedPoint.equals(endColumn)) {
                        List<Vec2i> route = new ArrayList<>();
                        Vec2i lastPoint = pushedPoint;
                        while (lastPoint != null) {
                            route.add(0, lastPoint);
                            lastPoint = visited.get(lastPoint);
                        }
                        return route;
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    public static Vec2i push(boolean[][] board, Vec2i pos, Direction direction) {
        switch (direction) {
            case UP:
                for (int row = pos.row; row >= 0; row--) {
                    if (board[row][pos.col]) {
                        return new Vec2i(row + 1, pos.col);
                    }
                }
                return new Vec2i(0, pos.col);
            case DOWN:
                for (int row = pos.row; row <= 18; row++) {
                    if (board[row][pos.col]) {
                        return new Vec2i(row - 1, pos.col);
                    }
                }
                return new Vec2i(18, pos.col);
            case LEFT:
                for (int column = pos.col; column >= 0; column--) {
                    if (board[pos.row][column]) {
                        return new Vec2i(pos.row, column + 1);
                    }
                }
                return new Vec2i(pos.row, 0);
            case RIGHT:
                for (int column = pos.col; column <= 18; column++) {
                    if (board[pos.row][column]) {
                        return new Vec2i(pos.row, column - 1);
                    }
                }
                return new Vec2i(pos.row, 18);
        }
        return null;
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
}
