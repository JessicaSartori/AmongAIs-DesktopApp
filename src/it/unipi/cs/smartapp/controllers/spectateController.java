package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class spectateController implements Controller {

    private StateManager stateMgr;
    private PlayerSettings playerSettings;

    private GraphicsContext canvasContext;
    private ChatManager chat;
    private TableManager table;

    private ScheduledThreadPoolExecutor automaticActions;

    private boolean gameEnded = false;

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

        // Setup chat
        chat.setupChat();

        // Initialize status and map
        Controllers.updateStatus(true);
        Controllers.updateMap();

        // Prepare the interface
        lobbyName.setText(stateMgr.getGameName());
        table.createTable();
        stateMgr.map.drawMap(canvasContext, mapCanvas, stateMgr.playersList, null);

        // Keyboard events
        spectatePanel.setOnKeyPressed(keyEvent -> {
            KeyCode key = keyEvent.getCode();
            if (key == playerSettings.getFlipLeft()) Controllers.flipVisiblePane(leftSubPanel);
            else if (key == playerSettings.getFlipRight()) Controllers.flipVisiblePane(rightSubPanel);
        });

        // Setup automatic LOOK and STATUS
        automaticActions = Controllers.setupPoolExecutor();
        automaticActions.scheduleWithFixedDelay(this::updateStatus,
                500, PlayerSettings.getInstance().getStatusFreq(), TimeUnit.MILLISECONDS
        );
        automaticActions.scheduleWithFixedDelay(this::updateMap,
                500, PlayerSettings.getInstance().getMapFreq(), TimeUnit.MILLISECONDS
        );
    }

    private void updateStatus() {
        Controllers.updateStatus(true);

        Platform.runLater(() -> {
            // Check finished game
            if (stateMgr.getGameState() == GameState.FINISHED && !gameEnded) {
                gameEnded = true;
                Alert message = new Alert(Alert.AlertType.INFORMATION);
                message.setTitle("Game finished!");
                message.setContentText("Click OK to see final results or close this message to stay in game.");
                Optional<ButtonType> result = message.showAndWait();

                if(result.get() == ButtonType.OK) {
                    quitScene("resultScene");
                }
            }
        });
    }

    private void updateMap() {
        Controllers.updateMap();
        Platform.runLater(() -> stateMgr.map.drawMap(canvasContext, mapCanvas, stateMgr.playersList, null));
    }

    @FXML
    public void btnGoBackPressed() { quitScene("mainMenu"); }

    private void quitScene(String nextScene) {
        automaticActions.shutdownNow();
        Controllers.closeGameServerConnection();
        chat.closeChat();
        Renderer.getInstance().show(nextScene);
    }
}