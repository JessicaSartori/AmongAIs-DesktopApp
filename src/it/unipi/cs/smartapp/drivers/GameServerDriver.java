package it.unipi.cs.smartapp.drivers;

import java.io.*;
import java.util.Arrays;

import it.unipi.cs.smartapp.statemanager.StateManager;


public class GameServerDriver extends SocketDriver {
    // Instance reference
    private static GameServerDriver instance = null;

    // Make instance available to the outside
    public static GameServerDriver getInstance() {
        if(instance == null) instance = new GameServerDriver();
        return instance;
    }


    // Last command timestamp (milliseconds)
    private long lastCommandSent;
    // Thread to send NOP in case of inactivity
    private Thread connectionSaver = null;
    // Milliseconds before next command can be sent to Game Server
    private long MIN_DELAY;

    // Constant - Milliseconds of inactivity before NOP is sent
    private static final long NOP_DELAY = 40*1000;

    // Constructor
    private GameServerDriver() {
        HOSTNAME = "margot.di.unipi.it";
        PORT = 8421;
        MIN_DELAY = 500;
        lastCommandSent = 0;
    }

    public void setMinDelay(long delay) {
        MIN_DELAY = delay;
    }


    /*
     * List of commands to send to Game Server
     *
     * All the following methods do is:
     * -	generate command (according to Game Protocol);
     * -	send command to Game Server;
     * -	get response from Game Server;
     * -	do basic preprocessing
     *
     * These methods return a GameServerResponse instance containing:
     * -	the response code (enum ResponseCode)
     * -	some free text describing the outcome of the request
     * -    eventual (partially processed) data
     */

