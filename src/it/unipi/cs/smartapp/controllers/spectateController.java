package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.application.Platform;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.PlayerSettings;
import it.unipi.cs.smartapp.statemanager.GameState;
import it.unipi.cs.smartapp.statemanager.Player;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class spectateController implements Controller {
    // Singleton components
    private StateManager stateMgr;
    private PlayerSettings playerSettings;

    // Complex element managers
    private ChatManager chat;
    private TableController table;

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
        // Initialize singleton components
        stateMgr = StateManager.getInstance();
        playerSettings = PlayerSettings.getInstance();

        // Initialize complex element managers
        chat = new ChatManager(chatPane);
        table = new TableController(tblPlayers);

        System.out.println("Spectate Controller done");
    }

    @Override
    public void updateContent() {
        // Reduce commands delay
        GameServerDriver.getInstance().setMinDelay(50);

        // Setup chat
        chat.setupChat();

        // Initialize status and map
        Controllers.updateStatus(true);
        Controllers.updateMap();

        // Prepare the interface
        lobbyName.setText(stateMgr.getGameName());
        table.createTable(false, null);
        stateMgr.map.drawMap(mapCanvas, stateMgr.playersList, null);
        leftSubPanel.toFront();
        rightSubPanel.toFront();

        // Keyboard events
        spectatePanel.setOnKeyPressed(keyEvent -> {
            KeyCode key = keyEvent.getCode();
            if (key == playerSettings.getFlipLeft()) Controllers.flipVisiblePane(leftSubPanel);
            else if (key == playerSettings.getFlipRight()) Controllers.flipVisiblePane(rightSubPanel);
        });

        // Setup automatic LOOK and STATUS
        automaticActions = Controllers.setupPoolExecutor();
        automaticActions.scheduleWithFixedDelay(this::updateStatus,
                500, 300, TimeUnit.MILLISECONDS
        );
        automaticActions.scheduleWithFixedDelay(this::updateMap,
                500, 100, TimeUnit.MILLISECONDS
        );
    }

    private void updateStatus() {
        Controllers.updateStatus(true);

        Platform.runLater(() -> {
            tblPlayers.refresh();

            // Check finished game
            if (stateMgr.getGameState() == GameState.FINISHED) {
                automaticActions.shutdownNow();

                Alert message = new Alert(Alert.AlertType.INFORMATION);
                message.setTitle("Game finished!");
                message.setContentText("Click OK to see final results or close this message to stay in game.");
                message.showAndWait();
                Renderer.getInstance().show("resultScene");
            }
        });
    }

    private void updateMap() {
        Controllers.updateMap();
        Platform.runLater(() -> stateMgr.map.drawMap(mapCanvas, stateMgr.playersList, null));
    }

    @FXML
    public void btnGoBackPressed() {
        automaticActions.shutdownNow();
        Controllers.closeGameServerConnection();
        chat.closeChat();
        Renderer.getInstance().show("mainMenu");
    }
}