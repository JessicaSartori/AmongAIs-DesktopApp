package it.unipi.cs.smartapp.statemanager;

import java.util.HashMap;

public class StateManager {

    static private StateManager instance = null;

    static public StateManager getInstance() {
        if(instance == null) instance = new StateManager();
        return instance;
    }

    // Player state
    private String username = null;
    private Boolean creator = null;

    // Players state
    public PlayerStatus player;
    public HashMap<String, PlayerStatus> playerList = null;

    // Map state
    public MapStatus map;

    // Game state
    private String currentGameName = null;
    private String gameState = null;

    public String[] newMessage = null;

    /*
     * Setters
     */
    public void setUsername(String s) { username = s; }
    public void setCurrentGameName(String s) { currentGameName = s; }
    public void setEnergy(Integer e) { player.energy = e; }
    public void setScore(Integer s) { player.score = s; }
    public void setGameState(String s) { gameState = s; }

    /*
     * Getters
     */
    public String getUsername() { return username; }
    public String getCurrentGameName() { return currentGameName; }
    public Integer getTeam() { return player.team; }
    public Integer getLoyalty() { return player.loyalty; }
    public Integer getEnergy() { return player.energy; }
    public Integer getScore() { return player.score; }
    public Boolean getCreator() { return creator; }
    public Character getSymbol() { return player.symbol; }
    public String getGameState() { return gameState; }

    public void setInGame(String gameName, Boolean created) {
        player = new PlayerStatus();
        playerList = new HashMap<>();

        map = new MapStatus();

        currentGameName = gameName;
        gameState = "LOBBY";
        creator = created;
    }

    public void updateGameState(String info) {
        String[] tokens = info.split("[ =]");

        for(int i=0; i < tokens.length; i += 2) {
            String keyword = tokens[i], value = tokens[i+1];
            switch (keyword) {
                case "name" -> currentGameName = value;
                case "state" -> gameState = value;
                case "size" -> map.setMapSize(Integer.parseInt(value));
            }
        }
    }

    public void updatePlayerStatus(String info) {
        PlayerStatus pl = new PlayerStatus();
        pl.updateWith(info);
        playerList.put(pl.username, pl);
    }
}