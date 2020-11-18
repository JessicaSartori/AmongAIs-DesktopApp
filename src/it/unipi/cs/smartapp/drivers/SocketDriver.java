package it.unipi.cs.smartapp.drivers;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public abstract class SocketDriver {
    // Server hostname and host port
    public String HOSTNAME;
    public int PORT;

    // Socket and input/output streams
    private Socket socket = null;
    protected BufferedReader inSocket = null;
    protected PrintWriter outSocket = null;


    // Open a connection to the host server and create the input/output streams
    public synchronized void openConnection() {
        try {
            // Create new socket
            socket = new Socket(HOSTNAME, PORT);

            // Create input stream from socket
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            inSocket = new BufferedReader(isr);

            // Create output stream to socket
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osw);
            outSocket = new PrintWriter(bw, true);

        } catch (UnknownHostException e) {
            System.err.println("Can not find " + HOSTNAME);
            closeConnection();
        } catch (IOException e) {
            System.err.println(e.toString());
            closeConnection();
        }
    }

    // Close the connection to the host server
    public synchronized void closeConnection() {
        try { socket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        try { inSocket.close(); }
        catch (Exception e) { System.err.println("clearSocket: " + e.toString()); }

        try { outSocket.close(); }
        catch (Exception e) { System.out.println("clearSocket: " + e.toString()); }

        socket = null;
        inSocket = null;
        outSocket = null;
    }

    // Check whether a connection is open
    public boolean isConnected() {
        return (socket != null);
    }
}
