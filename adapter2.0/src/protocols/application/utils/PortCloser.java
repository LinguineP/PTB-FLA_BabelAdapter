package protocols.application.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PortCloser {

    //utility class for ensuring that ports get closed properly

    public static void closeProcessesOnPort(int port) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                closeProcessOnPortWindows(port);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                closeProcessOnPortLinux(port);
            } else {
                System.out.println("Unsupported OS.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static void closeProcessOnPortWindows(int port) throws IOException, InterruptedException {
        // Find PIDs using netstat and taskkill them
        Process findProcess = new ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr :" + port).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.trim().split("\\s+");
            String pid = tokens[tokens.length - 1];
            //System.out.println("Closing process " + pid + " on port " + port);
            new ProcessBuilder("cmd.exe", "/c", "taskkill /PID " + pid + " /F").start().waitFor();
        }
    }

    private static void closeProcessOnPortLinux(int port) throws IOException, InterruptedException {
        // Find PIDs using lsof and kill them
        Process findProcess = new ProcessBuilder("bash", "-c", "lsof -t -i :" + port).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
        String pid;
        while ((pid = reader.readLine()) != null) {
            //System.out.println("Closing process " + pid + " on port " + port);
            new ProcessBuilder("bash", "-c", "kill -9 " + pid).start().waitFor();
        }
    }

    public static void main(String[] args) {
        int port = 7001; // Change this to the port you want to close
        closeProcessesOnPort(port);
    }
}