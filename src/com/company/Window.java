package com.company;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Window extends JFrame {
    public Window(String map) {
        if (map == null || map.equals("")) {
            System.out.println("No map specified, defaulting to: '" + GameConstants.DEFAULT_MAP + "'");
            map = GameConstants.DEFAULT_MAP;
        }
        try {
            this.add(new Panel(new Scanner(new File(map)).useDelimiter("\\Z").next()));
        } catch (IOException e) {
            System.out.println("FEHLER: " + e.getMessage());
        }
        this.setSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        this.setTitle("All those territories");
        this.setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
