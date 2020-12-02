package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class createController implements Controller {
    private StateManager stateManager;
    private GameServerDriver gameServer;

    @FXML
    private TextField gameNameField;
    @FXML
    private Label lblMessage;
    @FXML
    private ToggleGroup radioMap, radioShape, radioTeam;

    public void initialize() {
        stateManager = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
        lblMessage.setText("");

        System.out.println("Create Controller done");
    }

    @Override
    public void updateContent() { lblMessage.setText(""); }

    @FXML
    private void btnBackPressed() { Renderer.getInstance().show("mainMenu"); }

    @FXML
    private void btnCreatePressed() {
        String gameName = gameNameField.getText();
        lblMessage.setText("");

        // Get options for graphical elements
        String options = "";
        RadioButton TeamRadioButton = (RadioButton) radioTeam.getSelectedToggle();
        String teamB = TeamRadioButton.getText();
        RadioButton MapShapeRadioButton = (RadioButton) radioShape.getSelectedToggle();
        String mapShape = MapShapeRadioButton.getText();
        RadioButton MapSizeRadioButton = (RadioButton) radioMap.getSelectedToggle();
        String mapSize = MapSizeRadioButton.getText();

        // Set options flags
        options = (teamB.charAt(0) == 'B') ? "B" : "";
        options += (mapShape.equals("Square")) ? "Q" : "W";
        if (mapSize.equals("32")) {
            options += "1";
        } else if (mapSize.equals("64")) {
            options += "2";
        } else {
            options += "3";
        }

        System.out.println("options: " + options);

        if(isValid(gameName) && tryConnect() &&
                create(gameName, options) && join(gameName)) {

            stateManager.setInGame(gameName, true);
            Renderer.getInstance().show("gameScene");
        }
    }

    // Check if the game name is valid (no whitespaces)
    private boolean isValid(String s) {
        boolean valid = !(s.isBlank() || s.contains(" "));
        if(!valid) lblMessage.setText("Invalid Game Name");
        return valid;
    }

    // Try to open a connection to the game server
    // Return false if the connection fails, true otherwise
    private boolean tryConnect() {
        gameServer.openConnection();
        if (!gameServer.isConnected()) lblMessage.setText("Cannot connect to game server");
        return gameServer.isConnected();
    }

    // Send a NEW request and process the response
    // Return true if the lobby is created, false otherwise
    private boolean create(String gameName, String options) {
        GameServerResponse res = gameServer.sendNEW(gameName, options);
        switch (res.code) {
            case ERROR -> lblMessage.setText(res.freeText);
            case FAIL -> {
                System.err.println(res.freeText);
                lblMessage.setText("Cannot create the lobby");
            }
        }
        return (res.code == ResponseCode.OK);
    }

    // Send a JOIN request and process the response
    // Return true if the request succeeded, false otherwise
    private boolean join(String gameName) {
        GameServerResponse res = gameServer.sendJOIN(gameName, stateManager.getUsername(), 'H', stateManager.getPrivateUsername());
        switch (res.code) {
            case ERROR -> lblMessage.setText(res.freeText);
            case FAIL -> {
                System.err.println(res.freeText);
                lblMessage.setText("Cannot join the lobby");
            }
        }
        return (res.code == ResponseCode.OK);
    }
}
