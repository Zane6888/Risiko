package com.company;

import java.util.List;

public class Computer {

    /**
     * Makes one move for the computer.
     *
     * @param gameState the current GameState
     * @param gameMap   the Map that is played on
     * @return returns true iff GUI has to be redrawn
     */
    public static boolean move(GameState gameState, GameMap gameMap) {
        List<Continent> continents = gameMap.getContinents();
        switch (gameState.currentPhase) {
            case CLAIMComputer:
                gameMap.getRandomTerritory(Territory.UNCLAIMED).setArmy(-1);

                if (!gameMap.containsTerritory(Territory.UNCLAIMED))
                    gameState.currentPhase = GamePhase.REINFORCE;
                else
                    gameState.currentPhase = GamePhase.CLAIM;

                gameState.armyComputer = 0;
                gameState.armyPlayer = 0;
                for (Continent c : continents) c.calculateArmies(gameState);
                gameState.armyComputer /= 3;
                gameState.armyPlayer /= 3;

                return true;
            case REINFORCEComputer: {
                for (; gameState.armyComputer > 0; gameState.armyComputer--)
                    gameMap.getRandomTerritory(Territory.OWNED_COMP).addArmy(1);
                gameState.currentPhase = GamePhase.ATTACK;
                break;
            }
        }

        return false;
    }
}
