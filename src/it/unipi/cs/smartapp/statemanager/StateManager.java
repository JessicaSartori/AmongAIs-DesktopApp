package it.unipi.cs.smartapp.statemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    public Player player;
    public HashMap<String, Player> players;
    public ObservableList<Player> playersList;

    // Setters
    public void setUsername(String s) { currentUsername = s; }
    public void setGameState(GameState s) { gameStatus.setState(s); }

    // Getters
    public String getUsername() { return currentUsername; }
    public String getGameName() { return currentGameName; }
    public Boolean getCreator() { return gameStatus.isCreated(); }
    public GameState getGameState() { return gameStatus.getState(); }


    public void setInGame(String gameName, Boolean created) {
        player = new Player();
        players = new HashMap<>();
        players.put(currentUsername, player);

        playersList = FXCollections.observableArrayList();

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
        Player pl = new Player();
        pl.updateWith(info);

        if (pl.getUsername().equals(currentUsername)) {
            playersList.remove(player);
            player.updateWith(info);
            playersList.add(player);
        } else {
            Player old = players.put(pl.getUsername(), pl);
            if (old != null) {
                playersList.remove(old);
            }
            playersList.add(pl);
        }
    }

    public void addNewPlayer(String name) {
        Player pl = new Player(name);
        players.put(pl.getUsername(), pl);
        playersList.add(pl);
    }

    public void removePlayer(String name) {
        Player pl = players.remove(name);
        playersList.remove(pl);
    }
}