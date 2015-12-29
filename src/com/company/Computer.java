package com.company;

import java.util.Collections;
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
                Collections.shuffle(continents);
                gameState.currentPhase = GamePhase.REINFORCE;
                for (Continent c : continents) {
                    if (c.conquer()) {
                        //Its now the players turn
                        gameState.currentPhase = GamePhase.CLAIM;

                        //Check if the whole map has been claimed already and set gamePhase accordingly
                        boolean mapIsFull = true;
                        for (Continent con : continents) {
                            mapIsFull &= con.isTaken();
                        }
                        if (mapIsFull) gameState.currentPhase = GamePhase.REINFORCE;

                        break;
                    }
                }
                gameState.armyComputer = 0;
                gameState.armyPlayer = 0;
                for (Continent c : continents) c.calculateArmies(gameState);
                gameState.armyComputer /= 3;
                gameState.armyPlayer /= 3;

                return true;
            case REINFORCEComputer: {
                while (gameState.armyComputer > 0) {
                    Collections.shuffle(continents);
                    if (continents.get(0).reinforce()) gameState.armyComputer--;
                }
                gameState.currentPhase = GamePhase.ATTACK;
                break;
            }
        }

        return false;
    }
}
