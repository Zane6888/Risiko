package com.company;

import com.company.strategy.*;

public class Computer {

    public TerritoryStrategy claimStrategy = new RandomClaimStrategy();
    public TerritoryStrategy reinforceStrategy = new RandomReinforceStrategy();
    public AttackStrategy attackStrategy = new RandomAttackStrategy();
    public FollowStrategy followStrategy = (s, f) -> {
    };
    public PlainStrategy moveStrategy = s -> {
    };

    public void doTurn(GameState state) {
        switch (state.currentPhase) {
            case CLAIMComputer:
                Territory claimed = claimStrategy.getTerritory(state);
                claimed.setArmy(-1);
                state.map.updateMonopol(claimed);

                if (!state.map.containsTerritory(Territory.UNCLAIMED)) {
                    state.currentPhase = GamePhase.REINFORCE;
                    state.updateArmy();
                } else
                    state.currentPhase = GamePhase.CLAIM;
                break;
            case REINFORCEComputer: {
                for (; state.armyComputer > 0; state.armyComputer--)
                    reinforceStrategy.getTerritory(state).addArmy(1);
                if (state.map.containsTerritory(t -> t.getArmy() > 1 && state.map.getNeighbors(t, Territory.OWNED_COMP).size() > 0))
                    state.currentPhase = GamePhase.ATTACK;
                else
                    state.currentPhase = GamePhase.MOVE;
                break;
            }
        }
    }

    public Fight attack(GameState state) {
        if (state.currentPhase != GamePhase.ATTACKComputer)
            throw new IllegalStateException("Computer can only attack in ATTACKComputer phase");

        Territory atk = attackStrategy.getAtk(state);
        if (atk == null)
            return null;
        Territory def = attackStrategy.getDef(state, atk);
        return new Fight(atk, def);
    }

    public void doPostAttack(GameState state, Fight fight) {
        switch (state.currentPhase) {
            case FOLLOWComputer:
                followStrategy.execute(state, fight);
                state.currentPhase = GamePhase.MOVEComputer;
                doPostAttack(state, fight);
                break;
            case MOVEComputer:
                moveStrategy.execute(state);
                state.currentPhase = GamePhase.REINFORCE;
                state.updateArmy();
                break;
        }
    }
}
