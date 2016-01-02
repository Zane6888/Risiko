package com.company;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class GameConstants {
    public final static int WINDOW_WIDTH = 1250;
    public final static int WINDOW_HEIGHT = 650;

    public static final int MAP_HEIGHT = 600; //Height of the tilted map
    public static final int MAP_WIDTH = 1000; //Minimum width of the tilted map

    public static final Color BORDER_COLOR_CONTINENT = new Color(30, 30, 30);
    public static final Color BORDER_COLOR_MONOPOL = Color.YELLOW;

    public static final Color BORDER_COLOR_TERRITORY = new Color(60, 60, 60);
    public static final Color BORDER_COLOR_SELECTED_TERRITORY = Color.YELLOW;

    public static final TexturePaint HATCHED_PAINT = initializeHatchedPaint();

    private static TexturePaint initializeHatchedPaint() {
        BufferedImage bufferedImage =
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setColor(new Color(0.15f, 0.15f, 0.15f));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(0, 10, 10, 0); // /

        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        return new TexturePaint(bufferedImage, rect);
    }

    public static final String DEFAULT_MAP = "maps/world.map";
}
