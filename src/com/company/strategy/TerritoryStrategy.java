package com.company.strategy;

import com.company.GameState;
import com.company.Territory;

public interface TerritoryStrategy {
    Territory getTerritory(GameState state);
}
