package com.company.strategy;

import com.company.GameState;
import com.company.Territory;

public class RandomClaimStrategy implements TerritoryStrategy {
    @Override
    public Territory getTerritory(GameState state) {
        return state.map.getRandomTerritory(Territory.UNCLAIMED);
    }
}
