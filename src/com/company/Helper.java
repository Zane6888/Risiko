package com.company;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Helper {
    private static final Random rnd = new Random();

    public static <T> T getRandom(List<T> l) {
        if (l == null || l.size() == 0)
            return null;
        int r = rnd.nextInt(l.size());
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

    public static int dice() {
        return rnd.nextInt(5) + 1;
    }

    public static int max(int[] arr) {
        int index = 0;

        for (int i = 1; i < arr.length; i++)
            if (arr[i] > arr[index])
                index = i;
        return index;
    }

    /**
     * Maps a Float f, that is in the range of in_min and in_max, to an range of out_min and out_max
     *
     * @param f       the value that is to map
     * @param in_min  Minimum of the input range
     * @param in_max  maximum of the input range
     * @param out_min minimum of the output range
     * @param out_max maximum of the output range
     */
    public static int map(float f, float in_min, float in_max, float out_min, float out_max) {
        return (int) ((f - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
}
