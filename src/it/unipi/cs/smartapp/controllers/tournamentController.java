package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import it.unipi.cs.smartapp.drivers.ChatSystemDriver;
import it.unipi.cs.smartapp.drivers.LeagueManagerDriver;
import it.unipi.cs.smartapp.screens.Renderer;


public class tournamentController implements Controller {

    @FXML
    private Label lblMessage;
    @FXML
    private TextField txtTournamentName;

    private ChatSystemDriver chatSystem;
    private LeagueManagerDriver lmDriver;

    public void initialize() {
        lblMessage.setText("");
        chatSystem = ChatSystemDriver.getInstance();
        lmDriver = LeagueManagerDriver.getInstance();

        System.out.println("Tournament Controller done");
    }

    @Override
    public void updateContent() {
        lblMessage.setText("");

        chatSystem.openConnection();
    }

    @FXML
    private void btnGoBackPressed() {
        Renderer.getInstance().show("mainMenu");
    }

    @FXML
    private void btnJoinPressed() {
        if (txtTournamentName.getText().trim().isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red");
            lblMessage.setText("The tournament name must be valid.");
        } else {
            lmDriver.joinTournament(txtTournamentName.getText());
            lblMessage.setStyle("-fx-text-fill: green");
            lblMessage.setText("Ok, joined!");
        }
    }

    @FXML
    private void btnWithdrawPressed() {
        if (txtTournamentName.getText().trim().isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red");
            lblMessage.setText("The tournament name must be valid.");
        } else {
            lmDriver.withdrawTournament(txtTournamentName.getText());
            lblMessage.setStyle("-fx-text-fill: green");
            lblMessage.setText("Ok, withdrawn!");
        }
    }
}