package it.unipi.cs.smartapp.drivers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import it.unipi.cs.smartapp.statemanager.StateManager;


public class GameServerDriver {
    // Instance reference
    private static GameServerDriver instance = null;
    // Socket (connection to Game Server)
    private Socket socket;
    // Read from socket (input stream)
    private BufferedReader inSocket = null;
    // Write on socket (output stream)
    private PrintWriter outSocket = null;
    // Last command timestamp (milliseconds)
    private long lastCommandSent = 0;
    // Thread to send NOP in case of inactivity
    private Thread connectionSaver = null;

    // Constant - Game Server hostname
    public static final String HOSTNAME = "margot.di.unipi.it";
    // Constant - Game Server port
    public static final int PORT = 8421;
    // Constant - Milliseconds before next command can be sent to Game Server
    private static final long MIN_DELAY = 500;

    // Constructor
    private GameServerDriver() { }

    // Make instance available to the outside
    public static GameServerDriver getInstance() {
        if(instance == null) instance = new GameServerDriver();
        return instance;
    }

    /*
     * List of commands to send to Game Server
     *
     * All the following methods do is:
     * -	generate command (according to Game Protocol);
     * -	send command to Game Server;
     * -	get response from Game Server;
     * -	do basic preprocessing (string split).
     *
     * These methods return an array of two strings:
     * -	response[0] is either OK or ERROR;
     * -	response[1] contains the rest of the data.
     *
     * Further processing of response[1] is left to business logic.
     */

    // <game> NOP : resets command timer
    public String[] sendNOP(String gameName) {
        String command = gameName + " NOP";
        String rawResponse = sendCommand(command);
        return rawResponse.split(" ", 2);
    }

    // NEW <game> : creates new game
    public String[] sendNEW(String gameName) {
        String command = "NEW " + gameName;
        String rawResponse = sendCommand(command);
        return rawResponse.split(" ", 2);
    }

    // <game> JOIN <player-name> <nature> <role> <user-info> : joins game
    public String[] sendJOIN(String gameName, String playerName, char nature, String userInfo) {
        // <role> currently does nothing, sending - as placeholder
        String command = gameName + " JOIN " + playerName + " " + nature + " - " + userInfo;
        String rawResponse = sendCommand(command);
        return rawResponse.split(" ", 2);
    }

    // <game> START : starts game (if possible)
    public String[] sendSTART(String gameName) {
        String command = gameName + " START";
        String rawResponse = sendCommand(command);
        return rawResponse.split(" ", 2);
    }

    // <game> LOOK : request to see the map
    public String[] sendLOOK(String gameName) {
        String command = gameName + " LOOK";
        String rawResponse = sendCommandLong(command, "ENDOFMAP");
        return rawResponse.split(" ", 2);
    }

    // <game> MOVE <direction> : request to move player
    public String[] sendMOVE(String gameName, char direction) {
        String command = gameName + " MOVE " + direction;
        String rawResponse = sendCommand(command);
        return rawResponse.split(" ", 2);
    }

    // <game> SHOOT <direction> : request to shoot
    public String[] sendSHOOT(String gameName, char direction) {
        String command = gameName + " SHOOT " + direction;
        String rawResponse = sendCommand(command);
        return rawResponse.split(" ", 2);
    }

    // <game> STATUS : request to get the status
    public String[] sendSTATUS(String gameName) {
        String command = gameName + " STATUS";
        String rawResponse = sendCommandLong(command, "ENDOFSTATUS");
        return rawResponse.split(" ", 2);
    }

    // <game> ACCUSE <player> : CURRENTLY NOT IMPLEMENTED
    public void sendACCUSE() {
        // TODO Implement when made available from Game Server
    }

    // <game> LEAVE <reason> : player leaves game
    public String[] sendLEAVE(String gameName, String reason) {
        String command = gameName + " LEAVE " + reason;
        String rawResponse = sendCommand(command);
        return rawResponse.split(" ", 2);
    }

    // Send a general command and wait for the response
    private synchronized String sendCommand(String command) {
        String rawResponse;

        try {
            if(socket == null)
                setupSocket();

            // Send request
            forcedWait(System.currentTimeMillis());
            outSocket.println(command);
            lastCommandSent = System.currentTimeMillis();

            // Wait for response
            rawResponse = inSocket.readLine();
            if(rawResponse == null) {
                rawResponse = "ERROR Socket closed";
                clearSocket();
            }
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
            clearSocket();
        }
        return rawResponse;
    }

    // Send command with a long response to be retrieved
    private synchronized String sendCommandLong(String command, String endString) {
        String rawResponse = "", line;

        try {
            if(socket == null)
                setupSocket();

            // Send request
            forcedWait(System.currentTimeMillis());
            outSocket.println(command);
            lastCommandSent = System.currentTimeMillis();

            // Read the response
            while(!(line = inSocket.readLine()).contains(endString)) {
                System.out.println(line);
                if (line.equals("OK "))
                    rawResponse = rawResponse.concat(line);
                else
                    rawResponse = rawResponse.concat(line + "\n");
            }
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
            clearSocket();
        } catch (NullPointerException e) {
            System.err.println("Idk");
            rawResponse = "ERROR idk";
            clearSocket();
        }
        return rawResponse;
    }

    // Forces to wait at least MIN_DELAY
    private void forcedWait(long currentTime) {
        // Time difference in milliseconds
        long timeDifference = currentTime - lastCommandSent;

        if(timeDifference < MIN_DELAY) {
            try {
                Thread.sleep(MIN_DELAY - timeDifference);
            } catch (InterruptedException e) {
                System.err.println("Thread.sleep(...) failed");
                e.printStackTrace();
            }
        }
    }

    // Send a NOP request in case the last sent request happened more than 30 seconds ago
    public String[] sendConditionalNOP(String gamename) {
        String[] res = null;
        if(System.currentTimeMillis() - lastCommandSent > 30*1000) {
            System.out.println(System.currentTimeMillis() - lastCommandSent);
            res = sendNOP(gamename);
        }
        return res;
    }

    // Create a new socket to the game server and the corresponding input/output streams
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

            lastCommandSent = 0;

            connectionSaver = new Thread(new NOPSender(10));
            connectionSaver.start();

        } catch (UnknownHostException e) {
            System.err.println("Can not find " + HOSTNAME);
            clearSocket();
        }
    }

    // Close the sockets and the input/output streams
    private void clearSocket() {
        try { inSocket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        try { outSocket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        try { socket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        connectionSaver.interrupt();
        try{
            connectionSaver.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while joining...");
        }

        inSocket = null;
        outSocket = null;
        socket = null;
    }

}

/*
 * Runnable to maintain the connection to the game server open
 * in case of inactivity
 */
class NOPSender implements Runnable {

    private final long secondsToWait;

    public NOPSender(long nSeconds) { secondsToWait = nSeconds; }

    @Override
    public void run() {
        System.out.println("NOP Thread started");
        try {
            while(true) {
                Thread.sleep(secondsToWait*1000);

                String[] res = GameServerDriver.getInstance().sendConditionalNOP(StateManager.getInstance().getCurrentGameName());
                if(res == null) continue;
                if(res[0].equals("ERROR")) {
                    System.err.println(res[1]);
                    return;
                }
                System.out.println("NOP Sent");
            }
        } catch (InterruptedException e) {
            System.out.println("NOP Thread interrupted");
        }
    }
}