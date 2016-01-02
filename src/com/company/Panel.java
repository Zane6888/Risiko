package com.company;

import com.sun.javaws.exceptions.InvalidArgumentException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.function.Predicate;

public class Panel extends JPanel implements MouseListener, MouseMotionListener {

    private GameMap map;

    private GameState gameState; //current game state
    private Territory hoverTerritory;
    private Territory selectedTerritory;

    private BufferedImage hud; //a buffered image for the HUD

    private Font mainFont;
    private Font smallFont;
    private final static Color hudColor = new Color(0.12f, 0.12f, 0.12f);

    //A radial gradient paint for the blue background
    private final static RadialGradientPaint backgroundPaint = new RadialGradientPaint(new Point2D.Float(625, 325), 1000, new float[]{0.0f, 0.5f}, new Color[]{new Color(150, 216, 255), new Color(89, 193, 255)});

    private boolean errorOccurred = false;

    /**
     * @param map A string in the given format containing the information from the map
     */
    public Panel(String map) {
        super(true);
        try {
            //Load the map
            if (!map.equals("")) this.map = MapParser.parseMap(map);
            else throw new InvalidArgumentException(new String[]{"The given map is empty"});

            //Load the font
            mainFont = Font.createFont(Font.TRUETYPE_FONT,
                    new FileInputStream("res/ArchivoBlack.ttf"));
            mainFont = mainFont.deriveFont(25F);
            smallFont = mainFont.deriveFont(16F);

            //Load the HUD image
            hud = ImageIO.read(new File("res/hud.png"));


        } catch (Exception e) {
            errorOccurred = true;

            System.out.println("FEHLER: " + e.getMessage());
            JLabel errorLabel = new JLabel("<html><body>An error occurred while loading.<br>Error Message: " + e.getMessage() + "</body><html>", SwingConstants.CENTER);

            this.add(errorLabel, BorderLayout.CENTER);

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(Box.createHorizontalGlue());
            add(errorLabel);
            add(Box.createHorizontalGlue());
        }

        gameState = new GameState(GamePhase.CLAIM, 0, 0);

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

        if (!errorOccurred) {
            map.paint(g2, hoverTerritory, selectedTerritory);
            paintHUD(g2);
        }


    }

    private void paintHUD(Graphics2D g) {
        g.drawImage(hud, 0, 0, null);

        g.setColor(hudColor);

        //Draw the spare armies of each opponent
        g.setFont(smallFont);
        String playerString = Integer.toString(gameState.armyPlayer);
        String computerString = Integer.toString(gameState.armyComputer);
        g.drawString(playerString, (float) (195 - g.getFontMetrics().getStringBounds(playerString, g).getWidth()), 47);
        g.drawString(computerString, 1055, 47);


        //Draw the current game phase
        g.setFont(mainFont);
        String currentPhase = "";
        switch (gameState.currentPhase) {
            case CLAIM:
            case CLAIMComputer:
                currentPhase = "CLAIM";
                break;
            case REINFORCE:
            case REINFORCEComputer:
                currentPhase = "REINFORCE";
                break;
            case ATTACK:
            case ATTACKComputer:
                currentPhase = "ATTACK";
                break;
        }
        double stringWidth = g.getFontMetrics().getStringBounds(currentPhase, g).getWidth();
        g.drawString(currentPhase, (int) (GameConstants.WINDOW_WIDTH / 2f - stringWidth / 2), 27);
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

    public void mousePressed(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
    }

    public void mouseEntered(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
    }

    public void mouseClicked(MouseEvent me) {
        if (!errorOccurred) {
            if (hoverTerritory != null) {
                switch (gameState.currentPhase) {
                    case CLAIM:
                        hoverTerritory.setArmy(1);
                        map.updateMonopol(hoverTerritory);
                        gameState.currentPhase = GamePhase.CLAIMComputer;
                        break;
                    case REINFORCE:
                        hoverTerritory.addArmy(1);
                        gameState.armyPlayer--;
                        if (gameState.armyPlayer <= 0) gameState.currentPhase = GamePhase.REINFORCEComputer;
                        break;
                    case ATTACK:
                        if (Territory.OWNED_PLAYER.test(hoverTerritory))
                            selectedTerritory = hoverTerritory;
                        else {
                            //TODO:   implement Fight
                        }
                        break;
                }
            } else
                selectedTerritory = null;

            Computer.move(gameState, map);
            repaint();
        }
    }

    public void mouseMoved(MouseEvent me) {
        if (!errorOccurred) {
            Predicate<Territory> hoverable = t -> true;

            switch (gameState.currentPhase) {
                case CLAIM:
                    hoverable = Territory.UNCLAIMED;
                    break;
                case REINFORCE:
                    hoverable = Territory.OWNED_PLAYER;
                    break;
                case ATTACK:
                    if (selectedTerritory != null)
                        hoverable = Territory.OWNED_PLAYER.or(t -> map.getNeighbors(selectedTerritory, p -> true).contains(t));
                    else
                        hoverable = Territory.OWNED_PLAYER;
            }

            Territory old = hoverTerritory;
            hoverTerritory = map.findTerritory(me.getX(), me.getY());
            if (hoverTerritory != null && !hoverable.test(hoverTerritory))
                hoverTerritory = null;
            if (old != hoverTerritory)
                repaint();
        }
    }

    public void mouseDragged(MouseEvent me) {

    }
}
