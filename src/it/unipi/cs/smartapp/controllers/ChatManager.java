package it.unipi.cs.smartapp.controllers;

import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import it.unipi.cs.smartapp.statemanager.*;


public class ChatManager {

    private final StateManager stateMgr;

    private final ScrollPane chatPane;
    private final TextFlow chatArea;

    private String fontFamily = "System";
    private double fontSize = 14;
    private final Color[] teamColors = {Color.RED, Color.BLUE};


    public ChatManager(ScrollPane pane) {
        chatPane = pane;
        chatArea = (TextFlow) pane.getContent();
        stateMgr = StateManager.getInstance();
    }

    public void setFontFamily(String f) { fontFamily = f; }
    public void setFontSize(double s) { fontSize = s; }

    public void processMessage(ChatMessage message) {
        if(!stateMgr.getGameName().equals(message.channel)) {
            Text channelTxt =  new Text("(" + message.channel + ") ");
            channelTxt.setFill(Color.YELLOW);
            channelTxt.setFont(Font.font(fontFamily, FontPosture.ITALIC, fontSize));
            chatArea.getChildren().add(channelTxt);
        }

        if(message.user.equals("@GameServer")) {
            handleSystemMessage(message);
            Text msg = new Text(message.user + ": " + message.text + "\n");
            msg.setFill(Color.DARKORANGE);
            msg.setFont(Font.font(fontFamily, FontPosture.ITALIC, fontSize));
            chatArea.getChildren().add(msg);

        } else {
            Text usernameTxt = new Text(message.user);
            usernameTxt.setFont(Font.font(fontFamily, fontSize));
            try {
                int userTeam = stateMgr.players.get(message.user).getTeam();
                usernameTxt.setFill(teamColors[userTeam]);
            } catch (NullPointerException ignored) { }
            chatArea.getChildren().add(usernameTxt);

            Text messageTxt = new Text(": " + message.text + "\n");
            messageTxt.setFont(Font.font(fontFamily, fontSize));
            chatArea.getChildren().add(messageTxt);
        }

        chatPane.setVvalue(1.0);
    }

    public void resetChat() { chatArea.getChildren().remove(0, chatArea.getChildren().size()); }

    private void handleSystemMessage(ChatMessage message) {
        // Handle succeeding shots
        if(message.text.contains(" hit ")) {
            String[] tokens = message.text.split(" ");
            stateMgr.players.get(tokens[2]).state = "killed";
        }

        // Handle game starting
        else if(message.text.equals("Now starting!")) {
            stateMgr.setGameState(GameState.ACTIVE);
        }

        // Handle game ending
        else if(message.text.contains("Game finished!")) {
            stateMgr.setGameState(GameState.FINISHED);
        }

        // Handle player connection
        else if(message.text.contains(" joined ")) {
            String username = message.text.split(" ")[0];
            stateMgr.addNewPlayer(username);
        }

        // Handle player disconnection
        else if(message.text.contains(" left ")) {
            String username = message.text.split(" ")[0];
            stateMgr.removePlayer(username);
        }

        // Handle Emergency Meeting events
        else if(message.text.contains("EMERGENCY MEETING")) {
            String[] tokens = message.text.split(" ");
            if(tokens[2].equals("condamned")) {
                stateMgr.players.get(tokens[3]).state = "killed";
            }
        }

        // Handle final scores
        else if(message.text.matches("\\(.:.\\) \\w+\\s+\\w+\\s+\\d+")) {
            String[] tokens = message.text.split("\s+");
            Player user = stateMgr.players.get(tokens[1]);
            user.state = tokens[2];
            user.score = Integer.parseInt(tokens[3]);
        }
    }
}
