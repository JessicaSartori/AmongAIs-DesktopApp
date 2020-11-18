package it.unipi.cs.smartapp.drivers;

import java.io.*;
import javafx.application.Platform;

import it.unipi.cs.smartapp.statemanager.StateManager;


public class ChatSystemDriver extends SocketDriver {
    // Instance reference
    private static ChatSystemDriver instance = null;

    // Make instance available to the outside
    public static ChatSystemDriver getInstance() {
        if(instance == null) instance = new ChatSystemDriver();
        return instance;
    }


    // Callback for message arrival
    private Runnable callback = null;
    // Thread listening for messages
    private Thread receiver = null;

    // Constructor
    private ChatSystemDriver() {
        HOSTNAME = "margot.di.unipi.it";
        PORT = 8422;
    }

    // Register a callback to be executed when a message arrives
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
        if(isConnected()) { outSocket.println(command); }
        else { System.err.println("Not connected to Chat System"); }
    }

    public String[] receive() {
        String[] res;

        if(!isConnected()) { return "FAIL Not connected".split(" ", 2); }

        try {
            res = inSocket.readLine().split(" ", 3);
        } catch (IOException e) {
            res = "FAIL IOException".split(" ", 2);
            closeConnection();
        } catch (NullPointerException e) {
            res = "FAIL NullPointerException".split(" ", 2);
            closeConnection();
        }

        return res;
    }

    @Override
    public synchronized void openConnection() {
        if(isConnected()) return;
        super.openConnection();

        if(isConnected()) {
            receiver = new Thread(() -> {
                System.out.println("Receiver Thread: started");

                while(true) {
                    String[] message = receive();
                    if(message[0].equals("FAIL")) { break; }

                    StateManager.getInstance().newMessage = message;
                    Platform.runLater(callback);
                }
                System.out.println("Receiver Thread: stopped");
            });

            receiver.setDaemon(true);
            receiver.start();
            System.out.println("Chat System connection open");
        }
    }

    @Override
    public synchronized void closeConnection() {
        if(!isConnected()) return;
        super.closeConnection();
        receiver = null;
        System.out.println("Chat System connection closed");
    }
}