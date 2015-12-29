package com.company;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Window extends JFrame {
    private static String DEFAULT_MAP = "maps/world.map";

    public Window(String map) {
        if (map == null || map.equals("")) {
            System.out.println("No map specified, defaulting to: '" + DEFAULT_MAP + "'");
            map = DEFAULT_MAP;
        }
        try {
            this.add(new Panel(new Scanner(new File(map)).useDelimiter("\\Z").next()));
        } catch (IOException e) {
            System.out.println("FEHLER: " + e.getMessage());
        }
        this.setSize(1250, 650);
        this.setTitle("All those territories");
        this.setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
