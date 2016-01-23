package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a single fight between two Territories
 * Do not change army values in the Territories between creating an instance of Fight and calling its apply method
 * <p>
 * Fancy display of the attack phase shall use instances of this class as input
 * apply() shall be called after all displays are are closed or immediately after creation if the fight is not displayed
 */
public class Fight implements Serializable {
    //Fighting Territories
    private final Territory atk;
    private final Territory def;

    public static BufferedImage[] dice;
    public static BufferedImage[] tank;

    /**
     * Must be called before any instance of Fight is used
     */
    static void loadImages() {
        dice = new BufferedImage[6];
        tank = new BufferedImage[3];
        try {
            dice[0] = ImageIO.read(new File("res/one.png"));
            dice[1] = ImageIO.read(new File("res/two.png"));
            dice[2] = ImageIO.read(new File("res/three.png"));
            dice[3] = ImageIO.read(new File("res/four.png"));
            dice[4] = ImageIO.read(new File("res/five.png"));
            dice[5] = ImageIO.read(new File("res/six.png"));
            tank[0] = ImageIO.read(new File("res/oneTank.png"));
            tank[1] = ImageIO.read(new File("res/twoTank.png"));
            tank[2] = ImageIO.read(new File("res/threeTank.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Territory getAtk() {
        return atk;
    }

    public Territory getDef() {
        return def;
    }

    //Results of all rolled dice, length == # of fighting armies
    private int[] diceAtk;
    private int[] diceDef;

    public int[] getDiceAtk() {
        return diceAtk;
    }

    public int[] getDiceDef() {
        return diceDef;
    }

    //Indexes of the dice rolls being compared to determine the outcome of the Fight
    private int[] duelAtk;
    private int[] duelDef;

    public int[] getDuelDef() {
        return duelDef;
    }

    public int[] getDuelAtk() {
        return duelAtk;
    }

    private int occupyingArmy = -1;

    /**
     * @return Attackers leftover army occupying the defenders Territory, -1 if defender did not lose their Territory
     */
    public int getOccupyingArmy() {
        return occupyingArmy;
    }

    private boolean applied = false;

    public Fight(Territory attacking, Territory defending) {
        atk = attacking;
        def = defending;

        if (atk == null || def == null)
            throw new IllegalArgumentException("Territories can not be null");

        if (atk.getArmy() == 0 || def.getArmy() == 0)
            throw new IllegalArgumentException("Territories can not be unclaimed");

        if (Math.signum((float) atk.getArmy()) == Math.signum((float) def.getArmy()))
            throw new IllegalArgumentException("Territories have to be claimed by different parties, you can not be the Austrian military");

        int armyA = Math.min(Math.abs(atk.getArmy()) - 1, 3);
        int armyD = Math.min(Math.abs(def.getArmy()), 2);

        diceAtk = new int[armyA];

        for (int i = 0; i < armyA; i++)
            diceAtk[i] = Helper.dice();
        Helper.sortDescending(diceAtk);

        diceDef = new int[armyD];

        for (int i = 0; i < armyD; i++)
            diceDef[i] = Helper.dice();
        Helper.sortDescending(diceDef);


        duelAtk = new int[]{Helper.max(diceAtk), -1};
        duelDef = new int[]{Helper.max(diceDef), -1};

        if (diceAtk.length > 1 && diceDef.length > 1) {
            diceAtk[duelAtk[0]] *= -1;
            duelAtk[1] = Helper.max(diceAtk);
            diceAtk[duelAtk[0]] *= -1;

            diceDef[duelDef[0]] *= -1;
            duelDef[1] = Helper.max(diceDef);
            diceDef[duelDef[0]] *= -1;
        }
    }

    /**
     * Applies the change in armies to the Territories
     *
     * @return true if the attacker now occupies the defending Territory
     */
    private boolean apply() {
        if (applied)
            throw new IllegalStateException("Fight was already applied previously");
        applied = true;

        int deathAtk = diceAtk[duelAtk[0]] <= diceDef[duelDef[0]] ? 1 : 0;
        deathAtk += duelAtk[1] != -1 && diceAtk[duelAtk[1]] <= diceDef[duelDef[1]] ? 1 : 0;

        int deathDef = diceAtk[duelAtk[0]] > diceDef[duelDef[0]] ? 1 : 0;
        deathDef += duelAtk[1] != -1 && diceAtk[duelAtk[1]] > diceDef[duelDef[1]] ? 1 : 0;

        if (Math.abs(def.getArmy()) - deathDef == 0) {
            occupyingArmy = diceAtk.length - deathAtk;
            def.setArmy((int) (occupyingArmy * Math.signum((float) atk.getArmy())));
            atk.addArmy(-diceAtk.length);
            return true;
        } else {
            def.addArmy(-deathDef);
            atk.addArmy(-deathAtk);
            return false;
        }
    }

    /**
     * Makes one step in the fight
     *
     * @return returns true iff the fight is over
     */
    public boolean update() {
        //TODO dont apply the whole fight at once
        apply();
        return true;

    }

    /**
     * Draws the current state of the fight with the given Graphics2D
     *
     * @param g Graphics2D to draw on
     */
    public void drawFight(Graphics2D g) {
        boolean playerAttacks = atk.getArmy()>0;

        //Draw the background
        drawBackground(g, playerAttacks);
        int width = (GameConstants.WINDOW_WIDTH-margin*2)/3; //width of one segment of the rectangle
        int height = (GameConstants.WINDOW_HEIGHT-margin*2); //height of the rectangle

        int startYAtk = (GameConstants.WINDOW_HEIGHT - ((diceAtk.length-1)*70 + dice[0].getHeight()))/2;
        int startYDef = (GameConstants.WINDOW_HEIGHT - ((diceDef.length-1)*70 + dice[0].getHeight()))/2;

        int startXAtk = width + margin + 20;
        int startXDef = width*2 + margin - 20 - dice[0].getWidth();



        //Draw the arrows
        for (int i = 0; i < Math.min(diceDef.length, diceAtk.length); i++) {
            Point atkPoint = new Point(startXAtk+dice[0].getWidth(),startYAtk + dice[0].getHeight()/2 + 70*i);
            Point defPoint = new Point(startXDef, startYDef + dice[0].getHeight()/2 + 70 * i);
            if (diceAtk[i] > diceDef[i]) {
                Helper.drawArrow(g, Color.GREEN, atkPoint, defPoint);
            }
            else {
                Helper.drawArrow(g, Color.YELLOW, defPoint, atkPoint);
            }
        }

        //Draw the dices
        for (int i = 0; i < diceAtk.length; i++) {
            g.drawImage(dice[diceAtk[i]-1],startXAtk, startYAtk+70*i, null);
        }

        for (int i = 0; i < diceDef.length; i++) {
            g.drawImage(dice[diceDef[i]-1],startXDef, startYDef+70*i, null);
        }

        //Draw the tanks
        g.drawImage(tank[diceAtk.length-1], width + margin - tank[diceAtk.length-1].getWidth(), (GameConstants.WINDOW_HEIGHT-tank[diceAtk.length-1].getHeight())/2, null);
        g.drawImage(tank[diceDef.length-1], width*2 + margin + tank[diceAtk.length-1].getWidth(), (GameConstants.WINDOW_HEIGHT-tank[diceAtk.length-1].getHeight())/2, -1*tank[diceDef.length-1].getWidth(), tank[diceDef.length-1].getHeight(), null);

        //Draw the names of the territories
        g.setColor(GameConstants.HUD_COLOR);
        g.drawString(atk.getName(), margin+width/2f - (float)g.getFontMetrics().getStringBounds(atk.getName(), g).getWidth()/2f,margin+height-20);
        g.drawString(def.getName(), margin+width*2.5f - (float)g.getFontMetrics().getStringBounds(def.getName(), g).getWidth()/2f,margin+height-20);
    }

    private static int margin = 180;


    /**
     * Draws a rectangle with rounded corners split in red, white and blue
     * @param g Graphics2D to draw on
     */
    private static void drawBackground(Graphics2D g, boolean playerAttacks) {
        int cornerRadius = 40;
        int width = (GameConstants.WINDOW_WIDTH-margin*2)/3;
        int height = GameConstants.WINDOW_HEIGHT-margin*2;

        //Draw the left section
        if (playerAttacks) g.setColor(Color.BLUE);
        else g.setColor(Color.RED);
        g.fillRect(margin+cornerRadius,margin,width-cornerRadius, height);
        g.fillOval(margin, margin, 2 * cornerRadius, 2 * cornerRadius);
        g.fillOval(margin, height+margin-2*cornerRadius, 2*cornerRadius, 2*cornerRadius);
        g.fillRect(margin, margin+cornerRadius, cornerRadius, height-2*cornerRadius);

        //Draw the middle section
        g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.6f));
        g.fillRect(margin+width, margin, width, height);

        //Draw the right section
        if (playerAttacks) g.setColor(Color.RED);
        else g.setColor(Color.BLUE);
        g.fillRect(margin+2*width, margin, width-cornerRadius, height);
        g.fillOval(3*width+margin-2*cornerRadius, margin, 2 * cornerRadius, 2 * cornerRadius);
        g.fillOval(3*width+margin-2*cornerRadius, height+margin-2*cornerRadius, 2*cornerRadius, 2*cornerRadius);
        g.fillRect(3*width+margin-cornerRadius, margin+cornerRadius, cornerRadius, height-2*cornerRadius);

    }
}
