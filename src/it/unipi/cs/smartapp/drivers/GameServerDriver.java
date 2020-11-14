package it.unipi.cs.smartapp.drivers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

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
    private static final long MIN_DELAY = 600;
    // Constant - Milliseconds of inactivity before NOP is sent
    private static final long NOP_DELAY = 30*1000;

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
    public GameServerResponse sendNOP(String gameName) {
        String command = gameName + " NOP";
        String[] rawResponse = sendCommand(command).split(" ", 2);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    public GameServerResponse sendNEW(String gameName) {
        String command = "NEW " + gameName;
        String[] rawResponse = sendCommand(command).split(" ", 2);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    public GameServerResponse sendJOIN(String gameName, String playerName, char nature, String userInfo) {
        String command = gameName + " JOIN " + playerName + " " + nature + " - " + userInfo;
        String[] rawResponse = sendCommand(command).split(" ", 2);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> START : starts game (if possible)
    public GameServerResponse sendSTART(String gameName) {
        String command = gameName + " START";
        String[] rawResponse = sendCommand(command).split(" ", 2);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
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
    public GameServerResponse sendSHOOT(String gameName, char direction) {
        String command = gameName + " SHOOT " + direction;
        String[] rawResponse = sendCommand(command).split(" ", 2);

        ResponseCode code = ResponseCode.fromString(rawResponse[0]);
        GameServerResponse res;

        if(code == ResponseCode.OK) {
            if(rawResponse[1].length() == 1) {
                // A single char -> map cell
                res = new GameServerResponse(code, rawResponse[1].toCharArray()[0], "Shot");
            } else {
                // A normal string -> didn't actually shoot
                res = new GameServerResponse(ResponseCode.ERROR, null, rawResponse[1]);
            }
        } else {
            res = new GameServerResponse(code, null, rawResponse[1]);
        }

        return res;
    }

    public GameServerResponse sendSTATUS(String gameName) {
        String command = gameName + " STATUS";
        String[] rawResponse = sendCommandLong(command, "ENDOFSTATUS").split(" ", 2);

        ResponseCode code = ResponseCode.fromString(rawResponse[0]);
        GameServerResponse res;

        if(code == ResponseCode.OK) {
            String[] content = rawResponse[1].split("\n");
            res = new GameServerResponse(code, Arrays.copyOfRange(content, 1, content.length), content[0]);
        } else {
            res = new GameServerResponse(code, null, rawResponse[1]);
        }

        return res;
    }

    // <game> ACCUSE <player> : CURRENTLY NOT IMPLEMENTED
    public void sendACCUSE() {
        // TODO Implement when made available from Game Server
    }

    // <game> LEAVE <reason> : player leaves game
    public GameServerResponse sendLEAVE(String gameName, String reason) {
        String command = gameName + " LEAVE " + reason;
        String[] rawResponse = sendCommand(command).split(" ", 2);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        clearSocket();

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // Send a general command and wait for the response
    private synchronized String sendCommand(String command) {
        String rawResponse;

        try {
            if(socket == null) setupSocket();

            // Send request
            forcedWait(System.currentTimeMillis());
            outSocket.println(command);
            lastCommandSent = System.currentTimeMillis();

            // Wait for response
            rawResponse = inSocket.readLine();
            if(rawResponse == null) {
                rawResponse = "FAIL Socket closed";
                clearSocket();
            }
        } catch (IOException e) {
            rawResponse = "FAIL Can not communicate with Game Server";
            clearSocket();
        }
        return rawResponse;
    }

    // Send command with a long response to be retrieved
    private synchronized String sendCommandLong(String command, String endString) {
        String rawResponse, line;

        try {
            if(socket == null) setupSocket();

            // Send request
            forcedWait(System.currentTimeMillis());
            outSocket.println(command);
            lastCommandSent = System.currentTimeMillis();

            rawResponse = inSocket.readLine();
            if(!rawResponse.contains("ERROR")) {
                rawResponse = rawResponse.concat("\n");
                while(!(line = inSocket.readLine()).contains(endString)) {
                    rawResponse = rawResponse.concat(line + "\n");
                }
            }
        } catch (IOException e) {
            rawResponse = "FAIL Can not communicate with Game Server";
            clearSocket();
        } catch (NullPointerException e) {
            rawResponse = "FAIL Socket closed";
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
    public GameServerResponse sendConditionalNOP(String gameName) {
        GameServerResponse res = null;
        if(System.currentTimeMillis() - lastCommandSent > NOP_DELAY) {
            res = sendNOP(gameName);
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
            connectionSaver.setDaemon(true);
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

        try { connectionSaver.interrupt(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        inSocket = null;
        outSocket = null;
        socket = null;
        connectionSaver = null;

        System.err.println("Socket closed");
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
            while(!Thread.currentThread().isInterrupted()) {
                Thread.sleep(secondsToWait*1000);

                GameServerResponse res = GameServerDriver.getInstance().sendConditionalNOP(StateManager.getInstance().getCurrentGameName());
                if(res == null) continue;
                if(res.code == ResponseCode.FAIL) {
                    System.err.println("Nop Thread: " + res.freeText);
                    return;
                }
                System.out.println("Nop Thread: " + res.freeText);
            }
        } catch (InterruptedException ignored) { }
        System.out.println("NOP Thread interrupted");
    }
}