package com.company;

public class GameState {
    public int reinforcementPlayer = 0; //number of spare armies possessed by the player
    public int reinforcementComputer = 0; //number of spare armies possessed by the computer

    public int territoriesPlayer = 0; //number of territories possessed by the player
    public int territoriesComputer = 0; //number of territories possessed by the computer

    //TODO maybe also update and draw the sum of all armies too
    public int armyPlayer = 0; //sum of all armies of the player
    public int armyComputer = 0; //sum of all armies of the computer

    public int continentsPlayer = 0;  //number of continents possessed by the player
    public int continentsComputer = 0; //number of continents possessed by the computer

    public void updateCounters() {
        territoriesPlayer = 0;
        territoriesComputer = 0;
        continentsPlayer = 0;
        continentsComputer = 0;
        for (Continent c : map.getContinents()) {
            territoriesPlayer += c.countTerritories(t -> t.getArmy() > 0);
            territoriesComputer += c.countTerritories(t -> t.getArmy() < 0);
            if (c.isMonopolPlayer()) continentsPlayer++;
            if (c.isMonopolComp()) continentsComputer++;
        }
    }

    public GamePhase currentPhase = GamePhase.CLAIM;

    public final GameMap map;

    public GameState(GameMap map) {
        this.map = map;
    }

    public void updateArmy() {
        Territory t;
        for (Continent c : map.getContinents()) {
            t = c.getRandomTerritory(t1 -> true);
            if (t.getArmy() > 0 && !c.containsTerritory(Territory.OWNED_PLAYER.negate()))
                reinforcementPlayer += c.getBonus();
            else if (t.getArmy() < 0 && !c.containsTerritory(Territory.OWNED_COMP.negate()))
                reinforcementComputer += c.getBonus();
        }

        reinforcementPlayer += map.countTerritories(Territory.OWNED_PLAYER) / 3;
        reinforcementComputer += map.countTerritories(Territory.OWNED_COMP) / 3;

        if (reinforcementPlayer == 0)
            reinforcementPlayer = 1;
        if (reinforcementComputer == 0)
            reinforcementComputer = 1;
    }

}
