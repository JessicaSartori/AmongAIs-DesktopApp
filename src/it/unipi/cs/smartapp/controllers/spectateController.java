package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.PlayerSettings;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class spectateController implements Controller {
    private StateManager stateMgr;

    private GraphicsContext canvasContext;
    private ChatManager chat;

    private ScheduledThreadPoolExecutor lookExecutor;

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
        chat.setupChat();

        // Update status
        Controllers.updateStatus(true);

        lookExecutor = new ScheduledThreadPoolExecutor(1);
        lookExecutor.setRemoveOnCancelPolicy(true);
        lookExecutor.scheduleWithFixedDelay(this::btnUpdMapPressed, 0, PlayerSettings.getInstance().getMapFreq(), TimeUnit.MILLISECONDS);

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
    public void btnGoBackPressed() {
        lookExecutor.shutdownNow();
        GameServerDriver.getInstance().closeConnection();

        chat.closeChat();

        Renderer.getInstance().show("mainMenu");
    }
}
