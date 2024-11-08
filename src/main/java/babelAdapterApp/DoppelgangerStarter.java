package babelAdapterApp;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import babelAdapterApp.Utils;


//class for starting doppelgangers
public class DoppelgangerStarter {
    private List<Process> processes = new ArrayList<>(); // To keep track of started processes
    private List<Integer> portsList=new ArrayList<>();
    private static  final  int offset=1000;

    // Method to start the Python process for each port
    public void startPythonScript() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String pythonCommand = os.contains("win") ? "python" : "python3";

        Utils utils=new Utils();

        Path doppelgangerFilePath = utils.getResourcePath("doppelganger.py");

        if (doppelgangerFilePath == null) {
            System.err.println("Could not find configuration or script files.");
            return;
        }

        int[] ports = utils.getDoppelgangerPortsFromConfig();

        for (int port : ports) {
            portsList.add(port);
            portsList.add(port+offset);
            System.out.println("DopelgangerPORTs"+port);
            startProcess(pythonCommand, doppelgangerFilePath.toAbsolutePath().toString(), port);
        }

        // Add shutdown hook to terminate all processes on exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanupProcesses));
    }



    private void startProcess(String pythonCommand, String scriptPath, int port) {
        List<String> command = new ArrayList<>();
        command.add(pythonCommand);
        command.add(scriptPath);
        command.add(String.valueOf(port));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Redirect error stream to output

        try {
            Process process = processBuilder.start();
            processes.add(process); // Keep track of the process

            // Capture output in a separate thread
            new Thread(() -> captureProcessOutput(process)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void captureProcessOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void sendExitMessage(String host, int port) {
        String message = "{\"payload\":\"exit\"}";
        try (Socket socket = new Socket(host, port);
             OutputStream outputStream = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(outputStream, true)) {

            writer.println(message);
            System.out.println("Message sent: " + message);

        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanupProcesses() {

        for (Integer port : portsList) {
                PortCloser.closeProcessesOnPort(port);
        }



        for (Process process : processes) {
            if (process != null) {
                process.destroy(); // Gracefully terminate the process
                System.out.println("Child process terminated.");
            }
        }
        processes.clear(); // Clear the list after cleanup
    }



    public static void main(String[] args) {
        try {
            new DoppelgangerStarter().startPythonScript();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}





