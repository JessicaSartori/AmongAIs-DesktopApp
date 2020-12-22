package it.unipi.cs.smartapp.statemanager;

import javafx.beans.property.*;
import javafx.scene.control.Button;

import java.util.HashMap;
import java.util.Map;


public class Player {

    public static String usernamePropertyName = "username";
    public static String scorePropertyName = "score";
    public static String statePropertyName = "state";

    private Character symbol = null;
    private Integer[] position = {-1 , -1};
    private Integer loyalty = null;
    private Integer energy = null;
    private Integer team = null;

    private StringProperty username = null;
    private IntegerProperty score = null;
    private StringProperty state = null;

    public static String accusePropertyName = "accuse";
    public static String judgeHumanPropertyName = "judgeHuman";
    public static String judgeAIPropertyName = "judgeAI";

    public ObjectProperty<Button> accuse = null;
    public ObjectProperty<Button> judgeHuman = null;
    public ObjectProperty<Button> judgeAI = null;

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
    public void setTeam(Integer t) { team = t; }

    public void setUsername(String username) { usernameProperty().set(username); }
    public void setScore(Integer score) { scoreProperty().set(score); }
    public void setState(String state) { stateProperty().set(state); }

    /*
     * Getters
     */
    public Character getSymbol() { return symbol; }
    public Integer[] getPosition() { return position; }
    public Integer getLoyalty() { return loyalty; }
    public Integer getEnergy() { return energy; }
    public Integer getTeam() { return team; }

    public String getUsername() { return usernameProperty().get(); }
    public Integer getScore() { return scoreProperty().get(); }
    public String getState() { return stateProperty().get(); }

    /*
     * Property methods
     */
    public StringProperty usernameProperty() {
        if(username == null) username = new SimpleStringProperty(this, usernamePropertyName);
        return username;
    }
    public IntegerProperty scoreProperty() {
        if(score == null) score = new SimpleIntegerProperty(this, scorePropertyName);
        return score;
    }
    public StringProperty stateProperty() {
        if(state == null) state = new SimpleStringProperty(this, statePropertyName);
        return state;
    }

    public void updateWith(Map<String, String> info) {
        String newSymbol = info.get("symbol");
        if(newSymbol != null) setSymbol(newSymbol.charAt(0));

        String newName = info.get("name");
        if(newName != null) setUsername(newName);

        String newTeam = info.get("team");
        if(newTeam != null) setTeam(Integer.parseInt(newTeam));

        String newLoyalty = info.get("loyalty");
        if(newLoyalty != null) setLoyalty(Integer.parseInt(newLoyalty));

        String newEnergy = info.get("energy");
        if(newEnergy != null) setEnergy(Integer.parseInt(newEnergy));

        String newScore = info.get("score");
        if(newScore != null) setScore(Integer.parseInt(newScore));

        String newX = info.get("x");
        String newY = info.get("y");
        if(newX != null && newY != null)
            setPosition(new Integer[]{Integer.parseInt(newX), Integer.parseInt(newY)});

        String newState = info.get("state");
        if(newState != null) setState(newState.toLowerCase());
    }

    public static Map<String, String> stringToMap(String info) {
        HashMap<String, String> map = new HashMap<>();
        String[] tokens = info.split("[ =]");
        for(int i=0; i < tokens.length; i += 2) map.put(tokens[i], tokens[i+1]);
        return map;
    }

    /*
     * Property methods
     */
    public ObjectProperty<Button> accuseProperty() {
        if(accuse == null) accuse = new SimpleObjectProperty<Button>(this, accusePropertyName);
        return accuse;
    }
    public ObjectProperty<Button> judgeHumanProperty() {
        if(judgeHuman == null) judgeHuman = new SimpleObjectProperty<Button>(this, judgeHumanPropertyName);
        return judgeHuman;
    }
    public ObjectProperty<Button> judgeAIProperty() {
        if(judgeAI == null) judgeAI = new SimpleObjectProperty<Button>(this, judgeAIPropertyName);
        return judgeAI;
    }
}
