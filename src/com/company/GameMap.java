package com.company;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public Territory getRandomTerritory(Predicate<Territory> p) {
        return Helper.getRandom(continents.stream().filter(c -> c.containsTerritory(p)).collect(Collectors.toList())).getRandomTerritory(p);
    }

    public boolean containsTerritory(Predicate<Territory> p) {
        for (Continent c : continents)
            if (c.containsTerritory(p))
                return true;
        return false;
    }
}
