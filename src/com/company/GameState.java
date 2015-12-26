package com.company;

public class GameState {
    public int armyPlayer; //number of spare armies possessed by the player
    public int armyComputer; //number of spare armies possessed by the computer
    public GamePhase currentPhase;

    public GameState(GamePhase currentPhase, int armyPlayer, int armyComputer) {
        this.currentPhase = currentPhase;
        this.armyPlayer = armyPlayer;
        this.armyComputer = armyComputer;
    }
}
