package com.company;

import java.awt.*;
import java.awt.geom.Area;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;


public class MapParser {

    private static final int height = 600, width = 1000;  //width and height of the map after it has been tilted
    private static final int mapHeight = 650, mapWidth = 1250; //width and height of the map and also the window

    /**
     * Parses a Map in the given format and returns a GameMap object, containing all the information of the String
     *
     * @param map       String in the given format containing the information of the map
     * @return A GameMap object, containing all the information of the Map-String
     * @throws IOException
     */
    public static GameMap parseMap(String map) throws IOException {

        Map<String, List<Polygon>> territories = new HashMap<>();  //Eine Map f�r die sp�tere Konvertierung in eine List<Territory>
        Map<String, Point> capitals = new HashMap<>();              //Map, that saves the Position of the Capitals of each Territory
        Map<String, List<String>> continents = new HashMap<>();     //Map, that will be converted to a List of Territories
        Map<String, Integer> continentBonus = new HashMap<>();      //
        Map<String, List<String>> neighborList = new HashMap<>();

        //Read the string line by line
        BufferedReader reader = new BufferedReader(new StringReader(map));
        String line;
        while ((line = reader.readLine()) != null) {
            line.trim();
            if (line.startsWith("patch-of ")) {
                patchOf(line, territories); //The string begins with the keyword "patch-of" and is parsed in the patchOf()-method
            } else if (line.startsWith("capital-of ")) {
                capitalOf(line, capitals);  //The string begins with the keyword "capital-of" and is parsed in the capitalOf()-method
            } else if (line.startsWith("continent ")) {
                continent(line, continentBonus, continents); //The string begins with the keyword "continent" and is parsed in the continent()-method
            } else if (line.startsWith("neighbors-of ")) {
                neighborsOf(line, neighborList); //The string begins with the keyword "neighbors-of" and is parsed in the neighborsOf()-method
            }
        }


        //Convert the collected data into a List<Continent> and a List<Neighbors>

        Map<String, Territory> territoryMap = new HashMap<>();

        List<Continent> continentList = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : continents.entrySet()) {
            Area continentBorder = new Area();
            List<Territory> territoryList = new LinkedList<>();
            for (String terrName : entry.getValue()) {
                //for (Polygon terr : territories.get(terrName)) continentBorder.add(new Area(AffineTransform.getScaleInstance(1.1,1.1).createTransformedShape(terr)));
                for (Polygon terr : territories.get(terrName)) continentBorder.add(new Area(terr));
                Territory newTerritory = new Territory(territories.remove(terrName), terrName, capitals.remove(terrName));
                territoryList.add(newTerritory);

                territoryMap.put(terrName, newTerritory);

            }
            continentList.add(new Continent(territoryList, continentBonus.get(entry.getKey()), continentBorder));
        }

        List<Territory> territoryList = new LinkedList<>();
        Area continentBorder = new Area();
        for (Map.Entry<String, List<Polygon>> entry : territories.entrySet()) {
            Territory newTerritory = new Territory(entry.getValue(), entry.getKey(), capitals.get(entry.getKey()));
            territoryList.add(newTerritory);
            for (Polygon terr : entry.getValue()) continentBorder.add(new Area(terr));

            territoryMap.put(entry.getKey(), newTerritory);
        }
        Continent restOfMap = new Continent(territoryList, 0, continentBorder);
        continentList.add(restOfMap);

