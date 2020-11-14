package it.unipi.cs.smartapp.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.Chart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;


public class gameController implements Controller {
    private StateManager stateMgr;
    private GameServerDriver gameServer;

    private GraphicsContext canvasContext;

    private Boolean firstTime = true;

    @FXML
    private Label PlayerName;
    @FXML
    private Label PlayerTeam;
    @FXML
    private Label PlayerScore;
    @FXML
    private Label PlayerLoyalty;
    @FXML
    private Label PlayerEnergy;
    @FXML
    private TextArea txtChat;
    @FXML
    private TextField txtMessage;
    @FXML
    private ProgressBar PlayerEnergyBar;
    @FXML
    private Button btnStartMatch;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private AnchorPane gamePanel;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();

        canvasContext = mapCanvas.getGraphicsContext2D();

        System.out.println("Game Controller done");
    }

    @Override
    public void updateContent() {
        if (!stateMgr.getCreator()) {
            btnStartMatch.setVisible(false);
        }

        // Retrieve other player info from the Game Server
        updateStatus();

        // Update the map
        updateMap();

        // Keyboard events for moving
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent keyEvent) {

                if (!stateMgr.getGameState().equals("ACTIVE")) {
                    Alert message = new Alert(Alert.AlertType.INFORMATION);
                    message.setTitle("Information");
                    message.setContentText("The game must start or not finished.\nGame state: " + stateMgr.getGameState());
                    message.showAndWait();
                    return;
                }

                switch (keyEvent.getCode().toString()) {
                    case "A":
                        movePlayer('W');
                        break;
                    case "W":
                        movePlayer('N');
                        break;
                    case "D":
                        movePlayer('E');
                        break;
                    case "S":
                        movePlayer('S');
                        break;

                    case "I":
                        tryToShoot('N');
                        break;
                    case "J":
                        tryToShoot('W');
                        break;
                    case "K":
                        tryToShoot('S');
                        break;
                    case "L":
                        tryToShoot('E');
                        break;
                }
            }
        });
    }

    public void tryToShoot(Character direction){
        GameServerResponse res = gameServer.sendSHOOT(stateMgr.getCurrentGameName(), direction);

        if(res.code != ResponseCode.OK) {
            System.err.println(res.freeText);
            return;
        }

        Character landed = (Character) res.data;
        System.out.println("Ok shot. Landed on: " + landed);

        // TODO - find out coordinates of landed
        // TODO - add "explosion" on map ?

        updateStatus();
    }

    public void movePlayer(Character position) {
        String[] res = gameServer.sendMOVE(stateMgr.getCurrentGameName(), position);

        if (res[0].equals("OK")) {
            updateMap();
        }

        System.out.println(res[1]);
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) {
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getCurrentGameName(), "Leaving the game");

        if (response.code == ResponseCode.ERROR) {
            System.err.println(response.freeText);
            return;
        }
        System.out.println(response.freeText);

        stateMgr.setCurrentGameName(null);

        Renderer.getInstance().show("mainMenu");
    }

    @FXML
    private void btnUpdMapPressed(ActionEvent event) { updateMap(); }

    @FXML
    private void btnUpdStatusPressed(ActionEvent event) { updateStatus(); }

    @FXML
    public void txtSendMessage(ActionEvent event) {
        txtChat.appendText("\n" + stateMgr.getUsername() + ": " + txtMessage.getText());
        txtMessage.setText("");
    }

    // Update general Status
    public void updateStatus() {
        GameServerResponse res = gameServer.sendSTATUS(stateMgr.getCurrentGameName());

        if (res.code != ResponseCode.OK) {
            System.err.println(res.freeText);
            return;
        }

        System.out.println(res.freeText);

        txtChat.appendText("\nWelcome, you are in " + stateMgr.getCurrentGameName());

        String[] data = (String[]) res.data;

        // Update game status
        String GA = data[0].substring(4); // Remove "GA: "
        stateMgr.updateGameState(GA);

        // Alert (do the same with the ending of the game)
        if (stateMgr.getGameState().equals("ACTIVE") && firstTime) {
            txtChat.appendText("\nGame state: " + stateMgr.getGameState());
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Information");
            message.setContentText("Game started!");
            message.showAndWait();
            firstTime = false;
        }

        // Update player status
        String ME = data[1].substring(4); // Remove "ME: "
        stateMgr.player.updateWith(ME);

        for (int i = 2; i < data.length; i++) {
            String PL = data[i].substring(4); // Remove "PL: "
            stateMgr.updatePlayerStatus(PL);
        }

        // Update Game View Values
        PlayerName.setText(stateMgr.getUsername());

        String team = (stateMgr.getTeam() == 0) ? "Red Team" : "Blue Team";
        PlayerTeam.setText(team);
        if (PlayerTeam.getText() == "Blue Team") {
            PlayerTeam.setStyle("-fx-background-color: blue");
        } else {
            PlayerTeam.setStyle("-fx-background-color: red");
        }

        PlayerScore.setText(stateMgr.getScore().toString());
        PlayerEnergy.setText(stateMgr.getEnergy().toString());
        updateEnergy(stateMgr.getEnergy());

        String loyalty = (stateMgr.getLoyalty() == 0) ? "Normal" : "Impostor";
        PlayerLoyalty.setText(loyalty);
        if (PlayerLoyalty.getText() == "Impostor") {
            PlayerLoyalty.setStyle("-fx-text-fill: red;");
        } else {
            PlayerLoyalty.setStyle("-fx-text-fill: black;");
        }
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

        stateMgr.setGameState("ACTIVE");

        System.out.println(res.freeText);

        Alert message = new Alert(Alert.AlertType.INFORMATION);
        message.setTitle("Game started!");
        message.setContentText("The game is started, enjoy!");
        message.showAndWait();
    }

    // Update ProgressBar correctly
    public void updateEnergy(Integer energyValue) {
        if (energyValue < 0) {
            energyValue = 0;
        }

        stateMgr.setEnergy(energyValue);
        PlayerEnergy.setText(energyValue.toString());
        Double barValue = (energyValue < 0) ? 0 : (energyValue / 256.0);
        PlayerEnergyBar.setProgress(Double.parseDouble(barValue.toString()));
    }

    // Update gameMap
    public void updateMap() {
        String response[] = gameServer.sendLOOK(stateMgr.getCurrentGameName());

        if (response[0].equals("OK")) {
            stateMgr.map.setGameMap(stringToCharMap(response[1]));
            drawMap();
        } else {
            System.err.println(response[1]);
        }
    }

    private Character[][] stringToCharMap(String mapResponse) {
        mapResponse = mapResponse.replace("LONG\n", "");

        Integer size = stateMgr.map.getMapSize();
        Character[][] parsedMap = new Character[size][size];

        int i = 0; // String index

        for(int r=0; r<size; r++)
            for(int c=0; c<size; c++) {
                if(mapResponse.charAt(i) == '\n') // Skip this character
                    i++;

                parsedMap[r][c] = mapResponse.charAt(i);
                i++;
            }

        return parsedMap;
    }

    private void drawMap() {
        Integer size = stateMgr.map.getMapSize();
        Integer cellSize = stateMgr.map.getCellSize();
        Character charMap[][] = stateMgr.map.getGameMap();

        int xCanvas = 0, yCanvas = 0;
        for(int r=0; r<size; r++) {
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
        switch (value) {
            case '.': // Grass
                canvasContext.setFill(Color.web("#009432"));
                break;
            case '#': // Wall
                canvasContext.setFill(Color.web("#718093"));
                break;
            case '~': // River
                canvasContext.setFill(Color.web("#00FFFF"));
                break;
            case '@': // Ocean
                canvasContext.setFill(Color.web("#006b6b"));
                break;
            case '!': // Trap
                canvasContext.setFill(Color.web("#ff8a00"));
                break;
            case '$': // Energy recharge
                canvasContext.setFill(Color.web("#fffd50"));
                break;
            case '&': // Barrier
                canvasContext.setFill(Color.web("#3b1909"));
                break;
            case 'X': // Flag team 0
                canvasContext.setFill(Color.web("#fdbda7"));
                break;
            case 'x': // Flag team 1
                canvasContext.setFill(Color.web("#b7beff"));
                break;
            default: // Players
                if(value == stateMgr.getSymbol()){
                    // Current player
                    if(stateMgr.getTeam() == 0)
                        canvasContext.setFill(Color.web("#ff0000"));
                    else
                        canvasContext.setFill(Color.web("#0000ff"));
                    break;
                }
                if(Character.isUpperCase(value)){
                    // 0 -> TEAM RED, uppercase letters
                    canvasContext.setFill(Color.web("#f25656"));
                    break;
                }
                if(Character.isLowerCase(value)) {
                    // 1 -> TEAM BLUE, lowercase letters
                    canvasContext.setFill(Color.web("#0652DD"));
                    break;
                }
                // Unknown
                canvasContext.setFill(Color.web("#000000"));
        }
    }
}
