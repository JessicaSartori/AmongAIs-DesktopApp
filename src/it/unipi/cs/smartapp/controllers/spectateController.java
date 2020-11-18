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

import java.util.HashMap;

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
    private HashMap<Character, Image> sprites = null;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();
        chatSystem.setMessageCallback(new MessageCallback(this));

        canvasContext = gameCanvas.getGraphicsContext2D();

        sprites = new HashMap<>();
        loadSprites();

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

    private void loadSprites(){
        Image icon = new Image("it/unipi/cs/smartapp/sprites/transparent.png");
        sprites.put(' ', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/grass.png"); // Grass
        sprites.put('.', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/wall.png"); // Wall
        sprites.put('#', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/river.png"); // River
        sprites.put('~', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/ocean.png"); // Ocean
        sprites.put('@', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/trap.png"); // Trap
        sprites.put('!', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/energy.png"); // Energy recharge
        sprites.put('$', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/barrier.png"); // Barrier
        sprites.put('&', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/flagRed.png"); // Flag team 0
        sprites.put('X', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/flagBlue.png"); // Flag team 1
        sprites.put('x', icon);

        icon = new Image("it/unipi/cs/smartapp/sprites/explosion.png");
        sprites.put('*', icon);

        icon = new Image("it/unipi/cs/smartapp/sprites/playerDownBlue.png");
        sprites.put('1', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerDownRed.png");
        sprites.put('2', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerLeftBlue.png");
        sprites.put('3', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerLeftRed.png");
        sprites.put('4', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerRightBlue.png");
        sprites.put('5', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerRightRed.png");
        sprites.put('6', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerTopBlue.png");
        sprites.put('7', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerTopRed.png");
        sprites.put('8', icon);
    }

    private Image setSprite(Character value) {
        if(Character.isUpperCase(value) && value != 'X') return sprites.get('8');
        if(Character.isLowerCase(value) && value != 'x') return sprites.get('7');
        return sprites.get(value);
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
