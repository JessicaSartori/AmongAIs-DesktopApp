package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import it.unipi.cs.smartapp.drivers.*;
import it.unipi.cs.smartapp.statemanager.*;
import it.unipi.cs.smartapp.screens.Renderer;


public class gameController implements Controller {
    private StateManager stateMgr;
    private PlayerSettings playerSettings;
    private GameServerDriver gameServer;
    private ChatSystemDriver chatSystem;

    private GraphicsContext canvasContext;
    private ChatManager chat;
    private TableManager table;

    private Boolean firstTime = true;

    @FXML
    private Label lobbyName, playerName, playerLoyalty, playerEnergy, playerScore, lblResponse;
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
        stateMgr = StateManager.getInstance();
        playerSettings = PlayerSettings.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();

        canvasContext = mapCanvas.getGraphicsContext2D();
        chat = new ChatManager(chatPane);
        table = new TableManager(tblPlayers);

        lblResponse.setText("");

        System.out.println("Game Controller done");
    }

    @Override
    public void updateContent() {
        gameServer.setMinDelay(150);

        // Prepare the interface
        btnStartMatch.setVisible(stateMgr.getCreator());
        lobbyName.setText(stateMgr.getGameName());
        playerName.setText(stateMgr.getUsername());
        lblResponse.setText("");

        // Setup chat
        chat.setupChat();

        // Retrieve other player info from the Game Server
        Controllers.updateStatus(false);

        // Setup table with players info
        table.createTable();

        // Update the interface with status information
        if (stateMgr.player.getLoyalty() == 0) {
            playerLoyalty.setText("Normal");
            playerLoyalty.setStyle("-fx-text-fill: black;");
        } else {
            playerLoyalty.setText("Impostor");
            playerLoyalty.setStyle("-fx-text-fill: red;");
        }
        playerScore.setText(stateMgr.player.getScore().toString());
        updateEnergy();

        // Update the map
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, mapCanvas, stateMgr.playersList, stateMgr.player.getUsername());

        // Keyboard events
        gamePanel.setOnKeyPressed(keyEvent -> {
            if (!(stateMgr.getGameState() == GameState.ACTIVE)) {
                lblResponse.setText("Can not move or shoot while in lobby");
                return;
            }

            KeyCode key = keyEvent.getCode();
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
    private void btnUpdStatusPressed() {
        Controllers.updateStatus(false);
        updateEnergy();

        // Update Game View Values
        if (stateMgr.getGameState() == GameState.ACTIVE && firstTime) {
            lblResponse.setText("Game started, now you can move and shoot!");
            firstTime = false;
        }

        // Check finished game
        if (stateMgr.getGameState() == GameState.FINISHED) {
            Renderer.getInstance().show("resultScene");
        }
    }

    @FXML
    private void btnUpdMapPressed() {
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, mapCanvas, stateMgr.playersList, stateMgr.player.getUsername());
    }

    @FXML
    public void btnGoBackPressed() {
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getGameName(), "Done playing");
        if (response.code != ResponseCode.OK) { System.err.println(response.freeText); }
        gameServer.closeConnection();
        gameServer.setMinDelay(500);

        chat.closeChat();

        Renderer.getInstance().show("mainMenu");
    }

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

    public void movePlayer(Character position) {
        GameServerResponse res = gameServer.sendMOVE(stateMgr.getGameName(), position);

        switch (res.code) {
            case FAIL:
                System.err.println(res.freeText);
                return;
            case ERROR:
                lblResponse.setText(res.freeText);
                //return;
            case OK:
                System.out.println(res.freeText);
        }

        // Should remove in future
        Controllers.updateMap();
        stateMgr.map.drawMap(canvasContext, mapCanvas, stateMgr.playersList, stateMgr.player.getUsername());
    }

    public void tryToShoot(Character direction){
        GameServerResponse res = gameServer.sendSHOOT(stateMgr.getGameName(), direction);

        switch (res.code) {
            case FAIL -> {
                System.err.println(res.freeText);
                return;
            }
            case ERROR -> {
                lblResponse.setText(res.freeText);
                return;
            }
            case OK -> System.out.println(res.freeText);
        }

        Integer energy = stateMgr.player.getEnergy();
        Character landed = (Character) res.data;
        System.out.println("Ok shot. Landed on: " + landed);

        // Should remove in future
        Controllers.updateStatus(false);

        stateMgr.map.drawShot(canvasContext, stateMgr.player.getPosition(), stateMgr.player.getTeam(), direction, landed, energy);
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
        Controllers.updateStatus(false);
    }

    // Update ProgressBar correctly
    public void updateEnergy() {
        Integer energyValue = stateMgr.player.getEnergy();
        playerEnergy.setText(energyValue.toString());
        playerEnergyBar.setProgress(((double) energyValue) / 256.0);
    }

    @FXML
    private void btnAccusePressed() {
        lblResponse.setTextFill(Color.RED);

        if(txtPlayerVote.getText().trim().isEmpty()) {
            lblResponse.setText("Player name empty");
            return;
        }

        GameServerResponse response = gameServer.sendACCUSE(stateMgr.getGameName(), txtPlayerVote.getText());

        if(response.code != ResponseCode.OK) {
            lblResponse.setText(response.freeText);
            return;
        }
        lblResponse.setTextFill(Color.GREEN);
        lblResponse.setText(response.freeText);
    }

    @FXML
    private void btnJudgePressed() {
        lblResponse.setTextFill(Color.RED);

        if(txtPlayerVote.getText().trim().isEmpty()) {
            lblResponse.setText("Player name empty");
            return;
        }

        System.out.println("Vote: " + txtPlayerVote.getText() + " Judge: " + txtPlayerJudge.getText());
        if(!txtPlayerJudge.getText().equalsIgnoreCase("AI") && !txtPlayerJudge.getText().equalsIgnoreCase("H")) {
            lblResponse.setText("Invalid nature");
            return;
        }

        GameServerResponse response = gameServer.sendJUDGE(stateMgr.getGameName(), txtPlayerVote.getText(), txtPlayerJudge.getText().toUpperCase());
        System.out.println("Response code: " + response.code + " free text: " + response.freeText);

        if(response.code != ResponseCode.OK) {
            lblResponse.setText(response.freeText);
            return;
        }
        lblResponse.setTextFill(Color.GREEN);
        lblResponse.setText(response.freeText);

    }
}