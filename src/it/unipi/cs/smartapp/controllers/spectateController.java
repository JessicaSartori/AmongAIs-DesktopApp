package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.ChatMessage;
import it.unipi.cs.smartapp.statemanager.PlayerStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import it.unipi.cs.smartapp.drivers.*;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;

import java.util.HashMap;
import java.util.Map;


public class spectateController implements Controller {
    private StateManager stateMgr;
    private GameServerDriver gameServer;
    private ChatSystemDriver chatSystem;

    private GraphicsContext canvasContext;

    @FXML
    private Label txtLobby;
    @FXML
    private TextArea txtChat;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private ListView<String> PlayersList = new ListView<>();


    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();

        canvasContext = gameCanvas.getGraphicsContext2D();

        System.out.println("Spectate Controller done");
    }

    @Override
    public void updateContent() {
        // Prepare the interface
        txtLobby.setText(stateMgr.getGameName());
        txtChat.setText("");

        // Setup chat
        chatSystem.openConnection();
        chatSystem.setMessageCallback(() -> {
            ChatMessage message = stateMgr.newMessages.poll();
            if(message == null) return;

            if(!stateMgr.getGameName().equals(message.channel)) txtChat.appendText("(" + message.channel + ") ");
            txtChat.appendText(message.user + ": " + message.text + "\n");
        });
        chatSystem.sendNAME(stateMgr.getUsername());
        chatSystem.sendJOIN(stateMgr.getGameName());
        //chatSystem.sendJOIN("#GLOBAL");

        // Update status
        updateStatus();

        // Update the map
        updateMap();
    }

    // Update general Status
    public void updateStatus() {
        GameServerResponse res = gameServer.sendSTATUS(stateMgr.getGameName());

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
        for (int i = 1; i < data.length; i++) {
            String PL = data[i].substring(4); // Remove "PL: "
            stateMgr.updatePlayerStatus(PL);
        }

        ObservableList<String> finalList = FXCollections.observableArrayList();
        HashMap<String, PlayerStatus> listPlayers = stateMgr.getListOfPlayer();

        for (Map.Entry<String, PlayerStatus> set : listPlayers.entrySet()) {
            PlayerStatus pl = set.getValue();
            String playerListName = pl.team + "\t\t\t" + set.getKey() + "\t\t\t" + pl.score + "\t" + pl.state;
            finalList.add(playerListName);
        }

        PlayersList.setItems(finalList);
    }

    // Update gameMap
    public void updateMap() {
        GameServerResponse response = gameServer.sendLOOK(stateMgr.getGameName());

        if (response.code != ResponseCode.OK) {
            System.err.println(response.freeText);
            return;
        }
        System.out.println(response.freeText);

        stateMgr.map.setGameMap((String[]) response.data);
        stateMgr.map.drawMap(canvasContext, gameCanvas);
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) {
        // Close connection with game server
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getGameName(), "Leaving the game");
        if (response.code != ResponseCode.OK) { System.err.println(response.freeText); }
        else { System.out.println(response.freeText); }
        gameServer.closeConnection();

        // Unsubscribe from all chat channels and close connection
        chatSystem.sendLEAVE(stateMgr.getGameName());
        chatSystem.closeConnection();

        Renderer.getInstance().show("mainMenu");
    }

    @FXML
    private void btnUpdMapPressed(ActionEvent event) { updateMap(); }

    @FXML
    private void btnUpdStatusPressed(ActionEvent event) { updateStatus(); }
}
