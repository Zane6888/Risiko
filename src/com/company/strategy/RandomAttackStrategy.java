package com.company.strategy;

import com.company.GameState;
import com.company.Helper;
import com.company.Territory;

import java.util.function.Predicate;

public class RandomAttackStrategy implements AttackStrategy {
    @Override
    public Territory getAtk(GameState state) {
        Predicate<Territory> p = t -> t.getArmy() < -1 && state.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0;
        return state.map.getRandomTerritory(p);
    }

    @Override
    public Territory getDef(GameState state, Territory atk) {
        return Helper.getRandom(state.map.getNeighbors(atk, Territory.OWNED_PLAYER));
    }
}
