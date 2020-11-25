package it.unipi.cs.smartapp.statemanager;

public enum GameState {
    LOBBY,      // Match is in lobby
    ACTIVE,     // Match is active
    FINISHED;   // Match is finished

    static public GameState fromString(String s) {
        return switch (s) {
            case "LOBBY" -> LOBBY;
            case "ACTIVE" -> ACTIVE;
            default -> FINISHED;
        };
    }
}