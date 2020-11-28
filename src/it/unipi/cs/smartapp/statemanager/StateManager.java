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
    public ObservableList<String> playerList;

    // Setters
    public void setUsername(String s) { currentUsername = s; }
    public void setGameState(GameState s) { gameStatus.setState(s); }

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
        player = new Player();
        players = new HashMap<>();
        players.put(currentUsername, player);

        playerList = FXCollections.observableArrayList();

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

        if (pl.username.equals(currentUsername)) {
            playerList.remove(player.team + "\t\t\t" + player.username + "\t\t\t\t" + player.score + "\t" + player.state);
            player.updateWith(info);
            playerList.add(player.team + "\t\t\t" + player.username + "\t\t\t\t" + player.score + "\t" + player.state);

        } else {
            Player old = players.put(pl.username, pl);
            if (old != null) {
                playerList.remove(old.team + "\t\t\t" + old.username + "\t\t\t\t" + old.score + "\t" + old.state);
            }
            playerList.add(pl.team + "\t\t\t" + pl.username + "\t\t\t\t" + pl.score + "\t" + pl.state);
        }
    }

    public void addNewPlayer(String name) {
        Player pl = new Player(name);
        players.put(pl.username, pl);
        playerList.add(pl.team + "\t\t\t" + pl.username + "\t\t\t\t" + pl.score + "\t" + pl.state);
    }

    public void removePlayer(String name) {
        Player pl = players.remove(name);
        playerList.remove(pl.team + "\t\t\t" + pl.username + "\t\t\t\t" + pl.score + "\t" + pl.state);
    }
}