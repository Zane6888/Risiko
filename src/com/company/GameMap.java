package com.company;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GameMap {
    private List<Neighbors> neighbors;
    private List<Continent> continents;

    public List<Continent> getContinents() {
        return continents;
    }

    public GameMap(List<Continent> continents, List<Neighbors> neighbors) {
        this.continents = continents;
        this.neighbors = neighbors;
    }

    public void paint(Graphics2D g, Territory hover, Territory selected) {
        for (Neighbors n : neighbors)
            n.paint(g);

        for (Continent c : continents)
            c.paintComponent(g, hover, selected);
    }

    public Territory getRandomTerritory(Predicate<Territory> p) {
        Continent co = Helper.getRandom(continents.stream().filter(c -> c.containsTerritory(p)).collect(Collectors.toList()));
        if (co == null)
            return null;
        return co.getRandomTerritory(p);
    }

    public boolean containsTerritory(Predicate<Territory> p) {
        for (Continent c : continents)
            if (c.containsTerritory(p))
                return true;
        return false;
    }

    public int countTerritories(Predicate<Territory> p) {
        return continents.stream().mapToInt(c -> c.countTerritories(p)).sum();
    }

    public void updateMonopol(Territory trigger) {
        for (Continent c : continents)
            if (trigger == null || c.containsTerritory(t -> t == trigger))
                c.updateMonopol();
    }

    public List<Territory> getNeighbors(Territory t, Predicate<Territory> p) {
        Set<Territory> l = neighbors.stream().filter(n -> n.neighborOne == t).map(n -> n.neighborTwo).filter(p).collect(Collectors.toSet());
        l.addAll(neighbors.stream().filter(n -> n.neighborTwo == t).map(n -> n.neighborOne).filter(p).collect(Collectors.toList()));
        return new LinkedList<>(l);
    }

    public Territory findTerritory(int x, int y) {
        for (Continent c : continents) {
            Territory t = c.findTerritory(x, y);
            if (t != null)
                return t;
        }
        return null;
    }
}
