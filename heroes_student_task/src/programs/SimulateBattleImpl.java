package com.heroes_task.programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Симуляция боя:
 *  раунд: собираем всех живых из обеих армий и сортируем по baseAttack
 *  каждый живой юнит делает не более одного хода за раунд
 *  если юнит умер до своего хода - он пропускается (фактически исключён из очереди)
 *  если в раунде никто не смог атаковать (все вернули null) - симуляция завершается
 *
 * Сложность одного раунда: O(N log N), N - число живых юнитов.
 */
public class SimulateBattleImpl implements SimulateBattle {

    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        if (playerArmy == null || computerArmy == null) return;

        while (hasAlive(playerArmy) && hasAlive(computerArmy)) {
            PriorityQueue<Unit> turnQueue = new PriorityQueue<>(
                    Comparator.comparingInt(Unit::getBaseAttack).reversed()
            );

            addAlive(turnQueue, playerArmy);
            addAlive(turnQueue, computerArmy);

            boolean anyAttackThisRound = false;

            while (!turnQueue.isEmpty()) {
                Unit attacker = turnQueue.poll();
                if (attacker == null || !attacker.isAlive()) continue;
                if (attacker.getProgram() == null) continue;

                Unit target = attacker.getProgram().attack();

                if (printBattleLog != null) {
                    printBattleLog.printBattleLog(attacker, target);
                }

                if (target != null) {
                    anyAttackThisRound = true;
                }
            }

            if (!anyAttackThisRound) {
                return;
            }
        }
    }

    private static void addAlive(PriorityQueue<Unit> pq, Army army) {
        if (army.getUnits() == null) return;
        for (Unit u : army.getUnits()) {
            if (u != null && u.isAlive()) {
                pq.add(u);
            }
        }
    }

    private static boolean hasAlive(Army army) {
        if (army.getUnits() == null) return false;
        for (Unit u : army.getUnits()) {
            if (u != null && u.isAlive()) return true;
        }
        return false;
    }
}
