package com.company.strategy;

import com.company.Fight;
import com.company.GameState;

public interface FollowStrategy {
    void execute(GameState state, Fight lastFight);
}
