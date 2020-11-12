package it.unipi.cs.smartapp.statemanager;

public class StateManager {

    static private StateManager instance = null;

    static public StateManager getInstance() {
        if(instance == null) instance = new StateManager();
        return instance;
    }


    private String username = null;
    private String currentGameName = null;

    private Integer team = null;
    private Integer loyalty = null;

    private Integer energy = null;
    private Integer score = null;
    private Boolean creator = null;
    private Character symbol = null;

    private Character[][] gameMap = null;

    /*
     * Setters
     */
    public void setUsername(String s) { username = s; }
    public void setCurrentGameName(String s) {
        currentGameName = s;
    }
    public void setTeam(Integer t) {
        team = t;
    }
    public void setLoyalty(Integer l) {
        loyalty = l;
    }
    public void setEnergy(Integer e) { energy = e; }
    public void setScore(Integer s) { score = s; }
    public void setCreator(Boolean c) { creator = c; }
    public void setSymbol(Character c) { symbol = c; }
    public void setGameMap(Character[][] m) { gameMap = m; }

    /*
     * Getters
     */
    public String getUsername() {
        return username;
    }
    public String getCurrentGameName() {
        return currentGameName;
    }
    public Integer getTeam() { return team; }
    public Integer getLoyalty() { return loyalty; }
    public Integer getEnergy() { return energy; }
    public Integer getScore() { return score; }
    public Boolean getCreator() { return creator; }
    public Character getSymbol() { return symbol; }
    public Character[][] getGameMap() { return gameMap; }

}
