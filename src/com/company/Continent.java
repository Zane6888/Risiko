package com.company;

import java.awt.*;
import java.awt.geom.Area;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Continent implements Serializable {
    private List<Territory> territories = new LinkedList<>();
    private int bonus; //number of bonus army when the whole continent is owned by one player
    private transient Area borders; //combined area of all territories of the continent

    private boolean isMonopolPlayer, isMonopolComp; //two booleans that save if the whole continent is owned by the player or the computer

    public boolean isMonopolPlayer() {
        return isMonopolPlayer;
    }

    public boolean isMonopolComp() {
        return isMonopolComp;
    }

    public int getBonus() {
        return bonus;
    }

    public Continent(List<Territory> territories, int bonus) {
        this.territories = territories;
        this.bonus = bonus;
        borders = new Area();
        for (Territory t : territories) borders.add(t.getArea());
    }

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(territories);
        stream.writeInt(bonus);
        stream.writeBoolean(isMonopolPlayer);
        stream.writeBoolean(isMonopolComp);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        territories = (List<Territory>) stream.readObject();
        bonus = stream.readInt();
        isMonopolPlayer = stream.readBoolean();
        isMonopolComp = stream.readBoolean();

        borders = new Area();
        for (Territory t : territories) borders.add(t.getArea());
    }


    public void paintComponent(Graphics2D g, Territory hoverTerritory, Territory selectedTerritory) {
        //draws a yellow border arround the continent if it is owned by a single opponent
        //a black border is drawn otherwise
        if (isMonopolComp || isMonopolPlayer) {
            g.setColor(GameConstants.BORDER_COLOR_MONOPOL);
            g.setStroke(new BasicStroke(7));
        } else {
            g.setColor(GameConstants.BORDER_COLOR_CONTINENT);
            g.setStroke(new BasicStroke(5));
        }

        if (borders != null) g.draw(borders);

        //Draw all  territories
        for (Territory t : territories) t.paintTerritory(g, t == hoverTerritory, t == selectedTerritory);

        //Draw the capital of each territory
        for (Territory t : territories) t.paintCapitals(g);

    }

    /**
     * Searches for the Territory occupying the point (x,y)
     *
     * @param x         x coordinate
     * @param y         y coordinate
     * @return The Territory under the point (x,y) or null if no such Territory is found
     */
    public Territory findTerritory(int x, int y) {
        if (borders.contains(x, y))   //if the mouse is within the borders of the continent
            for (Territory t : territories)
                if (t.contains(x, y))
                    return t;
        return null;
    }

    /**
     * Updates the variables isMonopolPlayer and isMonopolComp
     */
    public void updateMonopol() {
        isMonopolPlayer = countTerritories(Territory.OWNED_PLAYER) == territories.size();
        isMonopolComp = !isMonopolPlayer && countTerritories(Territory.OWNED_COMP) == territories.size();
    }

    public Territory getRandomTerritory(Predicate<Territory> p) {
        return Helper.getRandom(territories.stream().filter(p).collect(Collectors.toList()));
    }

    public boolean containsTerritory(Predicate<Territory> p) {
        return territories.stream().filter(p).findAny().isPresent();
    }

    public int countTerritories(Predicate<Territory> p) {
        return (int) territories.stream().filter(p).count();
    }
}
