package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.ChatSystemDriver;
import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

public class spectateController implements Controller {
    private StateManager stateMgr;
    private GameServerDriver gameServer;
    private ChatSystemDriver chatSystem;

    @FXML
    private Label txtUsername;
    @FXML
    private Label txtLobby;
    @FXML
    private TextArea txtChat;
    @FXML
    private Canvas gameCanvas;

    private GraphicsContext canvasContext;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();
        chatSystem.setMessageCallback(new MessageCallback(this));

        canvasContext = gameCanvas.getGraphicsContext2D();

        System.out.println("Spectate Controller done");
    }

    @Override
    public void updateContent() {

        // Add lobby name in the chat
        txtChat.setText("Lobby name: " + stateMgr.getCurrentGameName());

        txtUsername.setText(stateMgr.getUsername());
        txtLobby.setText(stateMgr.getCurrentGameName());

        // Setup chat
        chatSystem.openConnection();
        chatSystem.setMessageCallback(() -> {
            String[] message = stateMgr.newMessage;
            stateMgr.newMessage = null;
            txtChat.appendText("\n(" + message[0] + ") " + message[1] + ": " + message[2]);
        });
        chatSystem.sendNAME(stateMgr.getUsername());
        chatSystem.sendJOIN(stateMgr.getCurrentGameName());
        //chatSystem.sendJOIN("#GLOBAL");

        // Update status
        updateStatus();

        // Update the map
        updateMap();
    }

    // Update general Status
    public void updateStatus() {
        GameServerResponse res = gameServer.sendSTATUS(stateMgr.getCurrentGameName());

        if (res.code != ResponseCode.OK) {
            System.err.println(res.freeText);
            return;
        }
        System.out.println(res.freeText);
        String[] data = (String[]) res.data;

        // Update game status
        String GA = data[0].substring(4); // Remove "GA: "
        stateMgr.updateGameState(GA);

        // Update list of players
        for (int i = 2; i < data.length; i++) {
            String PL = data[i].substring(4); // Remove "PL: "
            stateMgr.updatePlayerStatus(PL);
        }
    }

    // Update gameMap
    public void updateMap() {
        GameServerResponse response = gameServer.sendLOOK(stateMgr.getCurrentGameName());

        if (response.code != ResponseCode.OK) {
            System.err.println(response.freeText);
            return;
        }
        System.out.println(response.freeText);

        stateMgr.map.setGameMap(stringToCharMap((String[]) response.data));
        drawMap();
    }

    private Character[][] stringToCharMap(String[]rows) {
        Integer size = stateMgr.map.getMapSize();
        Character[][] parsedMap = new Character[size][size];

        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                parsedMap[r][c] = rows[r].charAt(c);
            }

        return parsedMap;
    }

    private void drawMap() {
        Integer size = stateMgr.map.getMapSize();
        Integer cellSize = stateMgr.map.getCellSize();
        Character[][] charMap = stateMgr.map.getGameMap();

        // Clear canvas
        canvasContext.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        int xCanvas = cellSize, yCanvas = cellSize;
        for(int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Image sprite = setSprite(charMap[r][c]);
                canvasContext.drawImage(sprite, xCanvas, yCanvas, cellSize, cellSize);

                xCanvas += cellSize;
            }
            yCanvas += cellSize;
            xCanvas = cellSize;
        }
    }

    private Image setSprite(Character value) {
        Integer cellSize = stateMgr.map.getCellSize();

        Image sprite = new Image("it/unipi/cs/smartapp/sprites/transparent.png");

        switch (value) {
            case '.' -> sprite = new Image("it/unipi/cs/smartapp/sprites/grass.png"); // Grass
            case '#' -> sprite = new Image("it/unipi/cs/smartapp/sprites/wall.png"); // Wall
            case '~' -> sprite = new Image("it/unipi/cs/smartapp/sprites/river.png"); // River
            case '@' -> sprite = new Image("it/unipi/cs/smartapp/sprites/ocean.png"); // Ocean
            case '!' -> sprite = new Image("it/unipi/cs/smartapp/sprites/trap.png"); // Trap
            case '$' -> sprite = new Image("it/unipi/cs/smartapp/sprites/energy.png"); // Energy recharge
            case '&' -> sprite = new Image("it/unipi/cs/smartapp/sprites/barrier.png"); // Barrier
            case 'X' -> sprite = new Image("it/unipi/cs/smartapp/sprites/flagRed.png"); // Flag team 0
            case 'x' -> sprite = new Image("it/unipi/cs/smartapp/sprites/flagBlue.png"); // Flag team 1
            default -> { // Players
                if(Character.isUpperCase(value)) sprite = new Image("it/unipi/cs/smartapp/sprites/playerTopRed.png");
                else if(Character.isLowerCase(value)) sprite = new Image("it/unipi/cs/smartapp/sprites/playerTopBlue.png");
            }
        }
        return sprite;
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) {
        // Close connection with game server
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getCurrentGameName(), "Leaving the game");
        if (response.code != ResponseCode.OK) { System.err.println(response.freeText); }
        else { System.out.println(response.freeText); }
        gameServer.closeConnection();

        // Unsubscribe from all chat channels and close connection
        chatSystem.sendLEAVE(stateMgr.getCurrentGameName());
        //chatSystem.sendLEAVE("#GLOBAL");
        chatSystem.closeConnection();

        stateMgr.setCurrentGameName(null);

        Renderer.getInstance().show("mainMenu");
    }

    @FXML
    private void btnUpdateMapPressed(ActionEvent event) { updateMap(); }

    public void txtReceiveMessage(String s) {
        txtChat.appendText("\n" + s);
    }

    class MessageCallback implements Runnable {

        spectateController controller;

        public MessageCallback(spectateController c) {
            controller = c;
        }

        @Override
        public void run() {
            String[] message = StateManager.getInstance().newMessage;
            StateManager.getInstance().newMessage = null;

            controller.txtReceiveMessage(message[1] + ": " + message[2]);
        }
    }
}
