package com.company;

import java.io.Serializable;

public class PanelData implements Serializable {
    public GameState gameState; //current game state

    public Territory hoverTerritory;
    public Territory selectedTerritory;

    public Territory moveTarget;
    public Territory moveOrigin;
    public int moveAmount = 0;

    public Fight lastFight;
}
