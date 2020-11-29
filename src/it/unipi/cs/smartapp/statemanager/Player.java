package it.unipi.cs.smartapp.statemanager;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {
    public Character symbol = null;
    public Integer[] position = {-1 , -1};
    public Integer loyalty = null;
    public Integer energy = null;

    private StringProperty username = null;
    private IntegerProperty team = null;
    private IntegerProperty score = null;
    private StringProperty state = null;

    /*
     * Constructors
     */
    public Player() {}
    public Player(String username) { setUsername(username); }

    /*
     * Setters
     */
    public void setUsername(String username) { usernameProperty().set(username); }
    public void setTeam(Integer team) { teamProperty().set(team); }
    public void setScore(Integer score) { scoreProperty().set(score); }
    public void setState(String state) { stateProperty().set(state); }

    /*
     * Getters
     */
    public String getUsername() { return usernameProperty().get(); }
    public Integer getTeam() { return teamProperty().get(); }
    public Integer getScore() { return scoreProperty().get(); }
    public String getState() { return stateProperty().get(); }

    /*
     * Property methods
     */
    public StringProperty usernameProperty() {
        if(username == null) username = new SimpleStringProperty(this, "username");
        return username;
    }
    public IntegerProperty teamProperty() {
        if(team == null) team = new SimpleIntegerProperty(this, "team");
        return team;
    }
    public IntegerProperty scoreProperty() {
        if(score == null) score = new SimpleIntegerProperty(this, "score");
        return score;
    }
    public StringProperty stateProperty() {
        if(state == null) state = new SimpleStringProperty(this, "state");
        return state;
    }

    public void updateWith(String info) {
        String[] tokens = info.split("[ =]");

        for(int i=0; i < tokens.length; i += 2) {
            String keyword = tokens[i], value = tokens[i+1];
            switch (keyword) {
                case "symbol" -> symbol = value.toCharArray()[0];
                case "name" -> setUsername(value);
                case "team" -> setTeam(Integer.parseInt(value));
                case "loyalty" -> loyalty = Integer.parseInt(value);
                case "energy" -> energy = Integer.parseInt(value);
                case "score" -> setScore(Integer.parseInt(value));
                case "x" -> position[0] = Integer.parseInt(value);
                case "y" -> position[1] = Integer.parseInt(value);
                case "state" -> setState(value.toLowerCase());
            }
        }
    }
}
