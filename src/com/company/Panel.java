package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.function.Predicate;

public class Panel extends JPanel implements MouseListener, MouseMotionListener, Serializable {
    private JButton button;

    private Computer computer = new Computer();

    private PanelData data;

    private BufferedImage hud; //a buffered image for the HUD
    private String won = ""; //Contains the name of the opponent that has won

    private Font phaseFont; //Big font used for writing the current game phase name
    private Font smallFont; //Small font used for writing the armies and messages
    private Font hoverFont; //small font used to write additional information

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

        Initialize();

        data = new PanelData();

        //Load the map
        try {
            data.gameState = new GameState(MapParser.parseMap(map));
        } catch (Exception e) {
            errorOccurred = true;
            errorMessage = "An error occurred while loading.\nError Message: " + e.getMessage();
        }
    }

    /**
     * Tries to create a new panel and deserialize the date from 'saves/game.ser'
     */
    public Panel() throws IOException, ClassNotFoundException {
        super(true);
        Initialize();

        FileInputStream fis = new FileInputStream("saves/game.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        data = (PanelData) ois.readObject();
        ois.close();
        fis.close();

        //Unhide the button if necessary
        if (data.gameState.currentPhase == GamePhase.REINFORCE || data.gameState.currentPhase == GamePhase.MOVE
                || data.gameState.currentPhase == GamePhase.FOLLOW)
            button.setVisible(true);

    }

    /**
     * Initializes fonts, hud, button, mouse listeners
     */
    private void Initialize() {
        try {

            //Load the font
            phaseFont = Font.createFont(Font.TRUETYPE_FONT,
                    new FileInputStream("res/ArchivoBlack.ttf"));
            phaseFont = phaseFont.deriveFont(25F);
            smallFont = phaseFont.deriveFont(16F);
            hoverFont = phaseFont.deriveFont(14F);

            //Load the HUD image
            hud = ImageIO.read(new File("res/hud.png"));

            //Set the cursor
            BufferedImage img = ImageIO.read(new File("res/cursor.png"));
            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "risiko_cursor");
            setCursor(cursor);

        } catch (Exception e) {
            errorOccurred = true;
            errorMessage = "An error occurred while loading.\nError Message: " + e.getMessage();
        }

        //Add the button
        button = new JButton("Accept");
        button.setVisible(false);
        button.setPreferredSize(new Dimension(100, 20));

        button.setBackground(new Color(0, 179, 0));
        button.setForeground(Color.WHITE);
        button.setBorder(new BasicBorders.ButtonBorder(hudColor, hudColor, hudColor, hudColor));
        button.setFocusPainted(false);


        button.addActionListener(e -> {
            switch (data.gameState.currentPhase) {
                case FOLLOW:
                    button.setText("End Turn");
                    data.selectedTerritory = null;
                    data.gameState.currentPhase = GamePhase.MOVE;
                    this.repaint();
                    break;
                case MOVE:
                    button.setText("Accept");
                    button.setVisible(false);
                    data.selectedTerritory = null;
                    data.moveAmount = 0;
                    data.moveTarget = null;
                    data.moveOrigin = null;
                    data.gameState.currentPhase = GamePhase.ATTACKComputer;
                    data.lastFight = computer.attack(data.gameState);
                    //TODO: maybe add some fancy popup/window displaying the fight
                    if (data.lastFight != null) {
                        if (data.lastFight.apply()) {
                            data.gameState.map.updateMonopol(data.lastFight.getDef());
                            if (checkGameOver())
                                return;
                            data.gameState.currentPhase = GamePhase.FOLLOWComputer;
                        } else
                            data.gameState.currentPhase = GamePhase.MOVEComputer;
                    } else {
                        data.gameState.currentPhase = GamePhase.MOVEComputer;
                    }
                    computer.doPostAttack(data.gameState, data.lastFight);
                    this.repaint();
            }
        });
        setLayout(new BorderLayout());
        JPanel p = new JPanel();
        p.setBackground(new Color(0, 0, 0, 0));
        p.add(button);
        add(p, BorderLayout.SOUTH);

        //Add the mouse listeners
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
            data.gameState.map.paint(g2, data.hoverTerritory, data.selectedTerritory);
            paintHUD(g2);

            //Draw the arrow to indicate movement
            if (data.hoverTerritory != null) {
                switch (data.gameState.currentPhase) {
                    case ATTACK:
                        if (data.selectedTerritory != null && data.hoverTerritory.getArmy() < 0) {
                            drawArrow(g2, Color.YELLOW, data.selectedTerritory.getCapitalPosition(), data.hoverTerritory.getCapitalPosition());
                        }
                        break;
                }
            }


            //Draw the name of the hovered territory at the top
            String info = "";
            if (data.hoverTerritory != null) {
                switch (data.gameState.currentPhase) {
                    case ATTACK:
                        if (data.hoverTerritory.getArmy() > 0) info = "select ";
                        else info = "attack ";
                    default:
                        info += data.hoverTerritory.getName();
                }

            } else {
                switch (data.gameState.currentPhase) {
                    case REINFORCE:
                        info = data.gameState.reinforcementPlayer + " reinforcements left";
                }
            }

            g.setColor(hudColor);
            g.setFont(hoverFont);

            double stringWidth = g.getFontMetrics().getStringBounds(info, g).getWidth();
            g.drawString(info, (int) (GameConstants.WINDOW_WIDTH / 2f - stringWidth / 2), 55);



        } else {
            drawCenteredText(errorMessage, g2);
        }

    }

    /**
     * Draws the HUD including the number of armies of each opponent and the name of the current game phase
     *
     * @param g Graphics2D to draw on
     */
    private void paintHUD(Graphics2D g) {
        g.drawImage(hud, 0, 0, null); //Draw the hud

        g.setColor(hudColor);

        //Draw the spare armies of each opponent
        g.setFont(smallFont);
        data.gameState.updateCounters(); //update the stats before drawing them
        String territoriesPlayer = Integer.toString(data.gameState.territoriesPlayer);
        String territoriesComputer = Integer.toString(data.gameState.territoriesComputer);
        String continentsPlayer = Integer.toString(data.gameState.continentsPlayer);
        String continentsComputer = Integer.toString(data.gameState.continentsComputer);
        g.drawString(territoriesPlayer, (float) (195 - g.getFontMetrics().getStringBounds(territoriesPlayer, g).getWidth()), 47);
        g.drawString(territoriesComputer, GameConstants.WINDOW_WIDTH - 195, 47);
        g.drawString(continentsPlayer, (float) (100 - g.getFontMetrics().getStringBounds(continentsPlayer, g).getWidth()), 47);
        g.drawString(continentsComputer, GameConstants.WINDOW_WIDTH - 100, 47);


        //Draw the current game phase name
        g.setFont(phaseFont);
        String currentPhase = "";
        switch (data.gameState.currentPhase) {
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
        if (data.gameState.currentPhase == GamePhase.GameOver) {
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

    /**
     * Draws an arrow to symbolise movement between territories
     * Shortens it by 13 px of length on both sides to not overlap the capitals
     *
     * @param g Graphics2D that is drawn on
     * @param c Color the arrow is drawn in
     */
    private void drawArrow(Graphics2D g, Color c, Point from, Point to) {

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 20);
        arrowHead.addPoint(-10, 0);
        arrowHead.addPoint(10, 0);


        Point vector = new Point(to.x - from.x, to.y - from.y); //vector of the arrow
        double vectorLenght = Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2));
        Point2D.Double unitVector = new Point2D.Double(vector.x / vectorLenght, vector.y / vectorLenght); //unit vector


        //Shorten the arrow
        to = new Point(to.x - (int) (unitVector.x * 20), to.y - (int) (unitVector.y * 20)); //shorten the line to fit the arrow head
        to = new Point(to.x - (int) (unitVector.x * 13), to.y - (int) (unitVector.y * 13)); //shorten the line at the end to not overlap capital
        from = new Point(from.x + (int) (unitVector.x * 13), from.y + (int) (unitVector.y * 13)); //shorten the line at the end to not overlap capital


        //The head is drawn to a new Graphics2D object in order to rotate it with an AffineTransform

        AffineTransform at = new AffineTransform(); //AffineTransform to rotate the arrow head
        at.setToIdentity();
        double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX()); //angle of the arrow
        at.translate(to.x, to.y); //Move the arrow head to the end point
        at.rotate((angle - Math.PI / 2d)); //Rotate the arrow

        //Draw the line of the arrow
        g.setColor(c);
        g.setStroke(new BasicStroke(7));
        g.drawLine(from.x, from.y, to.x, to.y);

        Graphics2D g2 = (Graphics2D) g.create(); //new Graphics2D for the rotation
        g2.setTransform(at);
        g2.fill(arrowHead);
        g2.dispose();
    }


    public void mouseClicked(MouseEvent me) {
        //Only update if no error occured while loading and the game is not over yet
        if (!errorOccurred && data.gameState.currentPhase != GamePhase.GameOver) {
            if (data.hoverTerritory != null) {
                switch (data.gameState.currentPhase) {
                    case CLAIM:
                        //Claim the hoveredTerritory and change the game phase to CLAIMComputer or reinforce if there are
                        //unclaimed territories left
                        data.hoverTerritory.setArmy(1);
                        data.gameState.map.updateMonopol(data.hoverTerritory);
                        if (data.gameState.map.containsTerritory(Territory.UNCLAIMED))
                            data.gameState.currentPhase = GamePhase.CLAIMComputer;
                        else {
                            data.gameState.currentPhase = GamePhase.REINFORCE;
                            data.gameState.updateArmy();
                        }
                        break;
                    case REINFORCE:
                        data.hoverTerritory.addArmy(1);
                        data.gameState.reinforcementPlayer--;
                        if (data.gameState.reinforcementPlayer <= 0)
                            data.gameState.currentPhase = GamePhase.REINFORCEComputer;
                        break;
                    case ATTACK:
                        //Select if territory is owned by the player
                        if (Territory.OWNED_PLAYER.test(data.hoverTerritory))
                            data.selectedTerritory = data.hoverTerritory;
                            //Attack if territory is owned by the computer
                        else {
                            data.lastFight = new Fight(data.selectedTerritory, data.hoverTerritory);
                            //TODO: maybe add some fancy popup/window displaying the fight
                            if (data.lastFight.apply()) {
                                //Fight has been won
                                data.gameState.map.updateMonopol(data.lastFight.getDef());
                                if (checkGameOver())
                                    return;
                                data.selectedTerritory = data.hoverTerritory;
                                data.gameState.currentPhase = GamePhase.FOLLOW;
                            } else {
                                //Fight has not been won
                                data.selectedTerritory = null;
                                data.gameState.currentPhase = GamePhase.MOVE;
                                button.setText("End Turn");
                            }
                            data.hoverTerritory = null;
                            button.setVisible(true);
                        }
                        break;
                    case FOLLOW:
                        if (data.hoverTerritory != data.selectedTerritory)
                            break;
                        //Move one army to the new territory
                        if (data.lastFight.getAtk().getArmy() > 1 && me.getButton() == MouseEvent.BUTTON1) {
                            data.lastFight.getAtk().addArmy(-1);
                            data.lastFight.getDef().addArmy(1);
                        }
                        //Move one army to the old territory
                        if (data.lastFight.getDef().getArmy() > data.lastFight.getOccupyingArmy()
                                && me.getButton() == MouseEvent.BUTTON3) {
                            data.lastFight.getAtk().addArmy(1);
                            data.lastFight.getDef().addArmy(-1);
                        }
                        break;
                    case MOVE:
                        //Select the hovered territory
                        if (data.selectedTerritory == null) {
                            data.selectedTerritory = data.hoverTerritory;
                            break;
                        } else if (me.getButton() == MouseEvent.BUTTON1 && data.hoverTerritory.getArmy() > 1) {
                            data.selectedTerritory = data.hoverTerritory;
                        }
                        //Move army between two territories
                        else if (me.getButton() == MouseEvent.BUTTON3) {
                            //Only change move target and origin if they are not set yet or no armies are moved yet
                            if ((data.moveTarget == null || data.moveAmount == 0)
                                    && data.gameState.map.getNeighbors(data.selectedTerritory, Territory.OWNED_PLAYER).contains(data.hoverTerritory)) {
                                data.moveTarget = data.hoverTerritory;
                                data.moveOrigin = data.selectedTerritory;
                                moveOne(false);
                            }
                            //Move the armies accordingly
                            else if (data.moveTarget == data.hoverTerritory && data.moveOrigin == data.selectedTerritory) {
                                moveOne(false);
                            } else if (data.moveOrigin == data.hoverTerritory && data.moveTarget == data.selectedTerritory) {
                                moveOne(true);
                            }
                        }

                }
            } else if (data.gameState.currentPhase != GamePhase.FOLLOW && data.moveAmount == 0)
                //Deselect if no territory is hovered
                data.selectedTerritory = null;


            computer.doTurn(data.gameState); //let the computer move
            repaint();
        }
    }

    /**
     * Moves one army between data.moveTarget and data.moveOrigin
     * @param back if true army is moved from target to origin, from origin to target otherwise
     */
    private void moveOne(boolean back) {
        if (back) {
            if (data.moveTarget.getArmy() > 1) {
                data.moveAmount--;
                data.moveTarget.addArmy(-1);
                data.moveOrigin.addArmy(1);
                if (data.moveAmount == 0) {
                    data.moveTarget = null;
                    data.moveOrigin = null;
                    data.selectedTerritory = null;
                }
            }
        } else {
            if (data.moveOrigin.getArmy() > 1) {
                data.moveAmount++;
                data.moveTarget.addArmy(1);
                data.moveOrigin.addArmy(-1);
            }
        }
    }

    /**
     * Checks if game is over and sets current game phase and the winner accordingly
     * @return returns true iff the game is over
     */
    private boolean checkGameOver() {
        if (data.gameState.map.containsTerritory(Territory.UNCLAIMED))
            return false;
        boolean player = data.gameState.map.containsTerritory(Territory.OWNED_PLAYER);
        boolean comp = data.gameState.map.containsTerritory(Territory.OWNED_COMP);
        if (player && comp)
            return false;

        won = player ? "PLAYER" : "COMPUTER";

        data.gameState.currentPhase = GamePhase.GameOver;
        data.selectedTerritory = null;
        repaint();
        return true;
    }

    public void mouseMoved(MouseEvent me) {
        //Only update if no error occurred and game is not over yet
        if (!errorOccurred && data.gameState.currentPhase != GamePhase.GameOver) {
            Predicate<Territory> hoverable = t -> true;

            //Set hoverable according to current game phase
            switch (data.gameState.currentPhase) {
                case CLAIM:
                    hoverable = Territory.UNCLAIMED;
                    break;
                case REINFORCE:
                    hoverable = Territory.OWNED_PLAYER;
                    break;
                case ATTACK:
                    if (data.selectedTerritory != null)
                        //Select all territories from player that can attack
                        //and all territories that are neighbors of selection and owned by computer
                        hoverable = (Territory.CAN_ATTACK.and(Territory.OWNED_PLAYER))
                                .or(t -> data.gameState.map.getNeighbors(data.selectedTerritory, Territory.OWNED_COMP).contains(t));
                    else
                        //Select all territories from player that can attack and have neighbors that
                        //are owned by the computer
                        hoverable = Territory.CAN_ATTACK.and(Territory.OWNED_PLAYER)
                                .and(t -> data.gameState.map.getNeighbors(t, Territory.OWNED_COMP).size() > 0);
                    break;
                case FOLLOW:
                    hoverable = t -> t == data.selectedTerritory;
                    break;
                case MOVE:
                    //Only if no territory is selected and no army has been moved yet
                    if (data.selectedTerritory == null && data.moveAmount == 0)
                        hoverable = t -> t.getArmy() > 1
                                && data.gameState.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0;
                        //If move origin has already been set via data.selectedTerritory
                    else if (data.moveTarget == null || data.moveAmount == 0)
                        hoverable = t -> (t.getArmy() > 1
                                && data.gameState.map.getNeighbors(t, Territory.OWNED_PLAYER).size() > 0)
                                || data.gameState.map.getNeighbors(data.selectedTerritory, Territory.OWNED_PLAYER).contains(t);
                        //If move origin and target have been selected
                    else
                        hoverable = t -> t == data.moveTarget || t == data.moveOrigin;
            }

            //Test hovered territory if it is hoverable
            Territory old = data.hoverTerritory;
            data.hoverTerritory = data.gameState.map.findTerritory(me.getX(), me.getY());
            if (data.hoverTerritory != null && !hoverable.test(data.hoverTerritory))
                data.hoverTerritory = null;
            if (old != data.hoverTerritory) //only repaint if hovered territory has changed
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

    /**
     * Tries to save all necessary parts of the current game to 'saves/'
     *
     * @return returns true if saving has been successful
     */
    public boolean save() {
        if (GameConstants.ENABLE_SAVING && !errorOccurred && data.gameState.currentPhase != GamePhase.GameOver) {
            try {
                FileOutputStream fout = new FileOutputStream("saves/game.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(data);
                oos.close();
                fout.close();


            } catch (Exception ex) {
                System.err.print(ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        } else {
            return false;
        }


        return true;
    }
}
