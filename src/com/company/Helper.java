package com.company;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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

    /**
     * Lightens the given color by the given amount of percentage
     *
     * @param c       Color that should be lightened
     * @param percent percentage the color is lightened by
     * @return a new lightened Color
     */
    public static Color lightenColor(Color c, int percent) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
        float brightness = hsb[2] + percent / 100f;
        if (brightness > 1) brightness = 1;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], brightness));
    }

    /**
     * Returns a random integer between 1 and 6
     */
    public static int dice() {
        return rnd.nextInt(6) + 1;
    }

    /**
     * Returns the highest element of an int array
     */
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

    /**
     * Draws an arrow from 'from' to 'to'
     *
     * @param g Graphics2D that is drawn on
     * @param c Color the arrow is drawn in
     * @param from the starting point of the arrow
     * @param to the end point of the arrow
     */
    public static void drawArrow(Graphics2D g, Color c, Point from, Point to) {

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 20);
        arrowHead.addPoint(-10, 0);
        arrowHead.addPoint(10, 0);


        Point2D.Double unitVector = getUnitVector(from, to);


        //Shorten the arrow to fit the head
        to = new Point(to.x - (int) (unitVector.x * 20), to.y - (int) (unitVector.y * 20)); //shorten the line to fit the arrow head

        //The head is drawn to a new Graphics2D object in order to rotate it with an AffineTransform

        AffineTransform at = new AffineTransform(); //AffineTransform to rotate the arrow head
        at.setToIdentity();
        double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX()); //angle of the arrow
        at.translate(to.x, to.y); //Move the arrow head to the end point
        at.rotate((angle - Math.PI / 2d)); //Rotate the arrow

        //Draw the line of the arrow
        g.setColor(c);
        g.setStroke(new BasicStroke(7));
        g.drawLine(from.x, from.y, to.x, to.y);

        Graphics2D g2 = (Graphics2D) g.create(); //new Graphics2D for the rotation
        g2.setTransform(at);
        g2.fill(arrowHead);
        g2.dispose();
    }

    public static Point2D.Double getUnitVector(Point from, Point to) {
        Point vector = new Point(to.x - from.x, to.y - from.y); //vector of the arrow
        double vectorLenght = Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2));
        return new Point2D.Double(vector.x / vectorLenght, vector.y / vectorLenght); //unit vector
    }

    /**
     * Sorts an array descending using the Quicksort algorithm
     * @param array int[] array that should be sorted
     */
    public static void sortDescending(int[] array) {
        quickSort(array, 0, array.length-1);
    }

    /**
     * Sorts an array descending using the Quicksort algorithm
     * @param array int[] array that should be sorted
     * @param low starting element
     * @param high end element
     */
    private static void quickSort(int[] array, int low, int high) {
        int i = low;
        int j = high;
        int pivot = array[(low + high) / 2]; //take the middle of the search spectrum as pivot element

        //divide the array in a part that is greater than the pivot and one that is smaller
        while (i <= j) {
            while (array[i] > pivot) {
                i++;
            }
            while (pivot > array[j]) {
                j--;
            }

            if (i <= j) {
                int h = array[i];
                array[i] = array[j];
                array[j] = h;

                i++;
                j--;
            }
        }

        //do the same with each half
        if (low < i - 1) {
            quickSort(array, low, i - 1);
        }
        if (i < high) {
            quickSort(array, i, high);
        }


    }
}
