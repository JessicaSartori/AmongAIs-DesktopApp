package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ScrollPane;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.PlayerSettings;
import it.unipi.cs.smartapp.statemanager.Player;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class spectateController implements Controller {

    private StateManager stateMgr;

    private GraphicsContext canvasContext;
    private ChatManager chat;
    private TableManager table;

    private ScheduledThreadPoolExecutor automaticActions;

    @FXML
    private Label lobbyName;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private ScrollPane chatPane;
    @FXML
    private TableView<Player> tblPlayers;


    public void initialize() {
        stateMgr = StateManager.getInstance();

        canvasContext = gameCanvas.getGraphicsContext2D();
        chat = new ChatManager(chatPane);
        table = new TableManager(tblPlayers);

        System.out.println("Spectate Controller done");
    }

    @Override
    public void updateContent() {
        // Prepare the interface
        lobbyName.setText(stateMgr.getGameName());

        // Setup chat
        chat.setupChat();

        // Initialize status
        Controllers.updateStatus(true);

        // Setup table with players info
        table.createTable();

        // Initialize map
        updateMap();

        // Setup automatic LOOK and STATUS
        automaticActions = new ScheduledThreadPoolExecutor(2);
        automaticActions.setRemoveOnCancelPolicy(true);
        automaticActions.scheduleWithFixedDelay(this::updateStatus,
                500, PlayerSettings.getInstance().getStatusFreq(),
                TimeUnit.MILLISECONDS
        );
        automaticActions.scheduleWithFixedDelay(this::updateMap,
                500, PlayerSettings.getInstance().getMapFreq(),
                TimeUnit.MILLISECONDS
        );
    }

    private void updateStatus() {
        Controllers.updateStatus(true);
    }

    private void updateMap() {
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, gameCanvas, stateMgr.playersList, null);
    }

    @FXML
    public void btnGoBackPressed() {
        automaticActions.shutdownNow();
        GameServerDriver.getInstance().closeConnection();

        chat.closeChat();

        Renderer.getInstance().show("mainMenu");
    }
}