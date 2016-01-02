package com.company;

public class GameState {
    public int armyPlayer = 0; //number of spare armies possessed by the player
    public int armyComputer = 0; //number of spare armies possessed by the computer
    public GamePhase currentPhase = GamePhase.CLAIM;

    public final GameMap map;

    public GameState(GameMap map) {
        this.map = map;
    }

    public void updateArmy() {
        Territory t;
        for (Continent c : map.getContinents()) {
            t = c.getRandomTerritory(t1 -> true);
            if (t.getArmy() > 0 && !map.containsTerritory(Territory.OWNED_PLAYER.negate()))
                armyPlayer += c.getBonus();
            else if (t.getArmy() < 0 && !map.containsTerritory(Territory.OWNED_COMP.negate()))
                armyComputer += c.getBonus();
        }

        armyPlayer += map.countTerritories(Territory.OWNED_PLAYER) / 3;
        armyComputer += map.countTerritories(Territory.OWNED_COMP) / 3;

        if (armyPlayer == 0)
            armyPlayer = 1;
        if (armyComputer == 0)
            armyComputer = 1;
    }

}