        List<Neighbors> neighbors = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : neighborList.entrySet()) {
            for (String neighbourTwo : entry.getValue())
                neighbors.add(new Neighbors(territoryMap.get(entry.getKey()), territoryMap.get(neighbourTwo)));
        }

        return new GameMap(continentList, neighbors);
    }

    /**
     * Reads an input string starting with "neighbors-of " and parses it into an Map<String, List<String>>
     *
     * @param input     String that has to be parsed
     * @param neighbors a map where the neighbors described in the input string are added
     */
    public static void neighborsOf(String input, Map<String, List<String>> neighbors) {
        input = input.substring(13); //remove "neighbors-of "
        String[] words = input.split("\\s+:\\s+"); //Split the string into the two parts header and body
        for (String s : words[1].split("\\s+-\\s+")) { //split the body into each word and iterate it
            //Add all the strings to the map
            if (neighbors.containsKey(words[0])) neighbors.get(words[0]).add(s);
            else {
                List<String> newList = new LinkedList<>();
                newList.add(s);
                neighbors.put(words[0], newList);
            }
        }
    }

    /**
     * Reads an input string starting with "patch-of " and parses it into an Map<String, List<Polygon>>
     *
     * @param input       String that has to be parsed
     * @param territories a map where the polygons described in the input string are added
     */
    private static void patchOf(String input, Map<String, List<Polygon>> territories) {
        input = input.substring(9); //Removes "patch-of " from the string
        String[] words = input.split("\\s+"); //Split the input string into the seperate words

        String name = "";
        List<Integer> xCoords = new LinkedList<>();
        List<Integer> yCoords = new LinkedList<>();

        int i = 0;

        //Iterate through all the string values till a number appears and add it to the name string
        while (i < words.length && !words[i].matches("\\d+(\\.\\d+)?")) {
            name += " " + words[i];
            i++;
        }

        //Iterate through all the coordinats and save it in the xCoords/yCoords lists
        while (i < words.length - 1) {
            Point point = new Point(Integer.valueOf(words[i]), Integer.valueOf(words[i + 1]));
            point = translatePoint(point, height, width); //translate the point to generate the 3D effect
            xCoords.add(point.x);
            yCoords.add(point.y);
            i += 2;
        }

        name = name.trim(); //Trim the name string to remove the first whitespace


        //Add the new data to the map
        Polygon newPolygon = new Polygon(toIntArray(xCoords), toIntArray(yCoords), xCoords.size());
        if (territories.containsKey(name)) {
            territories.get(name).add(newPolygon);
        } else {
            List<Polygon> newList = new LinkedList<>();
            newList.add(newPolygon);
            territories.put(name, newList);

        }
    }

    /**
     * Reads an input string starting with "capital-of " and parses it into an Map<String, List<Polygon>>
     *
     * @param input    String that has to be parsed
     * @param capitals a map where the new capital positions are added
     */
    private static void capitalOf(String input, Map<String, Point> capitals) {
        input = input.substring(11); //remove "capital-of " from the input string
        String[] words = input.split("\\s+"); //split the string into the seperate words

        String name = "";

        //Iterate through the words till numbers appear and add them to the name string
        int i = 0;
        while (i < words.length && !words[i].matches("\\d+(\\.\\d+)?")) {
            name += " " + words[i];
            i++;
        }
        name = name.trim(); //Trim the first whitespace from the name

        //The rest of the string are the x and y coordinats and are added parsed into a new point
        Point capital = new Point(Integer.valueOf(words[i]), Integer.valueOf(words[i + 1]));

        capitals.put(name, translatePoint(capital, height, width)); //add the new capital to the map
    }

    /**
     * Reads and input string starting with "continent " and parses it into a Map<String, Integer> and Map<String, List<String>>
     *
     * @param input          String that has to be parsed
     * @param continentBonus a map, where the integer values of the bonuses from the continents are added
     * @param continents     a map, where the territories of the continents are added
     */
    private static void continent(String input, Map<String, Integer> continentBonus, Map<String, List<String>> continents) {
        input = input.substring(10); //remove "continent " from the string
        String[] words = input.split("\\s:\\s"); //split the input string into the seperate words

        //Header
        String[] header = words[0].split("\\s");
        String name = "";
        for (int i = 0; i < header.length - 1; i++)
            name += " " + header[i];
        name = name.trim(); //remove the first whitespace from the string
        Integer bonus = Integer.parseInt(header[header.length - 1]);

        //Body
        String[] body = words[1].split("\\s-\\s");

        //Parse the data into the maps
        continentBonus.put(name, bonus);
        continents.put(name, Arrays.asList(body));
    }


    /**
     * Converts an List<Integer> to an int-Array and returns it
     *
     * @param list List<Integer> that should be converted
     * @return An int-Array, containing the same values as the List<Integer> and has the same size
     */
    private static int[] toIntArray(List<Integer> list) {
        int[] returnArray = new int[list.size()];
        for (int i = 0; i < returnArray.length; i++)
            returnArray[i] = list.get(i);
        return returnArray;
    }

    /**
     * Translates a Point, so that the map appears to be tilted
     *
     * @param point  the Point that has to be tilted
     * @param height the height of the new map
     * @param width  the minimum width of the new map at the top of the map, the maximum width stays the same
     * @return a new translated Point
     */
    private static Point translatePoint(Point point, int height, int width) {
        Point ret = new Point();
        int temp = (int) ((width / 2f) + ((mapWidth / 2 - width / 2f) * point.y / (float) mapHeight));
        ret.x = map(point.x, 0, mapWidth, mapWidth / 2 - temp, mapWidth / 2 + temp);
        ret.y = map(point.y, 0, mapHeight, mapHeight - height, mapHeight);
        return ret;

    }

    /**
     * Maps an Integer x, that is in the range of in_min and in_max, to an range of out_min and out_max
     *
     * @param x       the value that is to map
     * @param in_min  Minimum of the input range
     * @param in_max  maximum of the input range
     * @param out_min minimum of the output range
     * @param out_max maximum of the output range
     * @return
     */
    public static int map(float x, float in_min, float in_max, float out_min, float out_max) {
        return (int) ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
}
