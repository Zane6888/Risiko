package com.company;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a territory with an capital and armies
 */
public class Territory implements Serializable {
    public static final Predicate<Territory> UNCLAIMED = t -> t.getArmy() == 0;
    public static final Predicate<Territory> OWNED_COMP = t -> t.getArmy() < 0;
    public static final Predicate<Territory> OWNED_PLAYER = t -> t.getArmy() > 0;
    public static final Predicate<Territory> CAN_ATTACK = t -> t.getArmy() > 1 || t.getArmy() < -1;

    private List<Polygon> patches = new LinkedList<>();
    private Color color; //color of the territory
    private int army; //number of army, negative numbers are for the computer, positive for the player

    public int getArmy() {
        return army;
    }

    public void setArmy(int army) {
        if (Math.signum(army) != Math.signum(this.army)) { //update Color iff possession has changed
            this.army = army;
            generateColor();
        } else this.army = army;
    }

    public void addArmy(int army) {
        this.army += this.army < 0 ? -army : army;
    }

    /**
     * @return returns an Area containing all the patches of the territory
     */
    public Area getArea() {
        Area area = new Area();
        for (Polygon p : patches) area.add(new Area(p));
        return area;
    }

    private String name; //Name of the territory

    public String getName() {
        return name;
    }

    private Point capital; //Position of the capital

    public Point getCapitalPosition() {
        return capital;
    }

    public Territory(List<Polygon> patches, String name, Point capital) {
        this.patches = patches;

        army = 0;
        this.name = name;
        this.capital = capital;
        generateColor();
    }

    /**
     * Generates a new random color, a red tone for the player, a blue tone for the computer and a gray tone for unconquered territories
     */
    public void generateColor() {
        if (army < 0)
            color = Color.getHSBColor(0f, 1 - (float) Math.random() * 0.2f, 0.5f + (float) Math.random() * 0.5f);
        else if (army > 0)
            color = Color.getHSBColor(0.7f - (float) Math.random() * 0.2f, 1 - (float) Math.random() * 0.2f, 0.4f + (float) Math.random() * 0.25f);
        else color = Color.getHSBColor(0f, 0, 0.55f + (float) Math.random() * 0.25f);
    }

    /**
     * Draws the territory
     *
     * @param g        Graphics2D object to draw on
     * @param hovered a boolean indicating whether the territory should be highlighted or not
     * @param  selected if true territory is drawn hatched
     */
    public void paintTerritory(Graphics2D g, boolean hovered, boolean selected) {
        //If selected lighten up the color
        if (hovered) {
            g.setColor(Helper.lightenColor(color, 50));
        } else g.setColor(color);

        patches.forEach(g::fillPolygon);

        if (selected) {
            g.setPaint(GameConstants.HATCHED_PAINT);
            patches.forEach(g::fillPolygon);
        }

        //Draw the border of the territory
        g.setColor(GameConstants.BORDER_COLOR_TERRITORY);
        g.setStroke(new BasicStroke(1.5f));
        patches.forEach(g::drawPolygon);
    }

    /**
     * Draws the capital with the number of armies
     *
     * @param g Graphics2D object to be draw on
     */
    public void paintCapitals(Graphics2D g) {
        g.setColor(new Color(1f, 1f, 1f, 0.4f));
        g.fillOval(capital.x - 13, capital.y - 13, 26, 26);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f));
        g.drawOval(capital.x - 13, capital.y - 13, 26, 26);
        String toPrint = ((Integer) Math.abs(army)).toString();
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(toPrint, g);
        g.drawString(toPrint, capital.x - (float) (stringBounds.getWidth() / 2), capital.y - 2 + (float) (stringBounds.getHeight() / 2f));
    }

    /**
     * Determines whether a Point is within a territory or not
     *
     * @param x x Position
     * @param y y Position
     * @return true iff territory contains point
     */
    public boolean contains(int x, int y) {
        for (Polygon p : patches) {
            if (p.contains(x, y)) return true;
        }
        return false;
    }
}
