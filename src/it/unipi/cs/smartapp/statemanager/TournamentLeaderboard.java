package it.unipi.cs.smartapp.statemanager;

// This class is actually a single player score of a tournament
public class TournamentLeaderboard {
    public String playerName = "";
    public Integer playerRank = 0;
    public Integer playerScore = 0;

    @Override
    public String toString() {
        return playerName + " - Rank: " + playerRank + " - Score: " + playerScore;
    }
}
