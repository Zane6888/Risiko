package com.company;

import java.awt.*;

public class Neighbors {
    public final Territory neighborOne;
    public final Territory neighborTwo;

    private final static BasicStroke dashed =
            new BasicStroke(5.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, new float[]{10.0f}, 0.0f);

    public Neighbors(Territory neighborOne, Territory neighborTwo) {
        this.neighborOne = neighborOne;
        this.neighborTwo = neighborTwo;
    }

    public void paint(Graphics2D g) {
        Point start = neighborOne.getCapitalPosition();
        Point end = neighborTwo.getCapitalPosition();
        if (Math.abs(start.x - end.x) > GameConstants.WINDOW_WIDTH / 2) { //if the start and end point are two far away draw the other side around
            if (start.x < end.x) {
                drawDashedLine(g, start, new Point(-end.x, end.y));
                drawDashedLine(g, end, new Point(GameConstants.WINDOW_WIDTH + start.x, start.y));
            } else {
                drawDashedLine(g, start, new Point(GameConstants.WINDOW_WIDTH + end.x, end.y));
                drawDashedLine(g, end, new Point(-start.x, start.y));
            }
            //TODO IF-Verzeigungen f�r die y-Koordinate fehlen noch
        } else drawDashedLine(g, start, end);
    }

    /**
     * Draws a dashed line for the connection between neighbors
     *
     * @param g     Graphics2D object for drawing
     * @param start Starting point
     * @param end   End point
     */
    private static void drawDashedLine(Graphics2D g, Point start, Point end) {
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(5));
        g.drawLine(start.x, start.y, end.x, end.y);
        g.setColor(new Color(230, 230, 230));
        g.setStroke(dashed);
        g.drawLine(start.x, start.y, end.x, end.y);
    }
}
