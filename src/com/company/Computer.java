package com.company;

import java.util.function.Predicate;

public class Computer {

    /**
     * Makes one move for the computer.
     *
     * @param gameState the current GameState
     * @return returns true iff GUI has to be redrawn
     */
    public static boolean move(GameState gameState) {
        switch (gameState.currentPhase) {
            case CLAIMComputer:
                gameState.map.getRandomTerritory(Territory.UNCLAIMED).setArmy(-1);

                if (!gameState.map.containsTerritory(Territory.UNCLAIMED)) {
                    gameState.currentPhase = GamePhase.REINFORCE;
                    gameState.updateArmy();
                } else
                    gameState.currentPhase = GamePhase.CLAIM;

                return true;
            case REINFORCEComputer: {
                for (; gameState.armyComputer > 0; gameState.armyComputer--)
                    gameState.map.getRandomTerritory(Territory.OWNED_COMP).addArmy(1);
                gameState.currentPhase = GamePhase.ATTACK;
                break;
            }
        }

        return false;
    }

    public static Fight attack(GameState state) {
        if (state.currentPhase != GamePhase.ATTACKComputer)
            throw new IllegalStateException("Computer can only attack in ATTACKComputer phase");

        Predicate<Territory> p = t -> t.getArmy() < -1 && state.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0;
        Territory atk = state.map.getRandomTerritory(p);
        if (atk == null)
            return null;
        Territory def = Helper.getRandom(state.map.getNeighbors(atk, Territory.OWNED_PLAYER));
        return new Fight(atk, def);
    }
}
