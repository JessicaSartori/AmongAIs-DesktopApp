package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent;

import it.unipi.cs.smartapp.drivers.*;
import it.unipi.cs.smartapp.screens.Renderer;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class gameController implements Controller {
    private StateManager stateMgr;
    private PlayerSettings playerSettings;
    private GameServerDriver gameServer;
    private ChatSystemDriver chatSystem;

    private GraphicsContext canvasContext;

    private Boolean firstTime = true;

    @FXML
    private Label playerName, playerTeam, playerLoyalty, playerEnergy, playerScore;
    @FXML
    private Label responseLabel;
    @FXML
    private TextField txtMessage;
    @FXML
    private TextField txtPlayerVote;
    @FXML
    private TextField txtPlayerJudge;
    @FXML
    private TextArea txtChat;
    @FXML
    private ProgressBar playerEnergyBar;
    @FXML
    private Button btnStartMatch;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private AnchorPane gamePanel;
    @FXML
    private Label lobbyName;
    @FXML
    private ListView<String> PlayersList = new ListView<>();

    public void initialize() {
        stateMgr = StateManager.getInstance();
        playerSettings = PlayerSettings.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();

        canvasContext = mapCanvas.getGraphicsContext2D();

        responseLabel.setText("");

        System.out.println("Game Controller done");
    }

    @Override
    public void updateContent() {
        // Prepare the interface
        btnStartMatch.setVisible(stateMgr.getCreator());
        lobbyName.setText(stateMgr.getGameName());
        responseLabel.setText("");

        // Setup chat
        chatSystem.openConnection();
        chatSystem.setMessageCallback(() -> {
            ChatMessage message = stateMgr.newMessages.poll();
            if(message == null) return;

            if(!stateMgr.getGameName().equals(message.channel)) txtChat.appendText("(" + message.channel + ") ");
            txtChat.appendText(message.user + ": " + message.text + "\n");
        });
        chatSystem.sendNAME(stateMgr.getUsername());
        chatSystem.sendJOIN(stateMgr.getGameName());

        // Retrieve other player info from the Game Server
        updateStatus();

        // Update the map
        updateMap();

        // Keyboard events
        gamePanel.setOnKeyPressed(keyEvent -> {
            System.out.println(keyEvent.getCode());
            if (!(stateMgr.getGameState() == GameState.ACTIVE)) {
                Alert message = new Alert(Alert.AlertType.INFORMATION);
                message.setTitle("Information");
                message.setContentText("You can move or shoot only with a started game.\n Game state: " + stateMgr.getGameState());
                message.showAndWait();
                return;
            }

            char key = keyEvent.getCode().toString().charAt(0);
            if(key == playerSettings.getMoveUp()) movePlayer('N');
            else if (key == playerSettings.getMoveLeft()) movePlayer('W');
            else if (key == playerSettings.getMoveDown()) movePlayer('S');
            else if (key == playerSettings.getMoveRight()) movePlayer('E');
            else if (key == playerSettings.getShootUp()) tryToShoot('N');
            else if (key == playerSettings.getShootLeft()) tryToShoot('W');
            else if (key == playerSettings.getShootDown()) tryToShoot('S');
            else if (key == playerSettings.getShootRight()) tryToShoot('E');
        });
    }

    @FXML
    public void btnGoBackPressed() { quit(); }

    @FXML
    private void btnUpdMapPressed() { updateMap(); }

    @FXML
    private void btnUpdStatusPressed() { updateStatus(); }

    @FXML
    public void txtSendMessage() {
        if(txtMessage.getText().isBlank()) {
            txtMessage.setStyle("-fx-border-color: red");
        } else {
            txtMessage.setStyle("-fx-border-color: none");
            chatSystem.sendPOST(stateMgr.getGameName(), txtMessage.getText());
        }
        txtMessage.setText("");
    }

    public void quit() {
        // Close connection with game server
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getGameName(), "Cause yes");
        if (response.code != ResponseCode.OK) { System.err.println(response.freeText); }
        else { System.out.println(response.freeText); }
        gameServer.closeConnection();

        // Unsubscribe from all chat channels and close connection
        chatSystem.sendLEAVE(stateMgr.getGameName());
        chatSystem.closeConnection();

        Renderer.getInstance().show("mainMenu");
    }

    public void movePlayer(Character position) {
        GameServerResponse res = gameServer.sendMOVE(stateMgr.getGameName(), position);

        switch (res.code) {
            case FAIL:
                System.err.println(res.freeText);
                return;
            case ERROR:
                txtChat.appendText("\nSystem: " + res.freeText);
                // return;
            case OK:
                System.out.println(res.freeText);
        }

        // Should remove in future
        updateMap();
    }

    public void tryToShoot(Character direction){
        GameServerResponse res = gameServer.sendSHOOT(stateMgr.getGameName(), direction);

        switch (res.code) {
            case FAIL -> {
                System.err.println(res.freeText);
                return;
            }
            case ERROR -> {
                txtChat.appendText("\nSystem: " + res.freeText);
                return;
            }
            case OK -> System.out.println(res.freeText);
        }

        Integer energy = stateMgr.getEnergy();
        Character landed = (Character) res.data;
        System.out.println("Ok shot. Landed on: " + landed);

        // Should remove in future
        updateStatus();

        stateMgr.map.drawShot(canvasContext, stateMgr.player.position, stateMgr.getTeam(), direction, landed, energy);
    }

    @FXML
    public void btnStartMatchPressed() {
        GameServerResponse res = gameServer.sendSTART(stateMgr.getGameName());

        if (res.code != ResponseCode.OK) {
            Alert message = new Alert(Alert.AlertType.ERROR);
            message.setTitle("Error");
            message.setContentText(res.freeText);
            message.showAndWait();
            return;
        }
        System.out.println(res.freeText);

        Alert message = new Alert(Alert.AlertType.INFORMATION);
        message.setTitle("Game started!");
        message.setContentText("The minimum number of player is reached, the game has started!");
        message.showAndWait();

        // Should remove in future
        updateStatus();
    }

    // Update ProgressBar correctly
    public void updateEnergy() {
        Integer energyValue = stateMgr.player.energy;
        playerEnergy.setText(energyValue.toString());
        playerEnergyBar.setProgress(((double) energyValue) / 256.0);
    }

    // Update general Status
    public void updateStatus() {
        GameServerResponse res = gameServer.sendSTATUS(stateMgr.getGameName());

        if (res.code != ResponseCode.OK) {
            System.err.println(res.freeText);
            return;
        }
        System.out.println(res.freeText);
        String[] data = (String[]) res.data;

        // Update game status
        String GA = data[0].substring(4); // Remove "GA: "
        stateMgr.updateGameState(GA);

        // Update player status
        String ME = data[1].substring(4); // Remove "ME: "
        stateMgr.player.updateWith(ME);

        // Update list of players
        for (int i = 2; i < data.length; i++) {
            String PL = data[i].substring(4); // Remove "PL: "
            stateMgr.updatePlayerStatus(PL);
        }

        ObservableList<String> finalList = FXCollections.observableArrayList();

        for (Map.Entry<String, PlayerStatus> set : stateMgr.playerList.entrySet()) {
            PlayerStatus pl = set.getValue();
            String playerListName = pl.team + "\t\t\t" + set.getKey() + "\t\t\t\t" + pl.score + "\t" + pl.state;
            finalList.add(playerListName);
        }

        PlayersList.setItems(finalList);

        // Update Game View Values
        if (stateMgr.getGameState() == GameState.ACTIVE && firstTime) {
            txtChat.appendText("\nGame state changed to: " + stateMgr.getGameState());
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Information");
            message.setContentText("Game started, now you can move and shoot!");
            message.showAndWait();
            firstTime = false;
        }

        // Check finished game
        if (stateMgr.getGameState() == GameState.FINISHED) {
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Game finished!");
            message.setHeaderText("Your final score is: " + stateMgr.getScore());
            message.setContentText("Go back to main menu.");
            message.showAndWait().ifPresent(response -> quit());
        }

        playerName.setText(stateMgr.getUsername());

        String loyalty = (stateMgr.getLoyalty() == 0) ? "Normal" : "Impostor";
        playerLoyalty.setText(loyalty);
        if (playerLoyalty.getText().equals("Impostor")) {
            playerLoyalty.setStyle("-fx-text-fill: red;");
        } else {
            playerLoyalty.setStyle("-fx-text-fill: black;");
        }

        playerScore.setText(stateMgr.getScore().toString());
        updateEnergy();
    }

    // Update gameMap
    public void updateMap() {
        GameServerResponse response = gameServer.sendLOOK(stateMgr.getGameName());

        if (response.code != ResponseCode.OK) {
            System.err.println(response.freeText);
            return;
        }
        System.out.println(response.freeText);

        stateMgr.map.setGameMap((String[]) response.data);
        stateMgr.map.drawMap(canvasContext, mapCanvas);
    }

    @FXML
    private void btnAccusePressed(ActionEvent event) {
        responseLabel.setTextFill(Color.RED);

        if(txtPlayerVote.getText().trim().isEmpty()) {
            responseLabel.setText("Player name empty");
            return;
        }

        GameServerResponse response = gameServer.sendACCUSE(stateMgr.getGameName(), txtPlayerVote.getText());

        if(response.code != ResponseCode.OK) {
            responseLabel.setText(response.freeText);
            return;
        }
        responseLabel.setTextFill(Color.GREEN);
        responseLabel.setText(response.freeText);
    }

    @FXML
    private void btnJudgePressed(ActionEvent event) {
        // TODO
    }
}