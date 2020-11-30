package it.unipi.cs.smartapp.drivers;

public class LeagueManagerDriver {
    private static LeagueManagerDriver instance = null;
    private ChatSystemDriver chatSystemDriver;

    public static LeagueManagerDriver getInstance() {
        if(instance == null) instance = new LeagueManagerDriver();
        return instance;
    }

    private LeagueManagerDriver() {
        chatSystemDriver = ChatSystemDriver.getInstance();
        chatSystemDriver.openConnection();
    }

    public void joinTournament(String TournamentName) {
        chatSystemDriver.sendPOST("#LEAGUE", TournamentName + " join");
    }

    public void withdrawTournament(String TournamentName) {
        chatSystemDriver.sendPOST("#LEAGUE", TournamentName + " withdraw");
    }
}
