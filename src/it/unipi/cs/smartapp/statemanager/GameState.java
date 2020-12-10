package it.unipi.cs.smartapp.statemanager;

public enum GameState {
    LOBBY,      // Match is in lobby
    ACTIVE,     // Match is active
    FINISHED;   // Match is finished

    static public GameState fromString(String s) {
        switch (s) {
            case "LOBBY":
                return LOBBY;
            case "ACTIVE":
                return ACTIVE;
            default:
                return FINISHED;
        }
    }
}
