package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

import java.awt.*;

public class gameController implements Controller {
    private StateManager stateMgr;
    private GameServerDriver gameServer;

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

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();

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
    }

    @FXML
    public void btnGoBackPressed(ActionEvent event) {
        Renderer.getInstance().show("mainMenu");
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
}
