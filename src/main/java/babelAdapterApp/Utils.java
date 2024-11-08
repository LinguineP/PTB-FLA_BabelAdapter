package babelAdapterApp;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {


   
    public int[] getDoppelgangerPortsFromConfig() {
        List<Integer> valuesList = new ArrayList<>();

        Path configFilePath = null;
        try {
            configFilePath = getResourcePath("adapter.conf");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Path doppelgangerFilePath = null;


        if (configFilePath == null) {
            System.err.println("Could not find configuration or script files.");
            return null;
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(configFilePath.toString()));
            boolean insideValues = false;

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("doppelganger.ports=[")) {
                    insideValues = true;
                    String valuesPart = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                    String[] values = valuesPart.split(",");
                    for (String value : values) {
                        try {
                            valuesList.add(Integer.parseInt(value.trim()));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid integer in config file: " + value);
                        }
                    }
                } else if (line.equals("]")) {
                    insideValues = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return valuesList.stream().mapToInt(Integer::intValue).toArray();
    }

    

    public Path getResourcePath(String resourceName) throws Exception {
        // Load the resource
        URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }

        // Copy to a temporary file and return the path
        Path tempFile = Files.createTempFile(resourceName, null);
        Files.copy(resource.openStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    public static boolean contains(int[] array, int value) {
        return Arrays.stream(array).anyMatch(num -> num == value);
    }
}
