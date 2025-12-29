package com.heroes_task.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

/**
 * Поиск кратчайшего пути на поле WIDTH x HEIGHT с препятствиями.
 * Используем BFS (так как все рёбра веса 1) => O(WIDTH*HEIGHT).
 */
public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;

    private static final int[] DX = { 1, -1,  0,  0,  1,  1, -1, -1 };
    private static final int[] DY = { 0,  0,  1, -1,  1, -1,  1, -1 };

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        if (attackUnit == null || targetUnit == null) return Collections.emptyList();

        int sx = attackUnit.getxCoordinate();
        int sy = attackUnit.getyCoordinate();
        int tx = targetUnit.getxCoordinate();
        int ty = targetUnit.getyCoordinate();

        if (!inBounds(sx, sy) || !inBounds(tx, ty)) return Collections.emptyList();

        if (sx == tx && sy == ty) {
            return Collections.singletonList(new Edge(sx, sy));
        }

        boolean[][] blocked = new boolean[WIDTH][HEIGHT];
        if (existingUnitList != null) {
            for (Unit u : existingUnitList) {
                if (u == null || !u.isAlive()) continue;
                if (u == attackUnit || u == targetUnit) continue;

                int x = u.getxCoordinate();
                int y = u.getyCoordinate();
                if (inBounds(x, y)) blocked[x][y] = true;
            }
        }

        int total = WIDTH * HEIGHT;
        boolean[] visited = new boolean[total];
        int[] prev = new int[total];
        Arrays.fill(prev, -1);

        ArrayDeque<Integer> q = new ArrayDeque<>();

        int start = encode(sx, sy);
        int goal = encode(tx, ty);

        visited[start] = true;
        q.add(start);

        while (!q.isEmpty()) {
            int cur = q.poll();
            if (cur == goal) break;

            int cx = cur / HEIGHT;
            int cy = cur % HEIGHT;

            for (int i = 0; i < 8; i++) {
                int nx = cx + DX[i];
                int ny = cy + DY[i];

                if (!inBounds(nx, ny)) continue;
                if (blocked[nx][ny]) continue;

                int ni = encode(nx, ny);
                if (visited[ni]) continue;

                visited[ni] = true;
                prev[ni] = cur;
                q.add(ni);
            }
        }

        if (!visited[goal]) return Collections.emptyList();

        ArrayList<Edge> path = new ArrayList<>();
        for (int cur = goal; cur != -1; cur = prev[cur]) {
            int x = cur / HEIGHT;
            int y = cur % HEIGHT;
            path.add(new Edge(x, y));
        }
        Collections.reverse(path);
        return path;
    }

    private static boolean inBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    private static int encode(int x, int y) {
        return x * HEIGHT + y;
    }
}
