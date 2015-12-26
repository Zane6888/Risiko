package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Panel extends JPanel implements MouseListener, MouseMotionListener {
    private List<Continent> continents = new LinkedList<>();  //List if continents containing all the map data
    private List<Neighbors> neighbors = new LinkedList<>(); //List of neighbored territories

    private GameState gameState; //current game state

    private BufferedImage hud; //a buffered image for the HUD

    //A radial gradient paint for the blue background
    private final static RadialGradientPaint backgroundPaint = new RadialGradientPaint(new Point2D.Float(625, 325), 1000, new float[]{0.0f, 0.5f}, new Color[]{new Color(150, 216, 255), new Color(89, 193, 255)});

    /**
     * @param map A string in the given format containing the informations from the map
     */
    public Panel(String map) {
        super(true);
        try {
            continents = MapParser.parseMap(map, neighbors);
        } catch (IOException e) {
            System.out.println("FEHLER: " + e.getMessage()); //TODO Fehler stattdessen auf Panel ausgeben
        }

        //Load the HUD image
        try {
            hud = ImageIO.read(new File("res/hud.png"));
        } catch (IOException e) {
            System.out.println("FEHLER: " + e.getMessage());
        }

        gameState = new GameState(GamePhase.LANDERWERB, 0, 0);

        addMouseListener(this);
        addMouseMotionListener(this);

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g; //generate a Graphics2D to enable antialiasing and make more functionality available

        //Activate antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintBackground(g2);
        paintNeighbors(g2);

        for (Continent c : continents) {
            c.paintComponent(g2);
        }

        paintHUD(g2);
    }

    private void paintHUD(Graphics2D g) {
        g.drawImage(hud, 0, 0, null);
        g.drawString(Integer.toString(gameState.armyPlayer), 193, 46);
        g.drawString(Integer.toString(gameState.armyComputer), 1055, 46);
    }

    private void paintBackground(Graphics2D g) {
        //Draw a blue radiant gradient rectangle as background
        Paint oldPaint = g.getPaint();
        g.setPaint(backgroundPaint);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setPaint(oldPaint);

        //Draw a blue grid
        //TODO Derzeit stimmt das grid nicht zu hundert Prozent mit der map �berein da das Grid schon bei x=0 anf�ngt, die Map erst weiter unten
        g.setColor(new Color(0, 149, 237));
        for (int x = -200; x < 1450; x += 50) g.drawLine(x, 650, MapParser.map(x, 0, 1250, 150, 1150), 0);
        for (int y = 46; y < 650; y += 46) g.drawLine(0, y, 1250, y);


    }

    /**
     * Draws a dashed line for the connection between neighbors
     *
     * @param g     Graphics2D object for drawing
     * @param start Starting point
     * @param end   End point
     */
    private void drawDashedLine(Graphics2D g, Point start, Point end) {
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(5));
        g.drawLine(start.x, start.y, end.x, end.y);
        float dash1[] = {10.0f};
        BasicStroke dashed =
                new BasicStroke(5.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f, dash1, 0.0f);
        g.setColor(new Color(230, 230, 230));
        g.setStroke(dashed);
        g.drawLine(start.x, start.y, end.x, end.y);
    }

    /**
     * Draws all connections between neighbors in the correct way
     *
     * @param g Graphics2D element for drawing
     */
    private void paintNeighbors(Graphics2D g) {
        for (Neighbors n : neighbors) {
            Point start = n.neighborOne.getCapitalPosition();
            Point end = n.neighborTwo.getCapitalPosition();
            if (Math.abs(start.x - end.x) > 625) { //if the start and end point are two far away draw the other side arround
                if (start.x < end.x) {
                    drawDashedLine(g, start, new Point(-end.x, end.y));
                    drawDashedLine(g, end, new Point(1250 + start.x, start.y));
                } else {
                    drawDashedLine(g, start, new Point(1250 + end.x, end.y));
                    drawDashedLine(g, end, new Point(-start.x, start.y));
                }
                //TODO IF-Verzeigungen f�r die y-Koordinate fehlen noch
            } else drawDashedLine(g, start, end);
        }
    }

    public void mousePressed(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
    }

    public void mouseEntered(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
    }

    public void mouseClicked(MouseEvent me) {
        for (Continent c : continents) {
            if (c.mouseClicked(me.getX(), me.getY(), gameState)) {
                this.repaint();
                break;
            }

        }

        if (gameState.currentPhase == GamePhase.LANDERWERBComputer) {
            Collections.shuffle(continents);
            gameState.currentPhase = GamePhase.EROBERUNG;
            for (Continent c : continents) {
                if (c.conquer()) {
                    gameState.currentPhase = GamePhase.LANDERWERB;
                    break;
                }
            }
            gameState.armyComputer = 0;
            gameState.armyPlayer = 0;
            for (Continent c : continents) c.calculateArmies(gameState);
            gameState.armyComputer /= 3;
            gameState.armyPlayer /= 3;
        }

        this.repaint();
    }

    public void mouseMoved(MouseEvent me) {
        for (Continent c : continents) {
            if (c.mouseMoved(me.getX(), me.getY(), gameState)) {
                this.repaint();
                break;
            }

        }
    }

    public void mouseDragged(MouseEvent me) {

    }
}
