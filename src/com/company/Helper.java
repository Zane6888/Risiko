package com.company;

import java.awt.*;
import java.util.List;

public class Helper {

    public static <T> T getRandom(List<T> l) {
        int r = (int) (Math.random() * l.size());
        return l.get(r);
    }

    /**
     * Converts an List<Integer> to an int-Array and returns it
     *
     * @param list List<Integer> that should be converted
     * @return An int-Array, containing the same values as the List<Integer> and has the same size
     */
    public static int[] toIntArray(List<Integer> list) {
        int[] returnArray = new int[list.size()];
        for (int i = 0; i < returnArray.length; i++)
            returnArray[i] = list.get(i);
        return returnArray;
    }

    public static Color multiplyColor(Color c, Float f) {
        int red = Math.max(Math.min((int) (c.getRed() * f), 255), 0);
        int green = Math.max(Math.min((int) (c.getGreen() * f), 255), 0);
        int blue = Math.max(Math.min((int) (c.getBlue() * f), 255), 0);
        return new Color(red, green, blue);
    }
}
