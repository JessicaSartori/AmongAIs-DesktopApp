package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.LeagueManagerDriver;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.TournamentLeaderboard;
import it.unipi.cs.smartapp.statemanager.TournamentRound;
import it.unipi.cs.smartapp.statemanager.TournamentStatus;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class tournamentInfoController implements Controller {

    @FXML
    private Button btnGoBack;
    @FXML
    private Label lblTournamentName, lblGameType;
    @FXML
    private ListView<String> listParticipants, listLeaderboard;
    @FXML
    private ListView<Text> listSchedule;

    private StateManager stateManager;
    private LeagueManagerDriver leagueManagerDriver;
    private TournamentStatus tournamentStatus;

    public void initialize() {
        lblTournamentName.setText("");
        lblGameType.setText("");

        stateManager = StateManager.getInstance();
        leagueManagerDriver = LeagueManagerDriver.getInstance();
        tournamentStatus = TournamentStatus.getInstance();

        listSchedule.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                String[] tournamentId = listSchedule.getSelectionModel().getSelectedItem().getText().split(" ");
                StringSelection selection = new StringSelection(tournamentId[0]);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                Alert message = new Alert(Alert.AlertType.INFORMATION);
                message.setTitle("Tournament copied!");
                message.setContentText("Tournament name copied, you can paste it in the join screen.");
                message.show();
            }
        });

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
        ArrayList<TournamentRound> tRounds = leagueManagerDriver.getTournamentSchedule(tName);

        if (tRounds.size() == 0) {
            Text message = new Text("There are no scheduled matches yet.");
            listSchedule.getItems().add(message);
        } else {
            for (int i = 0; i < tRounds.size(); i++) {
                ArrayList<TournamentRound.Match> tMatches = tRounds.get(i).matches;

                for (int j = 0; j < tMatches.size(); j++) {
                    String matchDate = tMatches.get(j).startDate;
                    Integer numParticipants = tMatches.get(j).participants.size();
                    String tournamentName = tMatches.get(j).id;

                    try {
                        DateFormat formatter = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.ENGLISH);
                        Date initDate = formatter.parse(matchDate);
                        String now = formatter.format(Calendar.getInstance().getTime());
                        Date timeStamp = formatter.parse(now);

                        Text row = new Text(tournamentName + " starts at " + matchDate + " - Participants: " + numParticipants);

                        if (timeStamp.after(initDate)) {
                            row.setStyle("-fx-text-fill: red");
                        } else {
                            row.setStyle("-fx-text-fill: green");
                        }

                        listSchedule.getItems().add(row);

                    } catch (ParseException e) {
                        System.err.println("Error parsing the dates.");
                    }
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
