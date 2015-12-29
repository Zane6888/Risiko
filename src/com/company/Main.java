package com.company;

import java.awt.*;

public class Main {

    public static void main(String[] args) {
        final String map = args.length > 0 ? args[0] : null;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Window window = new Window(map);
                window.setVisible(true);
            }
        });
    }
}
