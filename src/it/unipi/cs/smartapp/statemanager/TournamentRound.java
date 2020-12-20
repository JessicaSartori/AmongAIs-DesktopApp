package it.unipi.cs.smartapp.statemanager;

import java.util.ArrayList;

public class TournamentRound {
    public ArrayList<Match> rounds;

    public TournamentRound() {
        rounds = new ArrayList<>();
    }

    public static class Match {
        public ArrayList<String> participants;
        public String startDate;

        public Match() {
            participants = new ArrayList<>();
        }
    };
}
