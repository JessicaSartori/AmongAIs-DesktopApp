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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

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
    private Canvas GameCanvas;

    private GraphicsContext canvasContext;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();
        chatSystem.setMessageCallback(new MessageCallback(this));

        canvasContext = GameCanvas.getGraphicsContext2D();

        System.out.println("Spectate Controller done");
    }

    @Override
    public void updateContent() {
        txtUsername.setText(stateMgr.getUsername());
        txtLobby.setText(stateMgr.getCurrentGameName());

        // Subscribe to chat channels
        chatSystem.sendNAME(stateMgr.getUsername());
        chatSystem.sendJOIN(stateMgr.getCurrentGameName());
        chatSystem.sendJOIN("#GLOBAL");

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

        int xCanvas = 0, yCanvas = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                setColor(charMap[r][c]);

                canvasContext.fillRect(xCanvas, yCanvas, cellSize, cellSize);

                xCanvas += cellSize;
            }
            yCanvas += cellSize;
            xCanvas = 0;
        }
    }

    private void setColor(Character value) {

        Color color = Color.web("#000000");

        switch (value) {
            case '.' -> color = Color.web("#009432"); // Grass
            case '#' -> color = Color.web("#718093"); // Wall
            case '~' -> color = Color.web("#00FFFF"); // River
            case '@' -> color = Color.web("#006b6b"); // Ocean
            case '!' -> color = Color.web("#ff8a00"); // Trap
            case '$' -> color = Color.web("#fffd50"); // Energy recharge
            case '&' -> color = Color.web("#3b1909"); // Barrier
            case 'X' -> color = Color.web("#fdbda7"); // Flag team 0
            case 'x' -> color = Color.web("#b7beff"); // Flag team 1
            default -> { // Players
                if (value == stateMgr.getSymbol()) {
                    // Current player
                    color = (stateMgr.getTeam() == 0) ? Color.web("#ff0000") : Color.web("#0000ff");
                } else {
                    // Other players
                    if (Character.isUpperCase(value)) color = Color.web("#f25656");
                    else if (Character.isLowerCase(value)) color = Color.web("#0652DD");
                }
            }
        }
        canvasContext.setFill(color);
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) {
        // Unsubscribe from all chat channels
        chatSystem.sendLEAVE(stateMgr.getCurrentGameName());
        chatSystem.sendLEAVE("#GLOBAL");
        // TODO: should close also chat connection?

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
