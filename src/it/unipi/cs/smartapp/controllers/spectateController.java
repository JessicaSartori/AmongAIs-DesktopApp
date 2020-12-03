package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.GameState;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class spectateController implements Controller {

    private StateManager stateMgr;
    private PlayerSettings playerSettings;

    private GraphicsContext canvasContext;
    private ChatManager chat;
    private TableManager table;

    private ScheduledThreadPoolExecutor automaticActions;

    @FXML
    private Label lobbyName;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private Pane leftSubPanel, rightSubPanel;
    @FXML
    private AnchorPane spectatePanel;
    @FXML
    private ScrollPane chatPane;
    @FXML
    private TableView<Player> tblPlayers;


    public void initialize() {
        stateMgr = StateManager.getInstance();
        playerSettings = PlayerSettings.getInstance();

        canvasContext = mapCanvas.getGraphicsContext2D();
        chat = new ChatManager(chatPane);
        table = new TableManager(tblPlayers);

        leftSubPanel.toFront();
        rightSubPanel.toFront();

        System.out.println("Spectate Controller done");
    }

    @Override
    public void updateContent() {
        GameServerDriver.getInstance().setMinDelay(50);

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

        // Keyboard events
        spectatePanel.setOnKeyPressed(keyEvent -> {
            KeyCode key = keyEvent.getCode();
            if (key == playerSettings.getFlipLeft()) Controllers.flipVisiblePane(leftSubPanel);
            else if (key == playerSettings.getFlipRight()) Controllers.flipVisiblePane(rightSubPanel);
        });

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
        stateMgr.map.drawMap(canvasContext, mapCanvas, stateMgr.playersList, null);
    }

    @FXML
    public void btnGoBackPressed() {
        automaticActions.shutdownNow();
        GameServerDriver.getInstance().closeConnection();
        GameServerDriver.getInstance().setMinDelay(500);

        chat.closeChat();

        Renderer.getInstance().show("mainMenu");
    }
}