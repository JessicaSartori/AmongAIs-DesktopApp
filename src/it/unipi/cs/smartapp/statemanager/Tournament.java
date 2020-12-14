package it.unipi.cs.smartapp.statemanager;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

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
    public StringProperty minParticipants = null;
    public StringProperty maxParticipants = null;

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

    public StringProperty minParticipantsProperty() {
        if(minParticipants == null) minParticipants = new SimpleStringProperty(this, minPropertyParticipants);
        return minParticipants;
    }

    public StringProperty maxParticipantsProperty() {
        if(maxParticipants == null) maxParticipants = new SimpleStringProperty(this, maxPropertyParticipants);
        return maxParticipants;
    }

    @Override
    public String toString() {
        return tournamentName + " - " + gameType + " - " + startTournament.toString();
    }
}
