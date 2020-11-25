package it.unipi.cs.smartapp.statemanager;

public class ChatMessage {

    public final String channel, user, text;

    public ChatMessage(String channel, String user, String text) {
        this.channel = channel;
        this.user = user;
        this.text = text;
    }
}
