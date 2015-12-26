package com.company;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Window extends JFrame {
    public Window() {
        //TODO Hier musst du den Speicherort der map halt �ndern
        //TODO In der fertigen Version muss die Map dem Programm �bergeben werden
        try {
            this.add(new Panel(new Scanner(new File("maps/world.map")).useDelimiter("\\Z").next()));
        } catch (IOException e) {
            System.out.println("FEHLER: " + e.getMessage());
        }
        this.setSize(1250, 650);
        this.setTitle("All those territories");
        this.setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
