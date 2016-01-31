package com.company;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;


public class MapParser {

    /**
     * Parses a Map in the given format and returns a GameMap object, containing all the information of the String
     *
     * @param map       String in the given format containing the information of the map
     * @return A GameMap object, containing all the information of the Map-String
     * @throws IOException
     */
    public static GameMap parseMap(String map) throws IOException, InvalidArgumentException {

        Map<String, List<Polygon>> territories = new HashMap<>();  //Map for later conversion into a List<Territory>
        Map<String, Point> capitals = new HashMap<>();              //Map, that saves the Position of the Capitals of each Territory
        Map<String, List<String>> continents = new HashMap<>();     //Map, that will be converted to a List of Territories
        Map<String, Integer> continentBonus = new HashMap<>();      //
        Map<String, List<String>> neighborList = new HashMap<>();

        //Read the string line by line
        BufferedReader reader = new BufferedReader(new StringReader(map));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("patch-of ")) {
                patchOf(line, territories); //The string begins with the keyword "patch-of" and is parsed in the patchOf()-method
            } else if (line.startsWith("capital-of ")) {
                capitalOf(line, capitals);  //The string begins with the keyword "capital-of" and is parsed in the capitalOf()-method
            } else if (line.startsWith("continent ")) {
                continent(line, continentBonus, continents); //The string begins with the keyword "continent" and is parsed in the continent()-method
            } else if (line.startsWith("neighbors-of ")) {
                neighborsOf(line, neighborList); //The string begins with the keyword "neighbors-of" and is parsed in the neighborsOf()-method
            } else {
                if (!line.equals("")) throw new InvalidArgumentException(new String[]{"Invalid line in map"});
            }
        }


        //Convert the collected data into a List<Continent> and a List<Neighbors>

        Map<String, Territory> territoryMap = new HashMap<>();

        List<Continent> continentList = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : continents.entrySet()) {
            List<Territory> territoryList = new LinkedList<>();
            for (String terrName : entry.getValue()) {
                if (capitals.containsKey(terrName)) {
                    Territory newTerritory = new Territory(territories.remove(terrName), terrName, capitals.remove(terrName));
                    territoryList.add(newTerritory);
                    territoryMap.put(terrName, newTerritory);
                } else throw new InvalidArgumentException(new String[]{"'" + entry.getKey() + "' has no capital."});

            }
            continentList.add(new Continent(territoryList, continentBonus.get(entry.getKey())));
        }

        List<Territory> territoryList = new LinkedList<>();
        for (Map.Entry<String, List<Polygon>> entry : territories.entrySet()) {
            if (capitals.containsKey(entry.getKey())) {
                Territory newTerritory = new Territory(entry.getValue(), entry.getKey(), capitals.get(entry.getKey()));
                territoryList.add(newTerritory);
                territoryMap.put(entry.getKey(), newTerritory);
            } else throw new InvalidArgumentException(new String[]{"'" + entry.getKey() + "' has no capital."});
        }

        if (territoryList.size() > 0) {
            Continent restOfMap = new Continent(territoryList, 0);
            continentList.add(restOfMap);
        }

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
    private static void neighborsOf(String input, Map<String, List<String>> neighbors) {
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
        String[] words = input.split("\\s+"); //Split the input string into the separate words

        String name = "";
        List<Integer> xCoords = new LinkedList<>();
        List<Integer> yCoords = new LinkedList<>();

        int i = 0;

        //Iterate through all the string values till a number appears and add it to the name string
        while (i < words.length && !words[i].matches("\\d+(\\.\\d+)?")) {
            name += " " + words[i];
            i++;
        }

        //Iterate through all the coordinates and save it in the xCoords/yCoords lists
        while (i < words.length - 1) {
            Point point = new Point(Integer.valueOf(words[i]), Integer.valueOf(words[i + 1]));
            point = translatePoint(point, GameConstants.MAP_HEIGHT, GameConstants.MAP_WIDTH); //translate the point to generate the 3D effect
            xCoords.add(point.x);
            yCoords.add(point.y);
            i += 2;
        }

        name = name.trim(); //Trim the name string to remove the first whitespace


        //Add the new data to the map
        Polygon newPolygon = new Polygon(Helper.toIntArray(xCoords), Helper.toIntArray(yCoords), xCoords.size());
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
        String[] words = input.split("\\s+"); //split the string into the separate words

        String name = "";

        //Iterate through the words till numbers appear and add them to the name string
        int i = 0;
        while (i < words.length && !words[i].matches("\\d+(\\.\\d+)?")) {
            name += " " + words[i];
            i++;
        }
        name = name.trim(); //Trim the first whitespace from the name

        //The rest of the string are the x and y coordinates and are added parsed into a new point
        Point capital = new Point(Integer.valueOf(words[i]), Integer.valueOf(words[i + 1]));

        capitals.put(name, translatePoint(capital, GameConstants.MAP_HEIGHT, GameConstants.MAP_WIDTH)); //add the new capital to the map
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
        String[] words = input.split("\\s:\\s"); //split the input string into the separate words

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
     * Translates a Point, so that the map appears to be tilted
     *
     * @param point  the Point that has to be tilted
     * @param height the height of the new map
     * @param width  the minimum width of the new map at the top of the map, the maximum width stays the same
     * @return a new translated Point
     */
    private static Point translatePoint(Point point, int height, int width) {
        Point ret = new Point();
        int temp = (int) ((width / 2f) + ((GameConstants.WINDOW_WIDTH / 2 - width / 2f) * point.y / (float) GameConstants.WINDOW_HEIGHT));
        ret.x = Helper.map(point.x, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_WIDTH / 2 - temp, GameConstants.WINDOW_WIDTH / 2 + temp);
        ret.y = Helper.map(point.y, 0, GameConstants.WINDOW_HEIGHT, GameConstants.WINDOW_HEIGHT - height, GameConstants.WINDOW_HEIGHT);
        return ret;

    }


}
