package babelAdapterApp;

import com.google.gson.Gson;
import data.AdapterMessage;
import tardis.SimpleUseCase;
import tardis.app.data.UserMessage;


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;


//base communication class of this project

public class Mpapi {

    public static SimpleUseCase BabelBaseLogic;

    private static String HostName;
    private static int adapterServerPort;

    public static int getDoppelgangerMailboxOffset() {
        return doppelgangerMailboxOffset;
    }

    public static void setDoppelgangerMailboxOffset(int doppelgangerMailboxOffset) {
        Mpapi.doppelgangerMailboxOffset = doppelgangerMailboxOffset;
    }

    private static int doppelgangerMailboxOffset;

    public final static String ADAPTER_SERVER_PORT = "adapter.serverPort";
    public final static String DOPPELGANGER_BASE_PORT = "doppelganger.basePort";
    public final static String DOPPELGANGER_MAILBOX_OFFSET = "doppelganger.mailboxOffset";
    public final static String DOPPELGANGER_ID = "doppelganger.id";


    private static final int MAX_RETRY_COUNT = 1;

    public static boolean sendMsg(InetSocketAddress remoteServerAddress, Gson msg) {
        return sendMsg(remoteServerAddress, msg.toString());
    }

    public static boolean sendMsg(InetSocketAddress remoteServerAddress, String msg) {
        if (remoteServerAddress == null || msg == null) {
            System.err.println("Remote server address or message cannot be null.");
            return false;
        }
        System.out.println(remoteServerAddress);
        int counter = 0;

        while (counter < MAX_RETRY_COUNT) {
            try (Socket socket = new Socket(remoteServerAddress.getAddress(), remoteServerAddress.getPort());
                 OutputStream os = socket.getOutputStream();
                 DataOutputStream out = new DataOutputStream(os)) {

                // Convert the message to bytes and send it
                byte[] bmsg = msg.getBytes(StandardCharsets.UTF_8);
                out.write(bmsg);
                System.out.println("Message sent successfully: " + msg);
                return true; // Return true if message is sent successfully

            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + e.getMessage() + " (Address: " + remoteServerAddress + ")");
                retryOperation(counter);
            }
            catch (IOException e) {
                System.err.println("IOException in sendMsg: " + e.getMessage() + ". Retrying...");
                retryOperation(counter);
            }

            counter++;
        }

        System.err.println("Max retry count reached. Exiting...");
        return false; // Return false if sending fails after max retries
    }

    private static void retryOperation(int counter) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Method to start a TCP server and listen for incoming messages
    public static void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port + "...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    System.out.println("Connected to a client: " + clientSocket.getInetAddress());

                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Received message: " + message);
                        UserMessage babelMessage = new UserMessage(HostName,"ptbflaInstance",message);
                        BabelBaseLogic.sendUserMessage(babelMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }



}
