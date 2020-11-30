package it.unipi.cs.smartapp.statemanager;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {
    private Character symbol = null;
    private Integer[] position = {-1 , -1};
    private Integer loyalty = null;
    private Integer energy = null;

    private StringProperty username = null;
    private IntegerProperty team = null;
    private IntegerProperty score = null;
    private StringProperty state = null;

    public static String usernamePropertyName = "username";
    public static String teamPropertyName = "team";
    public static String scorePropertyName = "score";
    public static String statePropertyName = "state";

    /*
     * Constructors
     */
    public Player() {}
    public Player(String username) { setUsername(username); }

    /*
     * Setters
     */
    public void setSymbol(Character s) { symbol = s; }
    public void setPosition(Integer[] p) { position = p; }
    public void setLoyalty(Integer l) { loyalty = l; }
    public void setEnergy(Integer e) { energy = e; }

    public void setUsername(String username) { usernameProperty().set(username); }
    public void setTeam(Integer team) { teamProperty().set(team); }
    public void setScore(Integer score) { scoreProperty().set(score); }
    public void setState(String state) { stateProperty().set(state); }

    /*
     * Getters
     */
    public Character getSymbol() { return symbol; }
    public Integer[] getPosition() { return position; }
    public Integer getLoyalty() { return loyalty; }
    public Integer getEnergy() { return energy; }

    public String getUsername() { return usernameProperty().get(); }
    public Integer getTeam() { return teamProperty().get(); }
    public Integer getScore() { return scoreProperty().get(); }
    public String getState() { return stateProperty().get(); }

    /*
     * Property methods
     */
    public StringProperty usernameProperty() {
        if(username == null) username = new SimpleStringProperty(this, usernamePropertyName);
        return username;
    }
    public IntegerProperty teamProperty() {
        if(team == null) team = new SimpleIntegerProperty(this, teamPropertyName);
        return team;
    }
    public IntegerProperty scoreProperty() {
        if(score == null) score = new SimpleIntegerProperty(this, scorePropertyName);
        return score;
    }
    public StringProperty stateProperty() {
        if(state == null) state = new SimpleStringProperty(this, statePropertyName);
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
