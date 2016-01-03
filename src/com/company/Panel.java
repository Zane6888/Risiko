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
import java.io.FileInputStream;
import java.util.function.Predicate;

public class Panel extends JPanel implements MouseListener, MouseMotionListener {
    private JButton button;

    private Computer computer = new Computer();

    private GameState gameState; //current game state
    private Territory hoverTerritory;
    private Territory selectedTerritory;

    private Territory moveTarget;
    private Territory moveOrigin;
    private int moveAmount = 0;

    private Fight lastFight;

    private BufferedImage hud; //a buffered image for the HUD
    private String won = "";

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
            gameState = new GameState(MapParser.parseMap(map));

            //Load the font
            mainFont = Font.createFont(Font.TRUETYPE_FONT,
                    new FileInputStream("res/ArchivoBlack.ttf"));
            mainFont = mainFont.deriveFont(25F);
            smallFont = mainFont.deriveFont(16F);

            //Load the HUD image
            hud = ImageIO.read(new File("res/hud.png"));

            //TODO: Decide on positioning, add actual design
            button = new JButton("Accept");
            button.setVisible(true);
            button.setPreferredSize(new Dimension(100, 20));
            button.addActionListener(e -> {
                switch (gameState.currentPhase) {
                    case FOLLOW:
                        button.setText("End Turn");
                        selectedTerritory = null;
                        gameState.currentPhase = GamePhase.MOVE;
                        this.repaint();
                        break;
                    case MOVE:
                        button.setText("Accept");
                        selectedTerritory = null;
                        moveAmount = 0;
                        moveTarget = null;
                        moveOrigin = null;
                        gameState.currentPhase = GamePhase.ATTACKComputer;
                        lastFight = computer.attack(gameState);
                        //TODO: maybe add some fancy popup/window displaying the fight
                        if (lastFight != null) {
                            if (lastFight.apply()) {
                                gameState.map.updateMonopol(lastFight.getDef());
                                if (checkGameOver())
                                    return;
                                gameState.currentPhase = GamePhase.FOLLOWComputer;
                            } else
                                gameState.currentPhase = GamePhase.MOVEComputer;
                        } else {
                            gameState.currentPhase = GamePhase.MOVEComputer;
                        }
                        computer.doPostAttack(gameState, lastFight);
                        this.repaint();
                }
            });
            setLayout(new BorderLayout());
            JPanel p = new JPanel();
            p.setBackground(new Color(0, 0, 0, 0));
            p.add(button);
            add(p, BorderLayout.SOUTH);

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
            gameState.map.paint(g2, hoverTerritory, selectedTerritory);
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
            case MOVE:
                currentPhase = "MOVE";
                break;
            case FOLLOW:
                currentPhase = "FOLLOW";
                break;
            case GameOver:
                currentPhase = won + " WON!";
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
        g.setColor(new Color(0, 149, 237));
        //Vertical lines
        int xSize = (int) ((((GameConstants.WINDOW_WIDTH - GameConstants.MAP_WIDTH) / 2f) / GameConstants.MAP_HEIGHT) * GameConstants.WINDOW_HEIGHT);
        for (int x = -xSize; x < GameConstants.WINDOW_WIDTH + xSize; x += 50)
            g.drawLine(x, GameConstants.WINDOW_HEIGHT, Helper.map(x, 0, GameConstants.WINDOW_WIDTH, xSize, GameConstants.WINDOW_WIDTH - xSize), 0);
        //Horizontal lines
        int ySize = GameConstants.MAP_HEIGHT * 50 / GameConstants.WINDOW_HEIGHT;
        for (int y = ySize; y < GameConstants.WINDOW_HEIGHT; y += ySize)
            g.drawLine(0, y, GameConstants.WINDOW_WIDTH, y);


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
        if (!errorOccurred && gameState.currentPhase != GamePhase.GameOver) {
            if (hoverTerritory != null) {
                switch (gameState.currentPhase) {
                    case CLAIM:
                        hoverTerritory.setArmy(1);
                        gameState.map.updateMonopol(hoverTerritory);
                        if (gameState.map.containsTerritory(Territory.UNCLAIMED))
                            gameState.currentPhase = GamePhase.CLAIMComputer;
                        else {
                            gameState.currentPhase = GamePhase.REINFORCE;
                            gameState.updateArmy();
                        }
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
                            lastFight = new Fight(selectedTerritory, hoverTerritory);
                            //TODO: maybe add some fancy popup/window displaying the fight
                            if (lastFight.apply()) {
                                gameState.map.updateMonopol(lastFight.getDef());
                                if (checkGameOver())
                                    return;
                                selectedTerritory = hoverTerritory;
                                gameState.currentPhase = GamePhase.FOLLOW;
                            } else {
                                selectedTerritory = null;
                                gameState.currentPhase = GamePhase.MOVE;
                                button.setText("End Turn");
                            }
                            button.setVisible(true);
                        }
                        break;
                    case FOLLOW:
                        if (hoverTerritory != selectedTerritory)
                            break;
                        if (lastFight.getAtk().getArmy() > 1 && me.getButton() == MouseEvent.BUTTON1) {
                            lastFight.getAtk().addArmy(-1);
                            lastFight.getDef().addArmy(1);
                        }
                        if (lastFight.getDef().getArmy() > lastFight.getOccupyingArmy()
                                && me.getButton() == MouseEvent.BUTTON3) {
                            lastFight.getAtk().addArmy(1);
                            lastFight.getDef().addArmy(-1);
                        }
                        break;
                    case MOVE:
                        if (selectedTerritory == null) {
                            selectedTerritory = hoverTerritory;
                            break;
                        } else if (me.getButton() == MouseEvent.BUTTON1 && hoverTerritory.getArmy() > 1) {
                            selectedTerritory = hoverTerritory;
                        } else if (me.getButton() == MouseEvent.BUTTON3) {
                            if ((moveTarget == null || moveAmount == 0)
                                    && gameState.map.getNeighbors(selectedTerritory, Territory.OWNED_PLAYER).contains(hoverTerritory)) {
                                moveTarget = hoverTerritory;
                                moveOrigin = selectedTerritory;
                                moveOne(false);
                            } else if (moveTarget == hoverTerritory && moveOrigin == selectedTerritory) {
                                moveOne(false);
                            } else if (moveOrigin == hoverTerritory && moveTarget == selectedTerritory) {
                                moveOne(true);
                            }
                        }

                }
            } else if (gameState.currentPhase != GamePhase.FOLLOW && moveAmount == 0)
                selectedTerritory = null;


