package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.Tournament;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import it.unipi.cs.smartapp.drivers.ChatSystemDriver;
import it.unipi.cs.smartapp.drivers.LeagueManagerDriver;
import it.unipi.cs.smartapp.screens.Renderer;

import java.util.ArrayList;


public class tournamentController implements Controller {

    private LeagueManagerDriver lmDriver;
    private TournamentTable table;
    private StateManager stateManager;

    @FXML
    private TableView<Tournament> tblTournaments;
    @FXML
    private Button btnJoin, btnWithdraw, btnShowInfo;

    public void initialize() {
        lmDriver = LeagueManagerDriver.getInstance();
        stateManager = StateManager.getInstance();
        table = new TournamentTable(tblTournaments);

        System.out.println("Tournament Controller done");
    }

    @Override
    public void updateContent() {
        // Get tournaments list from LM
        lmDriver.getTournaments();

        // Show all tournaments in the scene
        table.createTable();

        // Disable buttons
        btnJoin.disableProperty().bind(Bindings.isEmpty(tblTournaments.getSelectionModel().getSelectedItems()));
        btnWithdraw.disableProperty().bind(Bindings.isEmpty(tblTournaments.getSelectionModel().getSelectedItems()));
        btnShowInfo.disableProperty().bind(Bindings.isEmpty(tblTournaments.getSelectionModel().getSelectedItems()));
    }

    @FXML
    private void btnGoBackPressed() {
        Renderer.getInstance().show("mainMenu");
    }

    @FXML
    private void btnJoinPressed() {
        // Perform JOIN with LM
        Tournament t = tblTournaments.getSelectionModel().getSelectedItem();
        String res = lmDriver.joinTournament(t.tournamentName.get(), stateManager.getUsername());
        Alert message = new Alert(Alert.AlertType.INFORMATION);
        message.setTitle(t.tournamentName.get());
        message.setContentText(res);
        message.showAndWait();
    }

    @FXML
    private void btnWithdrawPressed() {
        // Perform Withdraw with LM
        Tournament t = tblTournaments.getSelectionModel().getSelectedItem();
        String response = lmDriver.withdrawTournament(t.tournamentName.get(), stateManager.getUsername());
        Alert message = new Alert(Alert.AlertType.INFORMATION);
        message.setTitle(t.tournamentName.get());
        message.setContentText(response);
        message.showAndWait();
    }

    @FXML
    private void btnShowInfoPressed() {
        // Perform Show participants with LM
        Tournament t = tblTournaments.getSelectionModel().getSelectedItem();

        // Pass in some way the tournament name to the tournament info scene
        stateManager.TournamentName = t.tournamentName.get();

        Renderer.getInstance().show("tournamentInfoScene");
    }
}