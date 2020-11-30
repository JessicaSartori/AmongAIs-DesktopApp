package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.Player;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
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
    private TableManager table;

    private ScheduledThreadPoolExecutor lookExecutor;

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

        // Update status
        Controllers.updateStatus(true);

        lookExecutor = new ScheduledThreadPoolExecutor(1);
        lookExecutor.setRemoveOnCancelPolicy(true);
        lookExecutor.scheduleWithFixedDelay(this::btnUpdMapPressed, 0, PlayerSettings.getInstance().getMapFreq(), TimeUnit.MILLISECONDS);

        // Setup table with players info
        table.createTable();

        // Update map
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, gameCanvas, stateMgr.playersList, null);
    }

    @FXML
    private void btnUpdStatusPressed() { Controllers.updateStatus(true); }

    @FXML
    private void btnUpdMapPressed() {
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, gameCanvas, stateMgr.playersList, null);
    }

    @FXML
    public void btnGoBackPressed() {
        lookExecutor.shutdownNow();
        GameServerDriver.getInstance().closeConnection();

        chat.closeChat();

        Renderer.getInstance().show("mainMenu");
    }
}
