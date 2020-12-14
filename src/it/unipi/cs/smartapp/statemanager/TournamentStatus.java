package it.unipi.cs.smartapp.statemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;

public class TournamentStatus {

    static private TournamentStatus instance = null;
    public HashMap<String, Tournament> tournamentsList;
    public ObservableList<Tournament> tournamentTableList;

    static public TournamentStatus getInstance() {
        if(instance == null) instance = new TournamentStatus();
        return instance;
    }

    private TournamentStatus() {
        tournamentsList = new HashMap<>();
        tournamentTableList = FXCollections.observableArrayList();
    }
}
