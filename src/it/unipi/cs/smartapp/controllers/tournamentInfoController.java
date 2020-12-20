package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.LeagueManagerDriver;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.TournamentLeaderboard;
import it.unipi.cs.smartapp.statemanager.TournamentRound;
import it.unipi.cs.smartapp.statemanager.TournamentStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class tournamentInfoController implements Controller {

    @FXML
    private Button btnGoBack;
    @FXML
    private Label lblTournamentName, lblGameType;
    @FXML
    private ListView<String> listParticipants, listSchedule, listLeaderboard;

    private StateManager stateManager;
    private LeagueManagerDriver leagueManagerDriver;
    private TournamentStatus tournamentStatus;

    public void initialize() {
        lblTournamentName.setText("");
        lblGameType.setText("");

        stateManager = StateManager.getInstance();
        leagueManagerDriver = LeagueManagerDriver.getInstance();
        tournamentStatus = TournamentStatus.getInstance();

        System.out.println("Tournament Info Controller done");
    }

    @Override
    public void updateContent() {
        // Clear listViews
        listParticipants.getItems().clear();
        listSchedule.getItems().clear();
        listLeaderboard.getItems().clear();

        // Get info from TournamentState
        String tName = stateManager.TournamentName;
        lblTournamentName.setText(tName);
        lblGameType.setText(tournamentStatus.tournamentsList.get(tName).gameType.get());

        // Get participants
        ArrayList<String> participants = leagueManagerDriver.getTournamentParticipants(tName);

        // Show participants
        if (participants.size() == 0) {
            listParticipants.getItems().add("There are no participants yet.");
        } else {
            for (String player : participants) {
                listParticipants.getItems().add(player);
            }
        }

        // Get Tournament Schedule
        ArrayList<TournamentRound> rounds = leagueManagerDriver.getTournamentSchedule(tName);

        if (rounds.size() == 0) {
            listSchedule.getItems().add("There are no scheduled matches yet.");
        } else {
            for (int i = 0; i < rounds.size(); i++) {
                TournamentRound tr = rounds.get(i);
                for (int j = 0; j < tr.rounds.size(); j++) {
                    String matchDate = tr.rounds.get(j).startDate;
                    Integer numParticipants = tr.rounds.get(j).participants.size();
                    listSchedule.getItems().add("Match starts: " + matchDate + " - Participants: " + numParticipants);
                }
            }
        }

        // Get Tournament Leaderboard
        ArrayList<TournamentLeaderboard> leaderboard = leagueManagerDriver.getTournamentLeaderboard(tName);

        // Show Tournament Leaderboard
        if (leaderboard.size() == 0) {
            listLeaderboard.getItems().add("There are no results yet.");
        } else {
            for (TournamentLeaderboard player : leaderboard) {
                listLeaderboard.getItems().add(player.toString());
            }
        }
    }

    @FXML
    public void btnGoBackPressed() { Renderer.getInstance().show("tournaments"); }
}
