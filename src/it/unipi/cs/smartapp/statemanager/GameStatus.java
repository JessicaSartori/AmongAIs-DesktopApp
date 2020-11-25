package it.unipi.cs.smartapp.statemanager;

public class GameStatus {

    public final String gameName;
    public final boolean created;
    private GameState state;

    public GameStatus(String name, boolean c) {
        gameName = name;
        created = c;
        state = GameState.LOBBY;
    }

    public void setState(GameState state) { this.state = state; }

    public GameState getState() { return state; }
    public boolean isCreated() { return created; }
}
