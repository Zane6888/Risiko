package com.company;

import java.awt.*;
import java.awt.geom.Area;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Continent {
    private List<Territory> territories = new LinkedList<>();
    private Territory hoverTerritory = null;  //the territory that is currently hovered with the mouse
    private Territory selectedTerritory = null; //the territory that is currently selected
    private int bonus; //number of bonus army when the whole continent is owned by one player
    private Area borders; //combined area of all territories of the continent

    private boolean isMonopolPlayer, isMonopolComp; //two booleans that save if the whole continent is owned by the player or the computer

    public Continent(List<Territory> territories, int bonus) {
        this.territories = territories;
        this.bonus = bonus;
        borders = new Area();
        for (Territory t : territories) borders.add(t.getArea());
    }

    public void paintComponent(Graphics2D g) {
        //draws a yellow border arround the continent if it is owned by a single opponent
        //a black border is drawn otherwise
        if (isMonopolComp || isMonopolPlayer) {
            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(7));
        } else {
            g.setColor(new Color(30, 30, 30));
            g.setStroke(new BasicStroke(5));
        }

        if (borders != null) g.draw(borders);

        //Draw all  territories
        for (Territory t : territories) t.paintTerritory(g, t == hoverTerritory, t == selectedTerritory);

        //Draw the capital of each territory
        for (Territory t : territories) t.paintCapitals(g);

    }

    /**
     * Takes all the updates necessary when the mouse has been moved
     *
     * @param x         x position of the mouse
     * @param y         y position of the mouse
     * @param gameState the current GameState to take the right actions
     * @return Returns true iff the panel has to be redrawn
     */
    public boolean mouseMoved(int x, int y, GameState gameState) {

        if (borders.contains(x, y)) {  //if the mouse is within the borders of the continent
            if (gameState.currentPhase == GamePhase.CLAIM) {
                //Iterate throug all territories to check if mouse is within their borders
                for (Territory t : territories) {
                    if (t.contains(x, y)) { //if the territory is not conquered yet and the mouse is within its borders
                        if (t.getArmy() == 0) {
                            if (hoverTerritory != t) {
                                hoverTerritory = t; //correct territory is found and set as hoverTerritory
                                return true;
                            } else return false;  //return false because no repaint is needed
                        } else {
                            if (hoverTerritory != null) {
                                hoverTerritory = null; //no territoriy in this continent should be highlighted
                                return true;
                            } else return false;
                        }
                    }
                }
                return true;
            } else if (gameState.currentPhase == GamePhase.REINFORCE || gameState.currentPhase == GamePhase.ATTACK) {
                //Iterate throug all territories to check if mouse is within their borders
                for (Territory t : territories) {
                    if (t.contains(x, y)) { //if the territory is not conquered yet and the mouse is within its borders
                        if (t.getArmy() > 0) {
                            if (hoverTerritory != t) {
                                hoverTerritory = t; //correct territory is found and set as hoverTerritory
                                return true;
                            } else return false;  //return false because no repaint is needed
                        } else {
                            if (hoverTerritory != null) {
                                hoverTerritory = null; //no territoriy in this continent should be highlighted
                                return true;
                            } else return false;
                        }

                    }
                }
                return true;
            }
        } else {
            //the mouse has left the territory
            if (hoverTerritory != null) {
                //hoverTerritory is not null therefore it has to be set null and the Panel has to be redrawn
                hoverTerritory = null;
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * Updates the variables isMonopolPlayer and isMonopolComp
     */
    private void updateMonopol() {
        isMonopolPlayer = countTerritories(Territory.OWNED_PLAYER) == territories.size();
        isMonopolComp = !isMonopolPlayer && countTerritories(Territory.OWNED_COMP) == territories.size();
    }

    /**
     * taks all the updates necessary when the mouse has been clicked
     *
     * @param x         x position of the mouse
     * @param y         y position of the mouse
     * @param gameState current GameState
     * @return returns true iff Panel has to be redrawn
     */
    public boolean mouseClicked(int x, int y, GameState gameState) {
        if (borders.contains(x, y)) {
            if (gameState.currentPhase == GamePhase.CLAIM) {
                if (hoverTerritory != null && hoverTerritory.getArmy() == 0) {
                    hoverTerritory.setArmy(hoverTerritory.getArmy() + 1);
                    updateMonopol();
                    hoverTerritory = null;
                    gameState.currentPhase = GamePhase.CLAIMComputer;
                    return true;
                }
            } else if (gameState.currentPhase == GamePhase.REINFORCE) {
                if (hoverTerritory != null && hoverTerritory.getArmy() > 0) {
                    hoverTerritory.setArmy(hoverTerritory.getArmy() + 1);
                    gameState.armyPlayer--;
                    if (gameState.armyPlayer <= 0) gameState.currentPhase = GamePhase.REINFORCEComputer;
                    return true;
                }
            } else if (gameState.currentPhase == GamePhase.ATTACK) {
                if (hoverTerritory != null && hoverTerritory.getArmy() > 0) {
                    selectedTerritory = hoverTerritory;
                    return true;
                }
            }
        } else {
            if (selectedTerritory == null) return false;
            else {
                selectedTerritory = null;
                return true;
            }
        }
        return false;
    }

    public Territory getRandomTerritory(Predicate<Territory> p) {
        return Helper.getRandom(territories.stream().filter(p).collect(Collectors.toList()));
    }

    public boolean containsTerritory(Predicate<Territory> p) {
        return territories.stream().filter(p).findAny().isPresent();
    }

    public int countTerritories(Predicate<Territory> p) {
        return (int) territories.stream().filter(p).count();
    }

    /**
     * Returns true iff all territories are conquered by either of the opponents
     */
    public boolean isTaken() {
        for (Territory t : territories) {
            if (t.getArmy() == 0) return false;
        }
        return true;
    }

    /**
     * Calculates three times the number of armies that result from the possession of the territories
     *
     * @param gameState the current gameState that where the new values will be added
     */
    public void calculateArmies(GameState gameState) {
        gameState.armyPlayer += countTerritories(Territory.OWNED_PLAYER);
        gameState.armyComputer += countTerritories(Territory.OWNED_COMP);
        if (isMonopolPlayer) gameState.armyPlayer += bonus * 3;
        else if (isMonopolComp) gameState.armyComputer += bonus * 3;

    }

}
