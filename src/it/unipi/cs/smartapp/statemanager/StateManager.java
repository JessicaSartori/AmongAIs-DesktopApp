package it.unipi.cs.smartapp.statemanager;

import it.unipi.cs.smartapp.controllers.Controllers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


public class StateManager {

    static private StateManager instance = null;

    static public StateManager getInstance() {
        if(instance == null) instance = new StateManager();
        return instance;
    }

    private String privateUsername = null;

    private String currentUsername = null;
    private String currentGameName = null;

    private GameStatus gameStatus;
    public MapStatus map;
    public ConcurrentLinkedQueue<ChatMessage> newMessages;

    public Player player;
    public HashMap<String, Player> players;
    public ObservableList<Player> playersList;

    // Setters
    public void setPrivateUsername(String s) { privateUsername = s; }
    public void setUsername(String s) { currentUsername = s; }
    public void setGameState(GameState s) { gameStatus.setState(s); }

    // Getters
    public String getPrivateUsername() { return privateUsername; }
    public String getUsername() { return currentUsername; }
    public String getGameName() { return currentGameName; }
    public Boolean getCreator() { return gameStatus.isCreated(); }
    public GameState getGameState() { return gameStatus.getState(); }

    // For Tournament Info Scene
    public String TournamentName;

    public void setInGame(String gameName, Boolean created, Boolean spectator) {
        players = new HashMap<>();
        playersList = FXCollections.observableArrayList();

        if(!spectator) {
            player = new Player();
            players.put(currentUsername, player);
            playersList.add(player);
        }

        gameStatus = new GameStatus(gameName, created);
        map = null;
        newMessages = new ConcurrentLinkedQueue<>();

        currentGameName = gameName;
    }

    public void updateGameState(String info) {
        String[] tokens = info.split("[ =]");
        int size = 32;
        char ratio = 'Q';

        for(int i=0; i < tokens.length; i += 2) {
            String keyword = tokens[i], value = tokens[i+1];
            switch (keyword) {
                case "state": gameStatus.setState(GameState.fromString(value)); break;
                case "size": size = Integer.parseInt(value); break;
                case "ratio": ratio = value.charAt(0); break;
            }
        }
        if (map == null) map = new MapStatus(size, ratio);
    }

    public synchronized void updatePlayerStatus(String info) {
        Map<String, String> playerInfo = Player.stringToMap(info);

        String username = playerInfo.get("name");
        if (username.equals(currentUsername)) {
            // Current player status
            player.updateWith(playerInfo);
        } else {
            // Other player status
            Player pl = players.get(username);

            if(pl != null) {
                // Player already in the list
                //System.out.println("BEFORE - " + pl.getTeam() + " playerInfo " + playerInfo);
                pl.updateWith(playerInfo);
                System.out.println("PL != NULL " + pl.getUsername() + " team " + pl.getTeam());
            } else {
                // New player
                pl = new Player();
                pl.updateWith(playerInfo);
                System.out.println("name " + pl.getUsername() + " team " + pl.getTeam());
                players.put(pl.getUsername(), pl);
                playersList.add(pl);
            }
        }
    }

    public synchronized void addNewPlayer(String name) {
        if(players.get(name) != null) return;

        Player pl = new Player(name);
        players.put(pl.getUsername(), pl);
        playersList.add(pl);
    }

    public synchronized void removePlayer(String name) {
        Player pl = players.remove(name);
        playersList.remove(pl);
    }
}