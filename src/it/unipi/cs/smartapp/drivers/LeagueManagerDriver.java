package it.unipi.cs.smartapp.drivers;

public class LeagueManagerDriver {
    private static LeagueManagerDriver instance = null;
    private final ChatSystemDriver chatSystemDriver;

    public static LeagueManagerDriver getInstance() {
        if(instance == null) instance = new LeagueManagerDriver();
        return instance;
    }

    private LeagueManagerDriver() {
        chatSystemDriver = ChatSystemDriver.getInstance();
    }

    public void joinTournament(String TournamentName) {
        if(!chatSystemDriver.isConnected()) chatSystemDriver.openConnection();

        chatSystemDriver.sendPOST("#LEAGUE", TournamentName + " join");
    }

    public void withdrawTournament(String TournamentName) {
        if(!chatSystemDriver.isConnected()) chatSystemDriver.openConnection();

        chatSystemDriver.sendPOST("#LEAGUE", TournamentName + " withdraw");
    }
}