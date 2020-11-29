package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

import it.unipi.cs.smartapp.drivers.*;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.ChatMessage;


public class spectateController implements Controller {
    private StateManager stateMgr;
    private ChatSystemDriver chatSystem;

    private GraphicsContext canvasContext;
    private ChatManager chat;

    @FXML
    private Label lobbyName;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private ScrollPane chatPane;
    @FXML
    private ListView<String> listPlayers;


    public void initialize() {
        stateMgr = StateManager.getInstance();
        chatSystem = ChatSystemDriver.getInstance();

        canvasContext = gameCanvas.getGraphicsContext2D();
        chat = new ChatManager(chatPane);

        System.out.println("Spectate Controller done");
    }

    @Override
    public void updateContent() {
        // Prepare the interface
        lobbyName.setText(stateMgr.getGameName());

        // Prepare player list
        listPlayers.setItems(stateMgr.playerList);

        // Setup chat
        chat.resetChat();
        chatSystem.openConnection();
        chatSystem.setMessageCallback(() -> {
            ChatMessage msg = stateMgr.newMessages.poll();
            if(msg != null) chat.processMessage(msg);
        });
        chatSystem.sendNAME(stateMgr.getUsername());
        chatSystem.sendJOIN(stateMgr.getGameName());

        // Update status
        Controllers.updateStatus(true);

        // Update map
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, gameCanvas);
    }

    @FXML
    private void btnUpdStatusPressed() { Controllers.updateStatus(true); }

    @FXML
    private void btnUpdMapPressed() {
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, gameCanvas);
    }

    @FXML
    public void btnGoBackPressed() { Controllers.quit(); }
}
