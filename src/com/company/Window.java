package com.company;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Window extends JFrame {

    public Window(String map) {
        if (map == null || map.equals("")) { //no map is given
            System.out.println("No map specified, trying to load saves/game.ser");
            try {  //Try to load saved map
                Panel game = new Panel();
                add(game);

                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        System.out.println("Saving game");
                        game.save();
                    }
                }, "Shutdown thread"));

            } catch (Exception e) {
                System.out.println("Could not load saves/game.ser, defaulting to: '" + GameConstants.DEFAULT_MAP + "'");
                map = GameConstants.DEFAULT_MAP;
                addPanel(map);
            }

        } else addPanel(map); //Map is specified, try to load it

        this.setSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        this.setTitle("All those territories");
        this.setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Makes a new instance of Panel and adds a shutdown hook to save the game when window is closed
     * @param map the location of the map file the Panel is created with
     */
    private void addPanel(String map) {
        try {
            Panel game = new Panel(new Scanner(new File(map)).useDelimiter("\\Z").next());
            this.add(game);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    System.out.println("Saving game");
                    game.save();
                }
            }, "Shutdown thread"));

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
