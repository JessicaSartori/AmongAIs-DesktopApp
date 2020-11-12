package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;

import java.util.ArrayList;

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
        String[] response = gameServer.sendSTATUS(stateMgr.getCurrentGameName());

        if (response[0].equals("OK")) {
            String PlayerStatusRow = response[1].split("\n")[2];      // Select only ME row
            String castPlayerStatusRow = PlayerStatusRow.substring(4);      // Remove "ME: "
            String[] PlayerValues = castPlayerStatusRow.split(" ");  // Split by " "

            ArrayList<String> finalValues = new ArrayList<>();

            // Save symbol, name, team, loyalty, energy and score
            for (int i = 0; i < PlayerValues.length; i++) {
                String[] data = PlayerValues[i].split("=");
                finalValues.add(data[1]);
            }

            // Update State Manager
            stateMgr.setSymbol(finalValues.get(0).charAt(0));
            stateMgr.setUsername(finalValues.get(1));
            stateMgr.setTeam(Integer.parseInt(finalValues.get(2)));
            stateMgr.setLoyalty(Integer.parseInt(finalValues.get(3)));
            stateMgr.setEnergy(Integer.parseInt(finalValues.get(4)));
            stateMgr.setScore(Integer.parseInt(finalValues.get(5)));

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

            updateMap();
        } else {
            Alert errorMessage = new Alert(Alert.AlertType.ERROR);
            errorMessage.setTitle("Can't update player status");
            errorMessage.setContentText("There was an error updating the player status");
        }
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) {
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getCurrentGameName(), "Leaving the game");

        if (response.code == ResponseCode.ERROR) {
            System.err.println((String) response.get("freeText"));
            return;
        }
        System.out.println((String) response.get("freeText"));

        stateMgr.setCurrentGameName(null);

        Renderer.getInstance().show("mainMenu");
    }

    @FXML
    private void btnUpdMapPressed(ActionEvent event) { updateMap(); }

    @FXML
    private void btnUpdStatusPressed(ActionEvent event) {
        String[] res = gameServer.sendSTATUS(stateMgr.getCurrentGameName());
        // Fetch elements in "ME" line
        String PlayerStatus = res[1].split("\n")[2];
        String playerValues[] = PlayerStatus.split(" ");

        // Symbol
        playerValues[1] = playerValues[1].replace("symbol=", "");
        stateMgr.setSymbol(playerValues[1].charAt(0));

        // Energy
        playerValues[5] = playerValues[5].replace("energy=", "");
        PlayerEnergy.setText(playerValues[5]);
        stateMgr.setEnergy(Integer.parseInt(playerValues[5]));

        // Score
        playerValues[6] = playerValues[6].replace("score=", "");
        PlayerScore.setText(playerValues[6]);
        stateMgr.setScore(Integer.parseInt(playerValues[6]));
    }

    @FXML
    public void txtSendMessage(ActionEvent event) {
        txtChat.appendText("\n" + stateMgr.getUsername() + ": " + txtMessage.getText());
        txtMessage.setText("");
    }

    @FXML
    public void btnStartMatchPressed(ActionEvent event) {
        GameServerResponse res = gameServer.sendSTART(stateMgr.getCurrentGameName());
        System.out.println((String) res.get("freeText"));
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
        // Quick fix - improvements coming soon directly on GameServerDriver
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
        int rowCoordinate = 0, columnCoordinate = 0, cellDimension = 13;
        Character charMap[][] = stateMgr.getGameMap();

        for(int r=0; r<32; r++) { // rows
            for (int c = 0; c < 32; c++) { // columns
                setColor(charMap[r][c]);

                canvasContext.fillRect(rowCoordinate, columnCoordinate, cellDimension, cellDimension);

                columnCoordinate += cellDimension;
            }
            rowCoordinate += cellDimension;
            columnCoordinate = 0;
        }
    }

    private void setColor(Character terrain) {
        switch (terrain) {
            case '.': // grass
                canvasContext.setFill(Color.GREEN);
                break;
            case '#': // wall
                canvasContext.setFill(Color.GRAY);
                break;
            case '~': // river
                canvasContext.setFill(Color.CYAN);
                break;
            case '@': // ocean
                canvasContext.setFill(Color.BLUE);
                break;
            case '!': // trap
                canvasContext.setFill(Color.FIREBRICK);
                break;
            case '$': // energy recharge
                canvasContext.setFill(Color.YELLOW);
                break;
            case '&': // barrier
                canvasContext.setFill(Color.BROWN);
                break;
            case 'x': // flags - TODO distinguish color by team
            case 'X':
                canvasContext.setFill(Color.ORANGE);
                break;
            default: // player - TODO distinguish teams and current player from others
                canvasContext.setFill(Color.MAGENTA);
        }
    }
}
