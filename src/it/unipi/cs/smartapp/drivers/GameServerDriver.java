package it.unipi.cs.smartapp.drivers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameServerDriver {

    // Instance reference
    private static final GameServerDriver instance = new GameServerDriver();
    // Game Server hostname
    private String hostname = "margot.di.unipi.it";
    // Game Server port
    private int port = 8421;
    // Socket (connection to Game Server)
    private static Socket socket = null;
    // Read from socket (input stream)
    private BufferedReader inSocket = null;
    // Write on socket (output stream)
    private PrintWriter outSocket = null;

    // Constructor
    private GameServerDriver(){
        try {
            // Create new socket
            socket = new Socket(hostname, port);

            // For debug purposes
            System.out.println("Client socket: "+ socket);

            // Create input stream from socket
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            inSocket = new BufferedReader(isr);

            // Create output stream to socket
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osw);
            outSocket = new PrintWriter(bw, true);
        }
        catch (UnknownHostException e) {
            System.err.println("Can not find " + hostname);
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("I/O exception connecting to " + hostname);
            System.exit(1);
        }
    }

    // Make instance available to the outside
    public static GameServerDriver getInstance() {
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
        String rawResponse;

        try {
            outSocket.println(command);
            rawResponse =  inSocket.readLine();
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // NEW <game> : creates new game
    public String[] sendNEW(String gameName) {
        String command = "NEW " + gameName;
        String rawResponse;

        try {
            outSocket.println(command);
            rawResponse =  inSocket.readLine();
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // <game> JOIN <player-name> <nature> <role> <user-info> : joins game
    public String[] sendJOIN(String gameName, String playerName, char nature, String userInfo) {
        // <role> currently does nothing, sending - as placeholder
        String command = gameName + " JOIN " + playerName + " " + nature + " - " + userInfo;
        String rawResponse;

        try {
            outSocket.println(command);
            rawResponse =  inSocket.readLine();
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // <game> START : starts game (if possible)
    public String[] sendSTART(String gameName) {
        String command = gameName + " START";
        String rawResponse;

        try {
            outSocket.println(command);
            rawResponse =  inSocket.readLine();
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // <game> LOOK : request to see the map
    public String[] sendLOOK(String gameName) {
        String command = gameName + " LOOK";
        String line, rawResponse="";

        try {
            outSocket.println(command);

            // The response from LOOK is multiline
            line = inSocket.readLine();

            while(!line.contains("ENDOFMAP")) {
                if(line.equals("OK "))
                    rawResponse += line;
                else
                    rawResponse += line + "\n";

                line =  inSocket.readLine();
            }

        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // <game> MOVE <direction> : request to move player
    public String[] sendMOVE(String gameName, char direction) {
        String command = gameName + " MOVE " + direction;
        String rawResponse;

        try {
            outSocket.println(command);
            rawResponse =  inSocket.readLine();
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // <game> SHOOT <direction> : request to shoot
    public String[] sendSHOOT(String gameName, char direction) {
        String command = gameName + " SHOOT " + direction;
        String rawResponse;

        try {
            outSocket.println(command);
            rawResponse =  inSocket.readLine();
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // <game> STATUS : request to get the status
    public String[] sendSTATUS(String gameName) {
        String command = gameName + " STATUS";
        String line, rawResponse="";

        try {
            outSocket.println(command);

            // The response from STATUS is multiline
            line = inSocket.readLine();

            while(!line.contains("ENDOFSTATUS")) {
                if(line.equals("OK "))
                    rawResponse += line;
                else
                    rawResponse += line + "\n";

                line =  inSocket.readLine();

            }
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

    // <game> ACCUSE <player> : CURRENTLY NOT IMPLEMENTED
    public void sendACCUSE() {
        // TODO Implement when made available from Game Server
    }

    // <game> LEAVE <reason> : player leaves game
    public String[] sendLEAVE(String gameName, String reason) {
        String command = gameName + " LEAVE " + reason;
        String rawResponse;

        try {
            outSocket.println(command);
            rawResponse =  inSocket.readLine();
        } catch (IOException e) {
            rawResponse = "ERROR Can not communicate with Game Server";
        }

        String response[] = rawResponse.split(" ", 2);

        return response;
    }

}
