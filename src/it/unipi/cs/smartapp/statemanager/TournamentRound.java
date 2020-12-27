package it.unipi.cs.smartapp.statemanager;

import java.util.ArrayList;

public class TournamentRound {
    public ArrayList<Match> matches;

    public TournamentRound() {
        matches = new ArrayList<>();
    }

    public static class Match {
        public ArrayList<String> participants;
        public String startDate;
        public String id;

        public Match() {
            participants = new ArrayList<>();
        }
    };
}
