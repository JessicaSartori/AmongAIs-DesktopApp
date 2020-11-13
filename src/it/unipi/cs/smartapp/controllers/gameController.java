package it.unipi.cs.smartapp.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
                    message.setContentText("You can move or shoot only with a started game.\n Game state: " + stateMgr.getGameState());
                    message.showAndWait();
                    return;
                }

                switch (keyEvent.getCode().toString()) {
                    case "A":
                        break;

                    case "W":
                        break;

                    case "D":
                        break;

                    case "S":
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
        String res[] = gameServer.sendSHOOT(stateMgr.getCurrentGameName(), direction);

        if(res[0].equals("OK")){
            Character landed = res[1].charAt(0);
            System.out.println("Ok shot. Landed on: " + landed);

            // TODO - find out coordinates of landed

            updateStatus();
        }
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
        message.setContentText("The minimum number of player is reached, the game is started!");
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

        if (stateMgr.getGameState().equals("ACTIVE") && firstTime) {
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Information");
            message.setContentText("Game started, now you can move and shoot!");
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

    // Update gameMap
    public void updateMap() {
        String response[] = gameServer.sendLOOK(stateMgr.getCurrentGameName());

        if (response[0].equals("OK")) {
            stateMgr.setGameMap(stringToCharMap(response[1]));
            drawMap();
        } else {
            System.err.println(response[1]);
        }
    }

    private Character[][] stringToCharMap(String mapResponse) {
        // Quick fix
        // Map is currently limited to 32x32 - increasing in the future
        mapResponse = mapResponse.replace("LONG\n", "");

        Character[][] parsedMap = new Character[32][32];

        int i = 0; // String index

        for(int r=0; r<32; r++) // rows
            for(int c=0; c<32; c++) { // columns
                if(mapResponse.charAt(i) == '\n') // skip this character
                    i++;

                parsedMap[r][c] = mapResponse.charAt(i);

                i++; // now we need to increase anyways
            }

        return parsedMap;
    }

    private void drawMap() {
        // TODO update when maps won't be simply 32x32
        int xCanvas = 0, yCanvas = 0, cellDimension = 13;
        Character charMap[][] = stateMgr.getGameMap();

        for(int r=0; r<32; r++) { // rows
            for (int c = 0; c < 32; c++) { // columns
                System.out.print(charMap[r][c]);
            }
            System.out.println("");
        }

        for(int r=0; r<32; r++) { // rows
            for (int c = 0; c < 32; c++) { // columns
                setColor(charMap[r][c]);

                canvasContext.fillRect(xCanvas, yCanvas, cellDimension, cellDimension);

                xCanvas += cellDimension;
            }
            yCanvas += cellDimension;
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
