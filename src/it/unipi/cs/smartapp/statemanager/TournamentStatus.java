package it.unipi.cs.smartapp.statemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.HashMap;

public class TournamentStatus {

    static private TournamentStatus instance = null;
    public HashMap<String, Tournament> tournamentsList;
    public ObservableList<Tournament> tournamentTableList;
    public Pair<String, TournamentRound> tournamentRounds;

    static public TournamentStatus getInstance() {
        if(instance == null) instance = new TournamentStatus();
        return instance;
    }

    private TournamentStatus() {
        tournamentsList = new HashMap<>();
        tournamentRounds = new Pair<>("", null);
        tournamentTableList = FXCollections.observableArrayList();
    }

    // Add tournament to the ObservableList
    public void addTournament(Tournament tNew) {
        if (tournamentsList.containsKey(tNew.tournamentName.get())) {
            // Remove old existing tournament
            Tournament tOld = tournamentsList.remove(tNew.tournamentName.get());
            tournamentTableList.removeAll(tOld);

            // Replace it with the updated one
            tournamentsList.put(tNew.tournamentName.get(), tNew);
            tournamentTableList.add(tNew);
        } else {
            tournamentsList.put(tNew.tournamentName.get(), tNew);
            tournamentTableList.add(tNew);
        }
    }
}
