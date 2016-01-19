package com.company;

import java.awt.*;
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

        diceDef = new int[armyD];

        for (int i = 0; i < armyD; i++)
            diceDef[i] = Helper.dice();

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
     * Applies the change in armys to the Territories
     *
     * @return true if the attacker now occupies the defending Territory
     */
    public boolean apply() {
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
        //TODO actually draw the fight
        //Idea: http://www.x-oo.com/flash-online-games/brettspiele/world-wars-2.html
        g.drawOval(0, 0, 1000, 1000);
    }
}
