package com.company;

import java.awt.*;
import java.util.List;

public class GameMap {
    private List<Neighbors> neighbors;
    private List<Continent> continents;

    public List<Continent> getContinents() {
        return continents;
    }

    public List<Neighbors> getNeighbors() {
        return neighbors;
    }

    public GameMap(List<Continent> continents, List<Neighbors> neighbors) {
        this.continents = continents;
        this.neighbors = neighbors;
    }

    public void paint(Graphics2D g) {
        for (Neighbors n : neighbors)
            n.paint(g);

        for (Continent c : continents)
            c.paintComponent(g);
    }
}
