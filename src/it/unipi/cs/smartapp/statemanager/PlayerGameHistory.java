package it.unipi.cs.smartapp.statemanager;

import javafx.beans.property.*;

public class PlayerGameHistory {
    public static String matchPropertyId = "matchId";
    public static String playerPropertyAccuracy = "playerAccuracy";
    public static String playerPropertyMatchName = "playerMatchName";
    public static String playerPropertyRealName = "playerRealName";
    public static String playerPropertyKills = "playerKills";
    public static String playerPropertyLeaderboardPosition = "playerLeaderboardPosition";
    public static String playerPropertyScore = "playerScore";
    public static String playerPropertyKilled= "playerKilled";

    public StringProperty matchId = null;
    public DoubleProperty playerAccuracy = null;
    public StringProperty playerMatchName = null;
    public StringProperty playerRealName = null;
    public IntegerProperty playerKills = null;
    public IntegerProperty playerLeaderboardPosition = null;
    public IntegerProperty playerScore = null;
    public BooleanProperty playerKilled = null;

    public StringProperty matchIdProperty() {
        if(matchId == null) matchId = new SimpleStringProperty(this, matchPropertyId);
        return matchId;
    }

    public DoubleProperty playerAccuracyProperty() {
        if(playerAccuracy == null) playerAccuracy = new SimpleDoubleProperty(this, playerPropertyAccuracy);
        return playerAccuracy;
    }

    public StringProperty playerMatchNameProperty() {
        if(playerMatchName == null) playerMatchName = new SimpleStringProperty(this, playerPropertyMatchName);
        return playerMatchName;
    }

    public StringProperty playerRealNameProperty() {
        if(playerRealName == null) playerRealName = new SimpleStringProperty(this, playerPropertyRealName);
        return playerRealName;
    }

    public IntegerProperty playerKillsProperty() {
        if(playerKills == null) playerKills = new SimpleIntegerProperty(this, playerPropertyKills);
        return playerKills;
    }

    public IntegerProperty playerLeaderboardPositionProperty() {
        if(playerLeaderboardPosition == null) playerLeaderboardPosition = new SimpleIntegerProperty(this, playerPropertyLeaderboardPosition);
        return playerLeaderboardPosition;
    }

    public IntegerProperty playerScoreProperty() {
        if(playerScore == null) playerScore = new SimpleIntegerProperty(this, playerPropertyScore);
        return playerScore;
    }

    public BooleanProperty playerKilledProperty() {
        if(playerKilled == null) playerKilled = new SimpleBooleanProperty(this, playerPropertyKilled);
        return playerKilled;
    }

    @Override
    public String toString() {
        return matchId.get() + " - " + playerAccuracy.get() + " - " + playerKilled.get();
    }
}
