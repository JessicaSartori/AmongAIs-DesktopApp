package it.unipi.cs.smartapp.statemanager;

import javafx.beans.property.*;

public class Tournament {
    public static String tournamentPropertyName = "tournamentName";
    public static String gamePropertyType = "gameType";
    public static String startPropertySubscriptions = "startSubscriptions";
    public static String endPropertySubscriptions = "endSubscriptions";
    public static String startPropertyTournament = "startTournament";
    public static String endPropertyTournament = "endTournament";
    public static String minPropertyParticipants = "minParticipants";
    public static String maxPropertyParticipants = "maxParticipants";

    public StringProperty tournamentName = null;
    public StringProperty gameType = null;
    public StringProperty startSubscriptions = null;
    public StringProperty endSubscriptions = null;
    public StringProperty startTournament = null;
    public StringProperty endTournament = null;
    public IntegerProperty minParticipants = null;
    public IntegerProperty maxParticipants = null;

    public StringProperty tournamentNameProperty() {
        if(tournamentName == null) tournamentName = new SimpleStringProperty(this, tournamentPropertyName);
        return tournamentName;
    }

    public StringProperty gameTypeProperty() {
        if(gameType == null) gameType = new SimpleStringProperty(this, gamePropertyType);
        return gameType;
    }

    public StringProperty startSubscriptionsProperty() {
        if(startSubscriptions == null) startSubscriptions = new SimpleStringProperty(this, startPropertySubscriptions);
        return startSubscriptions;
    }

    public StringProperty endSubscriptionsProperty() {
        if(endSubscriptions == null) endSubscriptions = new SimpleStringProperty(this, endPropertySubscriptions);
        return endSubscriptions;
    }

    public StringProperty startTournamentProperty() {
        if(startTournament == null) startTournament = new SimpleStringProperty(this, startPropertyTournament);
        return startTournament;
    }

    public StringProperty endTournamentProperty() {
        if(endTournament == null) endTournament = new SimpleStringProperty(this, endPropertyTournament);
        return endTournament;
    }

    public IntegerProperty minParticipantsProperty() {
        if(minParticipants == null) minParticipants = new SimpleIntegerProperty(this, minPropertyParticipants);
        return minParticipants;
    }

    public IntegerProperty maxParticipantsProperty() {
        if(maxParticipants == null) maxParticipants = new SimpleIntegerProperty(this, maxPropertyParticipants);
        return maxParticipants;
    }

    @Override
    public String toString() {
        return tournamentName.get() + " - " + gameType.get() + " - " + startTournament.get();
    }
}
