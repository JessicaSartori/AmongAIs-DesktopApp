package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import it.unipi.cs.smartapp.drivers.*;
import it.unipi.cs.smartapp.statemanager.*;
import it.unipi.cs.smartapp.screens.Renderer;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class gameController implements Controller {
    // Singleton components
    private StateManager stateMgr;
    private PlayerSettings playerSettings;
    private GameServerDriver gameServer;
    private ChatSystemDriver chatSystem;

    // Complex element managers
    private ChatManager chat;
    private TableManager table;

    private Boolean firstTime = true;
    private ScheduledThreadPoolExecutor automaticActions;

    @FXML
    private Label lobbyName, playerLoyalty, playerEnergy, playerScore, lblResponse, lblGameState, lblPlayerState;
    @FXML
    private TextField txtPlayerVote, txtPlayerJudge, txtMessage;
    @FXML
    private ProgressBar playerEnergyBar;
    @FXML
    private Button btnStartMatch;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private Pane leftSubPanel, rightSubPanel;
    @FXML
    private AnchorPane gamePanel;
    @FXML
    private ScrollPane chatPane;
    @FXML
    private TableView<Player> tblPlayers;


    public void initialize() {
        // Initialize singleton components
        stateMgr = StateManager.getInstance();
        playerSettings = PlayerSettings.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();

        // Initialize complex element managers
        chat = new ChatManager(chatPane);
        table = new TableManager(tblPlayers);

        System.out.println("Game Controller done");
    }

    @Override
    public void updateContent() {
        // Reduce commands delay
        gameServer.setMinDelay(150);

        // Setup chat
        chat.setupChat();

        // Initialize status and map
        Controllers.updateStatus(false);
        Controllers.updateMap();

        // Prepare the interface
        lobbyName.setText(stateMgr.getGameName());
        lblGameState.setText(stateMgr.getGameState().toString());
        lblPlayerState.setText(stateMgr.player.getState());
        if (stateMgr.player.getLoyalty() == 0) {
            playerLoyalty.setText("Normal");
            playerLoyalty.setStyle("-fx-text-fill: black;");
        } else {
            playerLoyalty.setText("Impostor");
            playerLoyalty.setStyle("-fx-text-fill: red;");
        }
        playerScore.setText(stateMgr.player.getScore().toString());
        lblGameState.setText(stateMgr.getGameState().toString());
        lblPlayerState.setText(stateMgr.player.getState());
        updateEnergy();
        lblResponse.setText("");
        table.createTable();
        stateMgr.map.drawMap(mapCanvas, stateMgr.playersList, stateMgr.player.getUsername());
        btnStartMatch.setVisible(stateMgr.getCreator());
        leftSubPanel.toFront();
        rightSubPanel.toFront();

        // Setup Keyboard event listener
        gamePanel.setOnKeyPressed(keyEvent -> {
            KeyCode key = keyEvent.getCode();

            if (key == playerSettings.getFlipLeft()) Controllers.flipVisiblePane(leftSubPanel);
            else if (key == playerSettings.getFlipRight()) Controllers.flipVisiblePane(rightSubPanel);

            else if (stateMgr.getGameState() != GameState.ACTIVE) {
                lblResponse.setTextFill(Color.RED);
                lblResponse.setText("Cannot move or shoot while in lobby");
                labelFader(lblResponse, 3.0).play();
            }
            else if (stateMgr.player.getState().equalsIgnoreCase("killed")) {
                lblResponse.setTextFill(Color.RED);
                lblResponse.setText("Cannot move or shoot if dead");
                labelFader(lblResponse, 3.0).play();
            }

            else if (key == playerSettings.getMoveUp()) movePlayer('N');
            else if (key == playerSettings.getMoveLeft()) movePlayer('W');
            else if (key == playerSettings.getMoveDown()) movePlayer('S');
            else if (key == playerSettings.getMoveRight()) movePlayer('E');
            else if (key == playerSettings.getShootUp()) tryToShoot('N');
            else if (key == playerSettings.getShootLeft()) tryToShoot('W');
            else if (key == playerSettings.getShootDown()) tryToShoot('S');
            else if (key == playerSettings.getShootRight()) tryToShoot('E');
        });

        // Setup automatic LOOK and STATUS
        automaticActions = Controllers.setupPoolExecutor();
        automaticActions.scheduleWithFixedDelay(this::updateStatus,
                500, PlayerSettings.getInstance().getStatusFreq(), TimeUnit.MILLISECONDS
        );
        automaticActions.scheduleWithFixedDelay(this::updateMap,
                500, PlayerSettings.getInstance().getMapFreq(), TimeUnit.MILLISECONDS
        );
    }

    @FXML
    public void btnGoBackPressed() {
        automaticActions.shutdownNow();
        chat.closeChat();
        Controllers.closeGameServerConnection();
        Renderer.getInstance().show("mainMenu");
    }

    @FXML
    public void txtSendMessage() {
        if(txtMessage.getText().trim().isEmpty()) {
            txtMessage.setStyle("-fx-border-color: red");
        } else {
            txtMessage.setStyle("-fx-border-color: none");
            chatSystem.sendPOST(stateMgr.getGameName(), txtMessage.getText());
        }
        txtMessage.setText("");
        gamePanel.requestFocus();
    }

    @FXML
    public void btnStartMatchPressed() {
        GameServerResponse res = gameServer.sendSTART(stateMgr.getGameName());

        if (res.code != ResponseCode.OK) {
            Alert message = new Alert(Alert.AlertType.ERROR);
            message.setTitle("Error");
            message.setContentText(res.freeText);
            message.showAndWait();
        }
    }

    @FXML
    private void btnAccusePressed() {
        String username = txtPlayerVote.getText();

        if(username.trim().isEmpty()) {
            txtPlayerVote.setText("");
            txtPlayerVote.setStyle("-fx-border-color: red");
            return;
        }
        txtMessage.setStyle("-fx-border-color: none");

        GameServerResponse response = gameServer.sendACCUSE(stateMgr.getGameName(), username);
        switch (response.code) {
            case FAIL:
                System.err.println(response.freeText);
                return;
            case ERROR:
                lblResponse.setTextFill(Color.RED);
                lblResponse.setText(response.freeText);
                break;
            case OK:
                lblResponse.setTextFill(Color.DARKGREEN);
                lblResponse.setText("You accused " + username + "!");
                break;
        }
        labelFader(lblResponse, 2.0).play();
    }

    @FXML
    private void btnJudgePressed() {
        String username = txtPlayerVote.getText();
        String nature = txtPlayerJudge.getText();

        if(username.trim().isEmpty()) {
            txtPlayerVote.setText("");
            txtPlayerVote.setStyle("-fx-border-color: red");
        }

        if(!nature.equalsIgnoreCase("AI") && !nature.equalsIgnoreCase("H")) {
            txtPlayerJudge.setStyle("-fx-border-color: red");
        }

        GameServerResponse response = gameServer.sendJUDGE(stateMgr.getGameName(), txtPlayerVote.getText(), txtPlayerJudge.getText().toUpperCase());
        switch (response.code) {
            case FAIL:
                System.err.println(response.freeText);
                return;
            case ERROR:
                lblResponse.setTextFill(Color.RED);
                lblResponse.setText(response.freeText);
                return;
            case OK:
                lblResponse.setTextFill(Color.DARKGREEN);
                lblResponse.setText("You judged " + username + " as " + nature + "!");
        }
        labelFader(lblResponse, 2.0).play();
    }


    public void updateStatus() {
        Controllers.updateStatus(false);
        Platform.runLater(() -> {
            playerScore.setText(stateMgr.player.getScore().toString());
            lblGameState.setText(stateMgr.getGameState().toString());
            lblPlayerState.setText(stateMgr.player.getState());
            updateEnergy();

            // Update Game View Values
            if (stateMgr.getGameState() == GameState.ACTIVE && firstTime) {
                lblResponse.setTextFill(Color.DARKGREEN);
                lblResponse.setText("GAME STARTED");
                labelFader(lblResponse, 3.0).play();
                firstTime = false;
            }

            // Check finished game
            if (stateMgr.getGameState() == GameState.FINISHED) {
                automaticActions.shutdownNow();

                Alert message = new Alert(Alert.AlertType.INFORMATION);
                message.setTitle("Game finished!");
                message.setContentText("Continue to see final results");
                message.showAndWait();
                Renderer.getInstance().show("resultScene");
            }
        });
    }

    public void updateMap() {
        Controllers.updateMap();
        Platform.runLater(() -> stateMgr.map.drawMap(mapCanvas, stateMgr.playersList, stateMgr.player.getUsername()));
    }

    public void movePlayer(Character direction) {
        GameServerResponse res = gameServer.sendMOVE(stateMgr.getGameName(), direction);

        switch (res.code) {
            case FAIL:
                System.err.println(res.freeText);
                return;
            case ERROR:
                lblResponse.setTextFill(Color.RED);
                lblResponse.setText(res.freeText);
                labelFader(lblResponse, 2.0).play();
                return;
        }

        Integer[] old_position = stateMgr.player.getPosition();
        stateMgr.map.updatePosition(old_position[0], old_position[1], direction);

        switch (direction) {
            case 'N': old_position[1] -= 1; break;
            case 'S': old_position[1] += 1; break;
            case 'W': old_position[0] -= 1; break;
            case 'E': old_position[0] += 1; break;
        }
        stateMgr.player.setPosition(old_position);

        stateMgr.map.drawMap(mapCanvas, stateMgr.playersList, stateMgr.player.getUsername());
    }

    public void tryToShoot(Character direction){
        GameServerResponse res = gameServer.sendSHOOT(stateMgr.getGameName(), direction);

        switch (res.code) {
            case FAIL:
                System.err.println(res.freeText);
                return;
            case ERROR:
                lblResponse.setTextFill(Color.RED);
                lblResponse.setText(res.freeText);
                labelFader(lblResponse, 2.0).play();
                return;
        }

        Integer energy = stateMgr.player.getEnergy();
        Character landed = (Character) res.data;
        // System.out.println("Ok shot. Landed on: " + landed);

        stateMgr.map.drawShot(mapCanvas, stateMgr.player.getPosition(), stateMgr.player.getTeam(), direction, landed, energy);
    }

    // Update ProgressBar correctly
    public void updateEnergy() {
        Integer energyValue = stateMgr.player.getEnergy();
        playerEnergy.setText(energyValue.toString());
        playerEnergyBar.setProgress(((double) energyValue) / 256.0);
    }

    private FadeTransition labelFader(Node node, Double seconds) {
        FadeTransition fade = new FadeTransition(Duration.seconds(seconds), node);
        fade.setFromValue(1);
        fade.setToValue(0);

        return fade;
    }
}