    // <game> NOP : resets command timer
    public GameServerResponse sendNOP(String gameName) {
        String command = gameName + " NOP";
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> NEW : creates new game
    public GameServerResponse sendNEW(String gameName, String options) {
        String command = "NEW " + gameName + " " + options;
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> JOIN <player-name> <nature> <role> <user-info> : joins game
    public GameServerResponse sendJOIN(String gameName, String playerName, char nature, String userInfo) {
        String command = gameName + " JOIN " + playerName + " " + nature + " - " + userInfo;
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> START : starts game (if possible)
    public GameServerResponse sendSTART(String gameName) {
        String command = gameName + " START";
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> LOOK : request to see the map
    public GameServerResponse sendLOOK(String gameName) {
        String command = gameName + " LOOK";
        String[] rawResponse = sendCommandLong(command, "ENDOFMAP").split(" ", 2);

        ResponseCode code = ResponseCode.fromString(rawResponse[0]);
        GameServerResponse res;

        if(code == ResponseCode.OK) {
            String[] content = rawResponse[1].split("\n");
            res = new GameServerResponse(code, Arrays.copyOfRange(content, 1, content.length), "Looked");
        } else {
            res = new GameServerResponse(code, null, rawResponse[1]);
        }

        return res;
    }

    // <game> MOVE <direction> : request to move player
    public GameServerResponse sendMOVE(String gameName, char direction) {
        String command = gameName + " MOVE " + direction;
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        if(code == ResponseCode.OK && rawResponse[1].equals("blocked")) {
            // Didn't actually move
            code = ResponseCode.ERROR;
        }

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> SHOOT <direction> : request to shoot
    public GameServerResponse sendSHOOT(String gameName, char direction) {
        String command = gameName + " SHOOT " + direction;
        String[] rawResponse = sendCommand(command);

        ResponseCode code = ResponseCode.fromString(rawResponse[0]);
        GameServerResponse res;

        if(code == ResponseCode.OK) {
            if(rawResponse[1].length() == 1) {
                // A single char -> map cell
                res = new GameServerResponse(code, rawResponse[1].charAt(0), "Shot");
            } else {
                // A normal string -> didn't actually shoot
                res = new GameServerResponse(ResponseCode.ERROR, null, rawResponse[1]);
            }
        } else {
            res = new GameServerResponse(code, null, rawResponse[1]);
        }

        return res;
    }

    // <game> STATUS : request to get the status
    public GameServerResponse sendSTATUS(String gameName) {
        String command = gameName + " STATUS";
        String[] rawResponse = sendCommandLong(command, "ENDOFSTATUS").split(" ", 2);

        ResponseCode code = ResponseCode.fromString(rawResponse[0]);
        GameServerResponse res;

        if(code == ResponseCode.OK) {
            String[] content = rawResponse[1].split("\n");
            res = new GameServerResponse(code, Arrays.copyOfRange(content, 1, content.length), "Status");
        } else {
            res = new GameServerResponse(code, null, rawResponse[1]);
        }

        return res;
    }

    // <game> ACCUSE <player> : accuse another player of being an impostor
    public GameServerResponse sendACCUSE(String gameName, String playerName) {
        String command = gameName + " ACCUSE " + playerName;
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> JUDGE <player> <nature> : judge a player as Human or AI
    public GameServerResponse sendJUDGE(String gameName, String playerName, String nature) {
        String command = gameName + " JUDGE " + playerName + " " + nature;
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }

    // <game> LEAVE <reason> : player leaves game
    public GameServerResponse sendLEAVE(String gameName, String reason) {
        String command = gameName + " LEAVE " + reason;
        String[] rawResponse = sendCommand(command);
        ResponseCode code = ResponseCode.fromString(rawResponse[0]);

        return new GameServerResponse(code, null, rawResponse[1]);
    }


    // Send a general command and wait for the response
    private synchronized String[] sendCommand(String command) {
        String rawResponse;

        if(!isConnected()) { return "FAIL Not connected".split(" ", 2); }

        try {
            // Send request
            forcedWait();
            outSocket.println(command);

            // Wait for response
            rawResponse = inSocket.readLine();
            if(rawResponse == null) {
                rawResponse = "FAIL Socket closed";
                closeConnection();
            }
        } catch (IOException e) {
            rawResponse = "FAIL Can not communicate with Game Server";
            closeConnection();
        }
        lastCommandSent = System.currentTimeMillis();

        return rawResponse.split(" ", 2);
    }

    // Send command with a long response to be retrieved
    private synchronized String sendCommandLong(String command, String endString) {
        String rawResponse, line;

        if(!isConnected()) { return "FAIL Not connected"; }

        try {
            // Send request
            forcedWait();
            outSocket.println(command);

            // Read response
            rawResponse = inSocket.readLine();
            if(!rawResponse.contains("ERROR")) {
                rawResponse = rawResponse.concat("\n");
                while(!(line = inSocket.readLine()).contains(endString)) {
                    rawResponse = rawResponse.concat(line + "\n");
                }
            }
        } catch (IOException e) {
            rawResponse = "FAIL Can not communicate with Game Server";
            closeConnection();
        } catch (NullPointerException e) {
            rawResponse = "FAIL Socket closed";
            closeConnection();
        }
        lastCommandSent = System.currentTimeMillis();

        return rawResponse;
    }

    // Forces to wait at least MIN_DELAY
    private void forcedWait() {
        long timeDifference = System.currentTimeMillis() - lastCommandSent;

        if(timeDifference < MIN_DELAY) {
            try { Thread.sleep(MIN_DELAY - timeDifference); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }


    @Override
    public synchronized void openConnection()  {
        if(isConnected()) return;
        super.openConnection();

        if(isConnected()) {
            lastCommandSent = 0;

            connectionSaver = new Thread(() -> {
                System.out.println("NOP Thread: Started");

                long timeToWait = NOP_DELAY;

                try {
                    while(!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(timeToWait);

                        long timeSinceLastCommand = System.currentTimeMillis() - lastCommandSent;

                        if(timeSinceLastCommand > NOP_DELAY) {
                            GameServerResponse res = sendNOP(StateManager.getInstance().getGameName());
                            if(res.code != ResponseCode.OK) {
                                System.err.println("Nop Thread: " + res.freeText);
                                return;
                            }
                        } else {
                            timeToWait = NOP_DELAY - timeSinceLastCommand;
                        }
                    }
                } catch (InterruptedException ignored) { }
                System.out.println("NOP Thread: Interrupted");
            });
            connectionSaver.setDaemon(true);
            connectionSaver.start();
            System.out.println("Game Server connection open");
        }
    }

    @Override
    public synchronized void closeConnection() {
        if(!isConnected()) return;
        super.closeConnection();

        connectionSaver.interrupt();
        connectionSaver = null;

        System.out.println("Game Server connection closed");
    }
}