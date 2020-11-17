package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;

import it.unipi.cs.smartapp.drivers.*;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;


public class gameController implements Controller {
    private StateManager stateMgr;
    private GameServerDriver gameServer;
    private ChatSystemDriver chatSystem;

    private GraphicsContext canvasContext;

    private Boolean firstTime = true;

    @FXML
    private Label playerName, playerTeam, playerLoyalty, playerEnergy, playerScore;
    @FXML
    private TextArea txtChat;
    @FXML
    private TextField txtMessage;
    @FXML
    private ProgressBar playerEnergyBar;
    @FXML
    private Button btnStartMatch;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private AnchorPane gamePanel;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
        chatSystem = ChatSystemDriver.getInstance();

        canvasContext = mapCanvas.getGraphicsContext2D();

        System.out.println("Game Controller done");
    }

    @Override
    public void updateContent() {
        btnStartMatch.setVisible(stateMgr.getCreator());

        // Add lobby name in the chat
        txtChat.appendText("\nLobby name: " + stateMgr.getCurrentGameName());

        // Setup chat
        chatSystem.setMessageCallback(() -> {
            String[] message = stateMgr.newMessage;
            stateMgr.newMessage = null;
            txtChat.appendText("\n(" + message[0] + ") " + message[1] + ": " + message[2]);
        });
        chatSystem.sendNAME(stateMgr.getUsername());
        chatSystem.sendJOIN(stateMgr.getCurrentGameName());
        chatSystem.sendJOIN("#GLOBAL");

        // Retrieve other player info from the Game Server
        updateStatus();

        // Update the map
        updateMap();

        // Keyboard events
        gamePanel.setOnKeyPressed(keyEvent -> {
            if (!stateMgr.getGameState().equals("ACTIVE")) {
                Alert message = new Alert(Alert.AlertType.INFORMATION);
                message.setTitle("Information");
                message.setContentText("You can move or shoot only with a started game.\n Game state: " + stateMgr.getGameState());
                message.showAndWait();
                return;
            }

            switch (keyEvent.getCode().toString()) {
                case "W" -> movePlayer('N');
                case "A" -> movePlayer('W');
                case "S" -> movePlayer('S');
                case "D" -> movePlayer('E');
                case "I" -> tryToShoot('N');
                case "J" -> tryToShoot('W');
                case "K" -> tryToShoot('S');
                case "L" -> tryToShoot('E');
            }
        });
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) { quit(); }

    @FXML
    private void btnUpdMapPressed(ActionEvent event) { updateMap(); }

    @FXML
    private void btnUpdStatusPressed(ActionEvent event) { updateStatus(); }

    @FXML
    public void txtSendMessage(ActionEvent event) {
        if(txtMessage.getText().isBlank()) {
            txtMessage.setStyle("-fx-border-color: red");
        } else {
            txtMessage.setStyle("-fx-border-color: none");
            chatSystem.sendPOST(stateMgr.getCurrentGameName(), txtMessage.getText());
        }
        txtMessage.setText("");
    }

    public void quit() {
        // Close connection with game server
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getCurrentGameName(), "Leaving the game");
        if (response.code != ResponseCode.OK) { System.err.println(response.freeText); }
        else { System.out.println(response.freeText); }

        // Unsubscribe from all chat channels
        chatSystem.sendLEAVE(stateMgr.getCurrentGameName());
        chatSystem.sendLEAVE("#GLOBAL");
        // TODO: should close also chat connection?

        stateMgr.setCurrentGameName(null);

        Renderer.getInstance().show("mainMenu");
    }

    public void movePlayer(Character position) {
        GameServerResponse res = gameServer.sendMOVE(stateMgr.getCurrentGameName(), position);

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
        GameServerResponse res = gameServer.sendSHOOT(stateMgr.getCurrentGameName(), direction);

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

        Character landed = (Character) res.data;
        System.out.println("Ok shot. Landed on: " + landed);

        // TODO - find out coordinates of landed
        // TODO - add "explosion" on map ?

        // Should remove in future
        updateStatus();
    }

    @FXML
    public void btnStartMatchPressed(ActionEvent event) {
        GameServerResponse res = gameServer.sendSTART(stateMgr.getCurrentGameName());

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
    public void updateEnergy(Integer energyValue) {
        if (energyValue < 0) energyValue = 0;

        stateMgr.setEnergy(energyValue);
        playerEnergy.setText(energyValue.toString());
        playerEnergyBar.setProgress(((double) energyValue) / 256.0);
    }

    // Update general Status
    public void updateStatus() {
        GameServerResponse res = gameServer.sendSTATUS(stateMgr.getCurrentGameName());

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

        // Update Game View Values
        if (stateMgr.getGameState().equals("ACTIVE") && firstTime) {
            txtChat.appendText("\nGame state changed to: " + stateMgr.getGameState());
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Information");
            message.setContentText("Game started, now you can move and shoot!");
            message.showAndWait();
            firstTime = false;
        }

        // Check finished game
        if (stateMgr.getGameState().equals("FINISHED")) {
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Game finished!");
            message.setHeaderText("Your final score is: " + stateMgr.getScore());
            message.setContentText("Go back to main menu.");
            message.showAndWait().ifPresent(response -> quit());
        }

        playerName.setText(stateMgr.getUsername());

        String team = (stateMgr.getTeam() == 0) ? "Red Team" : "Blue Team";
        playerTeam.setText(team);
        if (playerTeam.getText().equals("Blue Team")) {
            playerTeam.setStyle("-fx-background-color: blue");
        } else {
            playerTeam.setStyle("-fx-background-color: red");
        }

        String loyalty = (stateMgr.getLoyalty() == 0) ? "Normal" : "Impostor";
        playerLoyalty.setText(loyalty);
        if (playerLoyalty.getText().equals("Impostor")) {
            playerLoyalty.setStyle("-fx-text-fill: red;");
        } else {
            playerLoyalty.setStyle("-fx-text-fill: black;");
        }

        playerScore.setText(stateMgr.getScore().toString());
        playerEnergy.setText(stateMgr.getEnergy().toString());
        updateEnergy(stateMgr.getEnergy());
    }

    // Update gameMap
    public void updateMap() {
        GameServerResponse response = gameServer.sendLOOK(stateMgr.getCurrentGameName());

        if (response.code != ResponseCode.OK) {
            System.err.println(response.freeText);
            return;
        }
        System.out.println(response.freeText);


        stateMgr.map.setGameMap(stringToCharMap((String[]) response.data));
        drawMap();
    }

    private Character[][] stringToCharMap(String[] rows) {
        Integer size = stateMgr.map.getMapSize();
        Character[][] parsedMap = new Character[size][size];

        for(int r=0; r < size; r++)
            for(int c=0; c < size; c++) {
                parsedMap[r][c] = rows[r].charAt(c);
            }

        return parsedMap;
    }

    private void drawMap() {
        Integer size = stateMgr.map.getMapSize();
        Integer cellSize = stateMgr.map.getCellSize();
        Character[][] charMap = stateMgr.map.getGameMap();

        int xCanvas = 0, yCanvas = 0;
        for(int r=0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                setColor(charMap[r][c]);

                canvasContext.fillRect(xCanvas, yCanvas, cellSize, cellSize);

                xCanvas += cellSize;
            }
            yCanvas += cellSize;
            xCanvas = 0;
        }
    }

    private void setColor(Character value) {

        Color color = Color.web("#000000");

        switch (value) {
            case '.' -> color = Color.web("#009432"); // Grass
            case '#' -> color = Color.web("#718093"); // Wall
            case '~' -> color = Color.web("#00FFFF"); // River
            case '@' -> color = Color.web("#006b6b"); // Ocean
            case '!' -> color = Color.web("#ff8a00"); // Trap
            case '$' -> color = Color.web("#fffd50"); // Energy recharge
            case '&' -> color = Color.web("#3b1909"); // Barrier
            case 'X' -> color = Color.web("#fdbda7"); // Flag team 0
            case 'x' -> color = Color.web("#b7beff"); // Flag team 1
            default -> { // Players
                if (value == stateMgr.getSymbol()) {
                    // Current player
                    color = (stateMgr.getTeam() == 0) ? Color.web("#ff0000") : Color.web("#0000ff");
                } else {
                    // Other players
                    if(Character.isUpperCase(value)) color = Color.web("#f25656");
                    else if(Character.isLowerCase(value)) color = Color.web("#0652DD");
                }
            }
        }
        canvasContext.setFill(color);
    }
}