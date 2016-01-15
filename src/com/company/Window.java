package com.company;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Window extends JFrame {
    public Window(String map) {
        if (map == null || map.equals("")) {
            System.out.println("No map specified, trying to load saves/game.ser");
            try {
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

        } else addPanel(map);

        this.setSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        this.setTitle("All those territories");
        this.setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

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
