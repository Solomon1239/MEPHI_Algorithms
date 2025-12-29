package com.heroes_task.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * isLeftArmyTarget:
 *  true => атакуют левую армию (обычно x=0..2), атакующий приходит справа => фронт = последняя колонка (index 2)
 *  false => атакуют правую армию (обычно x=24..26), атакующий приходит слева  => фронт = первая колонка (index 0)
 *
 * Сложность: O(K), где K - число юнитов в трёх колонках.
 */
public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    private static final int HEIGHT = 21;

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        if (unitsByRow == null || unitsByRow.isEmpty()) return Collections.emptyList();

        int cols = unitsByRow.size();
        if (cols == 0) return Collections.emptyList();

        int frontCol = isLeftArmyTarget ? cols - 1 : 0;
        int stepToFront = (frontCol == 0) ? -1 : +1;

        boolean[][] occupancy = new boolean[cols][HEIGHT];

        for (int c = 0; c < cols; c++) {
            List<Unit> list = unitsByRow.get(c);
            if (list == null) continue;

            for (Unit u : list) {
                if (u == null || !u.isAlive()) continue;
                int y = u.getyCoordinate();
                if (y >= 0 && y < HEIGHT) {
                    occupancy[c][y] = true;
                }
            }
        }

        ArrayList<Unit> result = new ArrayList<>();
        Set<Unit> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        for (int c = 0; c < cols; c++) {
            List<Unit> list = unitsByRow.get(c);
            if (list == null) continue;

            for (Unit u : list) {
                if (u == null || !u.isAlive()) continue;
                int y = u.getyCoordinate();
                if (y < 0 || y >= HEIGHT) continue;

                boolean open;
                if (c == frontCol) {
                    open = true;
                } else {
                    int ahead = c + stepToFront;
                    if (ahead < 0 || ahead >= cols) {
                        open = true;
                    } else {
                        open = !occupancy[ahead][y];
                    }
                }

                if (open && seen.add(u)) {
                    result.add(u);
                }
            }
        }

        return result;
    }
}
