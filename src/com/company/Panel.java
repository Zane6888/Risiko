package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
    private String won = ""; //Contains the name of the opponent that has won

    private Font phaseFont; //Big font used for writing the current game phase name
    private Font smallFont; //Small font used for writing the armies and messages
    private final static Color hudColor = new Color(0.12f, 0.12f, 0.12f);

    //A radial gradient paint for the blue background
    private final static RadialGradientPaint backgroundPaint = new RadialGradientPaint(new Point2D.Float(625, 325), 1000, new float[]{0.0f, 0.5f}, new Color[]{new Color(150, 216, 255), new Color(89, 193, 255)});

    private boolean errorOccurred = false;  //Boolean indicating whether an error occurred while loading the Panel
    private String errorMessage = ""; //String for the error message if an error occurred while loading

    /**
     * @param map A string in the given format containing the information from the map
     */
    public Panel(String map) {
        super(true);
        try {
            //Load the map
            gameState = new GameState(MapParser.parseMap(map));

            //Load the font
            phaseFont = Font.createFont(Font.TRUETYPE_FONT,
                    new FileInputStream("res/ArchivoBlack.ttf"));
            phaseFont = phaseFont.deriveFont(25F);
            smallFont = phaseFont.deriveFont(16F);

            //Load the HUD image
            hud = ImageIO.read(new File("res/hud.png"));

            button = new JButton("Accept");
            button.setVisible(false);
            button.setPreferredSize(new Dimension(100, 20));

            // button.setBorderPainted(false);
            button.setBackground(new Color(0, 179, 0));
            button.setForeground(Color.WHITE);
            button.setBorder(new BasicBorders.ButtonBorder(hudColor, hudColor, hudColor, hudColor));
            button.setFocusPainted(false);


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
                        button.setVisible(false);
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

            BufferedImage img = ImageIO.read(new File("res/cursor.png"));
            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "risiko_cursor");
            setCursor(cursor);

        } catch (Exception e) {
            errorOccurred = true;
            errorMessage = "An error occurred while loading.\nError Message: " + e.getMessage();
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
        } else {
            drawCenteredText(errorMessage, g2);
        }

    }

    /**
     * Draws the HUD including the number of armies of each opponent and the name of the current game phase
     *
     * @param g GRaphics2D to draw on
     */
    private void paintHUD(Graphics2D g) {
        g.drawImage(hud, 0, 0, null); //Draw the hud

        g.setColor(hudColor);

        //Draw the spare armies of each opponent
        g.setFont(smallFont);
        String playerString = Integer.toString(gameState.armyPlayer);
        String computerString = Integer.toString(gameState.armyComputer);
        g.drawString(playerString, (float) (195 - g.getFontMetrics().getStringBounds(playerString, g).getWidth()), 47);
        g.drawString(computerString, 1055, 47);


        //Draw the current game phase name
        g.setFont(phaseFont);
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
                currentPhase = "GAME OVER";
                break;
        }
        double stringWidth = g.getFontMetrics().getStringBounds(currentPhase, g).getWidth();
        g.drawString(currentPhase, (int) (GameConstants.WINDOW_WIDTH / 2f - stringWidth / 2), 27);

        //Draw which opponent has won in case that the game is over
        if (gameState.currentPhase == GamePhase.GameOver) {
            g.setColor(new Color(1f, 1f, 1f, 0.5f));
            g.fillRect(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
            drawCenteredText(won + " has won!", g);
        }

    }

    /**
     * Paints a blue map grid
     *
     * @param g Graphics2D to draw on
     */
    private void paintBackground(Graphics2D g) {

        //Draw a blue radiant gradient rectangle as background
        Paint oldPaint = g.getPaint(); //save old paint
        g.setPaint(backgroundPaint); //a blue RadiantGradientPaint
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setPaint(oldPaint); //reset paint

        //Draw a blue grid
        g.setColor(new Color(0, 149, 237));
        //Vertical lines
        //Space between x=0 and beginn of map at y=0
        int xSize = (int) ((((GameConstants.WINDOW_WIDTH - GameConstants.MAP_WIDTH) / 2f) / GameConstants.MAP_HEIGHT) * GameConstants.WINDOW_HEIGHT);
        for (int x = -xSize; x < GameConstants.WINDOW_WIDTH + xSize; x += 50)
            g.drawLine(x, GameConstants.WINDOW_HEIGHT, Helper.map(x, 0, GameConstants.WINDOW_WIDTH, xSize, GameConstants.WINDOW_WIDTH - xSize), 0);
        //Horizontal lines
        //Space between each horizontal line
        int ySize = GameConstants.MAP_HEIGHT * 50 / GameConstants.WINDOW_HEIGHT;
        for (int y = ySize; y < GameConstants.WINDOW_HEIGHT; y += ySize)
            g.drawLine(0, y, GameConstants.WINDOW_WIDTH, y);

    }

    /**
     * Draws a string in a centered white rectangle with rounded corners
     *
     * @param string The string that should be drawn
     * @param g      Graphics2D that is drawn on
     */
    private void drawCenteredText(String string, Graphics2D g) {
        g.setFont(smallFont);

        //Split the string into seperate lines
        String[] lines = string.split("\n");

        //Calculate the width of the longest line
        int maxStringWidth = 0;
        for (String line : lines) {
            int lineWidth = (int) g.getFontMetrics().getStringBounds(line, g).getWidth();
            if (lineWidth > maxStringWidth) maxStringWidth = lineWidth;
        }

        //Calculate position and size for the rectangle
        int height = (int) g.getFontMetrics().getStringBounds(string, g).getHeight() * lines.length + 15;
        int width = maxStringWidth + 20;
        int y = (GameConstants.WINDOW_HEIGHT) / 2 - height / 2;
        int x = (GameConstants.WINDOW_WIDTH - width) / 2;


        //Draw the rectangle
        g.setColor(Color.WHITE);
        g.fillOval(x - height / 2, y, height, height);
        g.fillRect(x, y, width, height);
        g.fillOval(x + width - height / 2, y, height, height);

        //Draw the text
        g.setColor(Color.BLACK);
        int yPositionText = y;
        for (String line : lines) {
            Rectangle2D stringDimensions = g.getFontMetrics().getStringBounds(line, g);
            g.drawString(line, GameConstants.WINDOW_WIDTH / 2 - (int) (stringDimensions.getWidth() / 2), yPositionText += stringDimensions.getHeight());
        }

    }


    public void mouseClicked(MouseEvent me) {
        //Only update if no error occured while loading and the game is not over yet
        if (!errorOccurred && gameState.currentPhase != GamePhase.GameOver) {
            if (hoverTerritory != null) {
                switch (gameState.currentPhase) {
                    case CLAIM:
                        //Claim the hoveredTerritory and change the game phase to CLAIMComputer or reinforce if there are
                        //unclaimed territories left
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
                        //Select if territory is owned by the player
                        if (Territory.OWNED_PLAYER.test(hoverTerritory))
                            selectedTerritory = hoverTerritory;
                            //Attack if territory is owned by the computer
                        else {
                            lastFight = new Fight(selectedTerritory, hoverTerritory);
                            //TODO: maybe add some fancy popup/window displaying the fight
                            if (lastFight.apply()) {
                                //Fight has been won
                                gameState.map.updateMonopol(lastFight.getDef());
                                if (checkGameOver())
                                    return;
                                selectedTerritory = hoverTerritory;
                                gameState.currentPhase = GamePhase.FOLLOW;
                            } else {
                                //Fight has not been won
                                selectedTerritory = null;
                                gameState.currentPhase = GamePhase.MOVE;
                                button.setText("End Turn");
                            }
                            hoverTerritory = null;
                            button.setVisible(true);
                        }
                        break;
                    case FOLLOW:
                        if (hoverTerritory != selectedTerritory)
                            break;
                        //Move one army to the new territory
                        if (lastFight.getAtk().getArmy() > 1 && me.getButton() == MouseEvent.BUTTON1) {
                            lastFight.getAtk().addArmy(-1);
                            lastFight.getDef().addArmy(1);
                        }
                        //Move one army to the old territory
                        if (lastFight.getDef().getArmy() > lastFight.getOccupyingArmy()
                                && me.getButton() == MouseEvent.BUTTON3) {
                            lastFight.getAtk().addArmy(1);
                            lastFight.getDef().addArmy(-1);
                        }
                        break;
                    case MOVE:
                        //Select the hovered territory
                        if (selectedTerritory == null) {
                            selectedTerritory = hoverTerritory;
                            break;
                        } else if (me.getButton() == MouseEvent.BUTTON1 && hoverTerritory.getArmy() > 1) {
                            selectedTerritory = hoverTerritory;
                        }
                        //Move army between two territories
                        else if (me.getButton() == MouseEvent.BUTTON3) {
                            //Only change move target and origin if they are not set yet or no armies are moved yet
                            if ((moveTarget == null || moveAmount == 0)
                                    && gameState.map.getNeighbors(selectedTerritory, Territory.OWNED_PLAYER).contains(hoverTerritory)) {
                                moveTarget = hoverTerritory;
                                moveOrigin = selectedTerritory;
                                moveOne(false);
                            }
                            //Move the armies accordingly
                            else if (moveTarget == hoverTerritory && moveOrigin == selectedTerritory) {
                                moveOne(false);
                            } else if (moveOrigin == hoverTerritory && moveTarget == selectedTerritory) {
                                moveOne(true);
                            }
                        }

                }
            } else if (gameState.currentPhase != GamePhase.FOLLOW && moveAmount == 0)
                //Deselect if no territory is hovered
                selectedTerritory = null;


            computer.doTurn(gameState); //let the computer move
            repaint();
        }
    }

    /**
     * Moves one army between moveTarget and moveOrigin
     * @param back if true army is moved from target to origin, from origin to target otherwise
     */
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

    /**
     * Checks if game is over and sets current game phase and the winner accordingly
     * @return returns true iff the game is over
     */
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
        //Only update if no error occurred and game is not over yet
        if (!errorOccurred && gameState.currentPhase != GamePhase.GameOver) {
            Predicate<Territory> hoverable = t -> true;

            //Set hoverable according to current game phase
            switch (gameState.currentPhase) {
                case CLAIM:
                    hoverable = Territory.UNCLAIMED;
                    break;
                case REINFORCE:
                    hoverable = Territory.OWNED_PLAYER;
                    break;
                case ATTACK:
                    if (selectedTerritory != null)
                        //Select all territories from player that can attack
                        //and all territories that are neighbors of selection and owned by computer
                        hoverable = (Territory.CAN_ATTACK.and(Territory.OWNED_PLAYER))
                                .or(t -> gameState.map.getNeighbors(selectedTerritory, Territory.OWNED_COMP).contains(t));
                    else
                        //Select all territories from player that can attack and have neighbors that
                        //are owned by the computer
                        hoverable = Territory.CAN_ATTACK.and(Territory.OWNED_PLAYER)
                                .and(t -> gameState.map.getNeighbors(t, Territory.OWNED_COMP).size() > 0);
                    break;
                case FOLLOW:
                    hoverable = t -> t == selectedTerritory;
                    break;
                case MOVE:
                    //Only if no territory is selected and no army has been moved yet
                    if (selectedTerritory == null && moveAmount == 0)
                        hoverable = t -> t.getArmy() > 1
                                && gameState.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0;
                        //If move origin has already been set via selectedTerritory
                    else if (moveTarget == null || moveAmount == 0)
                        hoverable = t -> (t.getArmy() > 1
                                && gameState.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0)
                                || gameState.map.getNeighbors(selectedTerritory, Territory.OWNED_PLAYER).contains(t);
                        //If move origin and target have been selected
                    else
                        hoverable = t -> t == moveTarget || t == moveOrigin;
            }

            //Test hovered territory if it is hoverable
            Territory old = hoverTerritory;
            hoverTerritory = gameState.map.findTerritory(me.getX(), me.getY());
            if (hoverTerritory != null && !hoverable.test(hoverTerritory))
                hoverTerritory = null;
            if (old != hoverTerritory) //only repaint if hovered territory has changed
                repaint();
        }
    }

    public void mouseDragged(MouseEvent me) {
    }

    public void mousePressed(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
    }

    public void mouseEntered(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) { }
}
