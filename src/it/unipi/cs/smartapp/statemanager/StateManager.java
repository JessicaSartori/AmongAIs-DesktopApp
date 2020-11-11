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
    private Boolean creator = null;

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
    public void setCreator(Boolean c) { creator = c; }

    /*
     * Getter
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
    public Boolean getCreator() { return creator; }


}
