package it.unipi.cs.smartapp.drivers;

import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class ChatSystemDriver {
    // Instance reference
    private static ChatSystemDriver instance = null;
    // Constant - Game Server hostname
    public static final String HOSTNAME = "margot.di.unipi.it";
    // Constant - Game Server port
    public static final int PORT = 8422;

    // Make instance available to the outside
    public static ChatSystemDriver getInstance() {
        if(instance == null) instance = new ChatSystemDriver();
        return instance;
    }


    // Socket (connection to Game Server)
    private Socket socket;
    // Read from socket (input stream)
    private BufferedReader inSocket = null;
    // Write on socket (output stream)
    private PrintWriter outSocket = null;
    // Callback for message arrival
    private Runnable callback = null;
    // Thread listening for messages
    private Thread receiver = null;

    // Constructor
    private ChatSystemDriver() { }

    public void setMessageCallback(Runnable c) { callback = c; }


    // NAME <name> : declare the client name
    public synchronized void sendNAME(String name) {
        String command = "NAME " + name;
        sendCommand(command);
    }

    // JOIN <channel> : subscribe to a channel
    public synchronized void sendJOIN(String channel) {
        String command = "JOIN " + channel;
        sendCommand(command);
    }

    // LEAVE <channel> : unsubscribe from a channel
    public synchronized void sendLEAVE(String channel) {
        String command = "LEAVE " + channel;
        sendCommand(command);
    }

    // POST <channel> <text> : post a message on a channel
    public synchronized void sendPOST(String channel, String text) {
        String command = "POST " + channel + " " + text;
        sendCommand(command);
    }

    private void sendCommand(String command) {
        try {
            if(socket == null) setupSocket();
            outSocket.println(command);
        } catch (IOException e) {
            clearSocket();
        }

    }

    public String[] receive() {
        String[] res;

        try {
            if(socket == null) setupSocket();

            String message = inSocket.readLine();
            res = message.split(" ", 3);
        } catch (IOException e) {
            res = new String[]{"FAIL", "IOException"};
            clearSocket();
        } catch (NullPointerException e) {
            res = new String[]{"FAIL", "NullPointerException"};
            clearSocket();
        }

        return res;
    }

    private void setupSocket() throws IOException {
        try {
            // Create new socket
            socket = new Socket(HOSTNAME, PORT);
            System.out.println("Client socket: " + socket);

            // Create input stream from socket
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            inSocket = new BufferedReader(isr);

            // Create output stream to socket
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osw);
            outSocket = new PrintWriter(bw, true);

            receiver = new Thread(new receiver(callback));
            receiver.setDaemon(true);
            receiver.start();

        } catch (UnknownHostException e) {
            System.err.println("Can not find " + HOSTNAME);
            clearSocket();
        }
    }

    private void clearSocket() {
        try { inSocket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        try { outSocket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        try { socket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        inSocket = null;
        outSocket = null;
        socket = null;
        receiver = null;
    }
}

class receiver implements Runnable {

    Runnable callback;

    public receiver(Runnable c) {
        callback  = c;
    }

    @Override
    public void run() {
        System.out.println("Listener Thread: started");

        ChatSystemDriver driver = ChatSystemDriver.getInstance();
        boolean failed = false;

        while(!failed) {
            String[] message = driver.receive();

            if(message[0].equals("FAIL")) {
                failed = true;
                System.err.println("Listener Thread: " + message[1]);
            } else {
                StateManager.getInstance().newMessage = message;
                Platform.runLater(callback);
            }
        }
        System.out.println("Listener Thread: stopped");
    }
}