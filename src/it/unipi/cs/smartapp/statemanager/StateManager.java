package it.unipi.cs.smartapp.statemanager;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateManager {

    static private StateManager instance = null;

    static public StateManager getInstance() {
        if(instance == null) instance = new StateManager();
        return instance;
    }


    private String currentUsername = null;
    private String currentGameName = null;

    private GameStatus gameStatus;
    public MapStatus map;
    public ConcurrentLinkedQueue<ChatMessage> newMessages;

    public PlayerStatus player;
    public HashMap<String, PlayerStatus> playerList;

    // Setters
    public void setUsername(String s) { currentUsername = s; }

    // Getters
    public String getUsername() { return currentUsername; }
    public String getGameName() { return currentGameName; }
    public Integer getTeam() { return player.team; }
    public Integer getLoyalty() { return player.loyalty; }
    public Integer getEnergy() { return player.energy; }
    public Integer getScore() { return player.score; }
    public Boolean getCreator() { return gameStatus.isCreated(); }
    public Character getSymbol() { return player.symbol; }
    public GameState getGameState() { return gameStatus.getState(); }


    public void setInGame(String gameName, Boolean created) {
        player = new PlayerStatus();
        playerList = new HashMap<>();

        gameStatus = new GameStatus(gameName, created);
        map = new MapStatus();
        newMessages = new ConcurrentLinkedQueue<>();

        currentGameName = gameName;
    }

    public void updateGameState(String info) {
        String[] tokens = info.split("[ =]");

        for(int i=0; i < tokens.length; i += 2) {
            String keyword = tokens[i], value = tokens[i+1];
            switch (keyword) {
                case "state" -> gameStatus.setState(GameState.fromString(value));
                case "size" -> map.setMapSize(Integer.parseInt(value));
            }
        }
    }

    public void updatePlayerStatus(String info) {
        PlayerStatus pl = new PlayerStatus();
        pl.updateWith(info);
        playerList.put(pl.username, pl);

        // Added to guarantee current player coordinates are updated correctly
        if(pl.username.equals(player.username))
            player.updateWith(info);
    }
}