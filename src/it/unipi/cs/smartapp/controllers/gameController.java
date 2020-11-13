package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
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
        btnUpdStatusPressed(null);

        // Update the map
        updateMap();
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
    private void btnUpdStatusPressed(ActionEvent event) {
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
        }

        PlayerScore.setText(stateMgr.getScore().toString());
        PlayerEnergy.setText(stateMgr.getEnergy().toString());

        String loyalty = (stateMgr.getLoyalty() == 0) ? "Normal" : "Impostor";
        PlayerLoyalty.setText(loyalty);
        if (PlayerLoyalty.getText() == "Impostor") {
            PlayerLoyalty.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void txtSendMessage(ActionEvent event) {
        txtChat.appendText("\n" + stateMgr.getUsername() + ": " + txtMessage.getText());
        txtMessage.setText("");
    }

    @FXML
    public void btnStartMatchPressed(ActionEvent event) {
        GameServerResponse res = gameServer.sendSTART(stateMgr.getCurrentGameName());
        System.out.println(res.freeText);
    }

    // Update ProgressBar correctly
    public void decreaseEnergy(Integer energyValue) {
        Integer newValue = stateMgr.getEnergy() - energyValue;

        if (newValue < 0) {
            newValue = 0;
        }

        stateMgr.setEnergy(newValue);
        PlayerEnergy.setText(newValue.toString());
        Double barValue = (newValue < 0) ? 0 : (newValue / 256.0);
        PlayerEnergyBar.setProgress(Double.parseDouble(barValue.toString()));
    }

    public void increaseEnergy(Integer energyValue) {
        Integer newValue = stateMgr.getEnergy() + energyValue;

        if (newValue > 256) {
            newValue = 256;
        }

        stateMgr.setEnergy(newValue);
        PlayerEnergy.setText(newValue.toString());
        Double barValue = (newValue > 256) ? 1.0 : (newValue / 256.0);
        PlayerEnergyBar.setProgress(Double.parseDouble(barValue.toString()));
    }

    public void updateMap() {
        String response[] = gameServer.sendLOOK(stateMgr.getCurrentGameName());

        if (response[0].equals("OK")) {
            stateMgr.setGameMap(stringToCharMap(response[1]));
            drawMap();
        } else {
            canvasContext.setStroke(Color.RED);
            canvasContext.strokeText("CANVAS ERROR!", 314, 314);
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
