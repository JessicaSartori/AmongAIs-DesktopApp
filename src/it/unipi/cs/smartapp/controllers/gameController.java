package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

import java.awt.*;

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
        // Retrieve player info from the state manager
        PlayerName.setText(stateMgr.getUsername());
        if (!stateMgr.getCreator()) {
            btnStartMatch.setVisible(false);
        }

        // Retrieve other player info from the Game Server
        String[] res = gameServer.sendSTATUS(stateMgr.getCurrentGameName());

        // Split the Game Server response
        String PlayerStatus = res[1].split("\n")[2];
        String castPlayerStatus = PlayerStatus.substring(PlayerStatus.length() - 35, PlayerStatus.length() - 8);
        String[] PlayerValues = castPlayerStatus.split(" ");
        System.out.println("Full string: " + PlayerStatus);
        System.out.println("Cast string: " + castPlayerStatus);

        // It will contain team, loyalty, energy and score value
        String[] FinalValues = new String[3];
        int count = 0;

        // Final splitting of Game Server response
        for (int i = 0; i < PlayerValues.length; i++) {
            String[] data = PlayerValues[i].split("=");
            FinalValues[count] = data[1];
            System.out.println(FinalValues[count]);
            count++;
        }

        // Update State Manager
        stateMgr.setTeam(Integer.parseInt(FinalValues[0]));
        stateMgr.setLoyalty(Integer.parseInt(FinalValues[1]));
        stateMgr.setEnergy(Integer.parseInt(FinalValues[2]));

        // Update Game View Values
        String team = (stateMgr.getTeam() == 0) ? "Red Team" : "Blue Team";
        PlayerTeam.setText(team);
        if (PlayerTeam.getText() == "Blue Team") {
            PlayerTeam.setStyle("-fx-background-color: blue");
        }

        PlayerScore.setText("0");

        PlayerEnergy.setText(stateMgr.getEnergy().toString());

        String loyalty = (stateMgr.getLoyalty() == 0) ? "Normal" : "Impostor";
        PlayerLoyalty.setText(loyalty);
        if (PlayerLoyalty.getText() == "Impostor") {
            PlayerLoyalty.setStyle("-fx-text-fill: red;");
        }

        updateMap();
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) {
        Renderer.getInstance().show("mainMenu");
    } //Aggiungere sendLEAVE?

    @FXML
    private void btnUpdMapPressed(ActionEvent event) { updateMap(); }

    @FXML
    private void btnUpdStatusPressed(ActionEvent event) {
        // TODO implement update status
    }

    @FXML
    public void txtSendMessage(ActionEvent event) {
        txtChat.appendText("\n" + stateMgr.getUsername() + ": " + txtMessage.getText());
        txtMessage.setText("");
    }

    @FXML
    public void btnStartMatchPressed(ActionEvent event) {
        String[] res = gameServer.sendSTART(stateMgr.getCurrentGameName());
        System.out.println(res[0] + " " + res[1]);
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

        if (response[0].equals("OK")){
            char[][] charMap = stringToCharMap(response[1]);
            drawMap(charMap);
        }
        else {
            canvasContext.setStroke(Color.RED);
            canvasContext.strokeText("CANVAS ERROR!", 314, 314);
        }
    }

    private char[][] stringToCharMap(String mapResponse) {
        // Quick fix - improvements coming soon directly on GameServerDriver
        // Map is currently limited to 32x32 - increasing in the future
        mapResponse = mapResponse.replace("LONG\n", "");

        char[][] parsedMap = new char[32][32];

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

    private void drawMap(char[][] charMap) {
        // TODO update when maps won't be simply 32x32
        int rowCoordinate = 0, columnCoordinate = 0, cellDimension = 13;

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

    private void setColor(char terrain) {
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
