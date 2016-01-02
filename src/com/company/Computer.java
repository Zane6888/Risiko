package com.company;

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
}
