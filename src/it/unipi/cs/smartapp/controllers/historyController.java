package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.LogDriver;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.GlobalPlayerStatistics;
import it.unipi.cs.smartapp.statemanager.PlayerGameHistory;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.HashMap;

import static it.unipi.cs.smartapp.screens.Renderer.*;

public class historyController implements Controller {

    private LogDriver logDriver;
    private StateManager stateManager;
    private GameHistoryTable table;

    @FXML
    private TableView<PlayerGameHistory> tableMatchHistory;
    @FXML
    private Label lblScore, lblTotalScore, lblName, lblPlayedMatches, lblAccuracy, lblDeaths, lblKillDeathRatio, lblTotalKills;
    @FXML
    private VBox globalStats;

    public void initialize() {
        logDriver = LogDriver.getInstance();
        stateManager = StateManager.getInstance();
        stateManager.gamesHistory = new HashMap<>();
        stateManager.gamesTableHistory = FXCollections.observableArrayList();
        table = new GameHistoryTable(tableMatchHistory);

        System.out.println("Game History Controller done.");
    }

    @Override
    public void updateContent() {
        tableMatchHistory.getItems().clear();

        table.createTable();

        logDriver.getPlayerHistory(stateManager.getUsername());

        GlobalPlayerStatistics stats = logDriver.getGlobalPlayerStatistics(stateManager.getUsername());

        if (stats == null) {
            // Hide VBox
            globalStats.setVisible(false);
        } else {
            lblName.setText(lblName.getText() + " " + stateManager.getUsername());
            lblScore.setText(lblScore.getText() + " " + stats.bestScore);
            lblAccuracy.setText(lblAccuracy.getText() + " " + stats.totalAccuracy);
            lblPlayedMatches.setText(lblPlayedMatches.getText() + " " + stats.playedMatches);
            lblDeaths.setText(lblDeaths.getText() + " " + stats.totalDeaths);
            lblKillDeathRatio.setText(lblKillDeathRatio.getText() + " " + stats.totalKillDeathRatio);
            lblTotalKills.setText(lblTotalKills.getText() + " " + stats.totalKills);
            lblTotalScore.setText(lblTotalScore.getText() + " " + stats.totalScore);
        }
    }

    @FXML
    private void btnGoBackPressed() { Renderer.getInstance().show("mainMenu"); }
}
