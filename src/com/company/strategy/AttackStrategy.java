package com.company.strategy;

import com.company.GameState;
import com.company.Territory;

public interface AttackStrategy {
    Territory getAtk(GameState state);

    Territory getDef(GameState state, Territory atk);
}
