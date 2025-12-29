package com.heroes_task.programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

/**
 * Генерация армии компьютера:
 *  приоритет: max(attack/cost), затем max(health/cost)
 *  ограничение: не более 11 юнитов каждого типа
 *  укладываемся в maxPoints
 *  расставляем на поле 0..2 по X, 0..20 по Y без пересечений
 *
 * Сложность: O(T log T + U), T - число типов, U - число добавленных юнитов (<= 11*T).
 */
public class GeneratePresetImpl implements GeneratePreset {

    private static final int X_MIN = 0;
    private static final int X_MAX = 2;
    private static final int HEIGHT = 21;

    private static final int MAX_PER_TYPE = 11;

    private static final class TypeInfo {
        final Unit template;
        final double atkPerCost;
        final double hpPerCost;

        TypeInfo(Unit template) {
            this.template = template;
            int cost = Math.max(0, template.getCost());
            this.atkPerCost = cost == 0 ? 0.0 : (template.getBaseAttack() * 1.0 / cost);
            this.hpPerCost = cost == 0 ? 0.0 : (template.getHealth() * 1.0 / cost);
        }
    }

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        Army army = new Army();

        if (unitList == null || unitList.isEmpty() || maxPoints <= 0) {
            army.setUnits(Collections.emptyList());
            army.setPoints(0);
            return army;
        }

        List<TypeInfo> types = new ArrayList<>();
        for (Unit u : unitList) {
            if (u != null && u.getCost() > 0) {
                types.add(new TypeInfo(u));
            }
        }
        if (types.isEmpty()) {
            army.setUnits(Collections.emptyList());
            army.setPoints(0);
            return army;
        }

        types.sort((a, b) -> {
            int c = Double.compare(b.atkPerCost, a.atkPerCost);
            if (c != 0) return c;
            c = Double.compare(b.hpPerCost, a.hpPerCost);
            if (c != 0) return c;
            c = Integer.compare(b.template.getBaseAttack(), a.template.getBaseAttack());
            if (c != 0) return c;
            c = Integer.compare(b.template.getHealth(), a.template.getHealth());
            if (c != 0) return c;
            return Integer.compare(a.template.getCost(), b.template.getCost());
        });

        List<int[]> freeCells = buildAndShuffleCells(maxPoints, types.size());

        Map<String, Integer> perTypeCount = new HashMap<>();
        Map<String, Integer> nameCounter = new HashMap<>();

        List<Unit> result = new ArrayList<>(Math.min(freeCells.size(), types.size() * MAX_PER_TYPE));
        int points = 0;

        for (TypeInfo type : types) {
            Unit t = type.template;
            String unitType = t.getUnitType();

            int already = perTypeCount.getOrDefault(unitType, 0);
            while (already < MAX_PER_TYPE
                    && !freeCells.isEmpty()
                    && points + t.getCost() <= maxPoints) {

                int[] cell = freeCells.remove(freeCells.size() - 1); // O(1)
                int x = cell[0];
                int y = cell[1];

                int num = nameCounter.getOrDefault(unitType, 0) + 1;
                nameCounter.put(unitType, num);

                Unit nu = new Unit(
                        unitType + " " + num,
                        t.getUnitType(),
                        t.getHealth(),
                        t.getBaseAttack(),
                        t.getCost(),
                        t.getAttackType(),
                        t.getAttackBonuses() == null ? null : new HashMap<>(t.getAttackBonuses()),
                        t.getDefenceBonuses() == null ? null : new HashMap<>(t.getDefenceBonuses()),
                        x,
                        y
                );

                result.add(nu);
                points += t.getCost();
                already++;
            }

            perTypeCount.put(unitType, already);

            if (points >= maxPoints || freeCells.isEmpty()) {
                break;
            }
        }

        army.setUnits(result);
        army.setPoints(points);
        return army;
    }

    private static List<int[]> buildAndShuffleCells(int maxPoints, int typeCount) {
        List<int[]> cells = new ArrayList<>((X_MAX - X_MIN + 1) * HEIGHT);

        for (int parity = 0; parity < 2; parity++) {
            for (int y = 0; y < HEIGHT; y++) {
                if ((y & 1) != parity) continue;
                for (int x = X_MIN; x <= X_MAX; x++) {
                    cells.add(new int[]{x, y});
                }
            }
        }

        long seed = 1469598103934665603L;
        seed ^= maxPoints; seed *= 1099511628211L;
        seed ^= typeCount; seed *= 1099511628211L;

        Collections.shuffle(cells, new Random(seed));
        return cells;
    }
}