            computer.doTurn(gameState);
            repaint();
        }
    }

    private void moveOne(boolean back) {
        if (back) {
            if (moveTarget.getArmy() > 1) {
                moveAmount--;
                moveTarget.addArmy(-1);
                moveOrigin.addArmy(1);
                if (moveAmount == 0) {
                    moveTarget = null;
                    moveOrigin = null;
                    selectedTerritory = null;
                }
            }
        } else {
            if (moveOrigin.getArmy() > 1) {
                moveAmount++;
                moveTarget.addArmy(1);
                moveOrigin.addArmy(-1);
            }
        }
    }

    private boolean checkGameOver() {
        if (gameState.map.containsTerritory(Territory.UNCLAIMED))
            return false;
        boolean player = gameState.map.containsTerritory(Territory.OWNED_PLAYER);
        boolean comp = gameState.map.containsTerritory(Territory.OWNED_COMP);
        if (player && comp)
            return false;

        won = player ? "PLAYER" : "COMPUTER";

        gameState.currentPhase = GamePhase.GameOver;
        selectedTerritory = null;
        repaint();
        return true;
    }

    public void mouseMoved(MouseEvent me) {
        if (!errorOccurred && gameState.currentPhase != GamePhase.GameOver) {
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
                        hoverable = (Territory.CAN_ATTACK.and(Territory.OWNED_PLAYER))
                                .or(t -> gameState.map.getNeighbors(selectedTerritory, p -> true).contains(t));
                    else
                        hoverable = Territory.CAN_ATTACK.and(Territory.OWNED_PLAYER)
                                .and(t -> gameState.map.getNeighbors(t, Territory.OWNED_COMP).size() > 0);
                    break;
                case FOLLOW:
                    hoverable = t -> t == selectedTerritory;
                    break;
                case MOVE:
                    if (selectedTerritory == null && moveAmount == 0)
                        hoverable = t -> t.getArmy() > 1
                                && gameState.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0;
                    else if (moveTarget == null || moveAmount == 0)
                        hoverable = t -> (t.getArmy() > 1
                                && gameState.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0)
                                || gameState.map.getNeighbors(selectedTerritory, Territory.OWNED_PLAYER).contains(t);
                    else
                        hoverable = t -> t == moveTarget || t == moveOrigin;
            }

            Territory old = hoverTerritory;
            hoverTerritory = gameState.map.findTerritory(me.getX(), me.getY());
            if (hoverTerritory != null && !hoverable.test(hoverTerritory))
                hoverTerritory = null;
            if (old != hoverTerritory)
                repaint();
        }
    }

    public void mouseDragged(MouseEvent me) {

    }
}
